package com.github.fntz.omhs.impl

import com.github.fntz.omhs.AsyncResult
import com.github.fntz.omhs.moar.MutableState

import scala.quoted.*

private[omhs] object MoarImpl {
  import Shared.*

  def generate[F: Type](body: Expr[F])(using Quotes): Expr[Any] = {
    import quotes.reflect.*

    val stateClass: Expr[Class[?]] = '{ classOf[MutableState] }
    val logger = new Logger()

    def unwrap(tree: Tree): Tree = tree match
      case Inlined(_, _, t) => unwrap(t)
      case Block(stats, expr) =>
        unwrap(expr) match
          case Closure(meth, _) =>
            stats.collectFirst {
              case d: DefDef if d.symbol == meth.symbol => d
            }.getOrElse(unwrap(expr))
          case other => other
      case Closure(meth, _) =>
        meth.symbol.tree match
          case d: DefDef => d
          case _         => tree
      case other => other

    def moarMethodName(name: String): Option[String] = name match
      case "contentType" => Some("setContentType")
      case "status"      => Some("setStatus")
      case "setCookie"   => Some("setCookie")
      case "setHeader"   => Some("setHeader")
      case _             => None

    def isMoarFun(fun: Term): Boolean =
      isMoar(fun) ||
        fun.symbol.owner.name == "moar" ||
        fun.symbol.owner.fullName.contains(".moar")

    def methodName(fun: Term): Option[String] = fun match
      case Select(_, name) => Some(name)
      case Ident(name)     => Some(name)
      case _               => None

    def selectStateMethod(state: Term, target: String, args: List[Term]): Term =
      val methods = TypeRepr.of[MutableState].typeSymbol.methodMember(target)
      val method = methods.find { sym =>
        val explicit = sym.paramSymss
          .takeWhile(ps => !ps.exists(p => p.flags.is(Flags.Implicit) || p.flags.is(Flags.Given)))
          .flatten
        explicit.size == args.size
      }.getOrElse {
        report.errorAndAbort(
          s"Cannot rewrite moar call to MutableState.$target with ${args.size} args"
        )
      }
      Select(state, method)

    def rewriteMoarApply(state: Term, fun: Term, args: List[Term], owner: Symbol, transform: Term => Term): Option[Term] =
      methodName(fun).flatMap(moarMethodName).flatMap { target =>
        if isMoarFun(fun) || fun.symbol.owner.name == "moar" || fun.symbol.owner.fullName.contains(".moar") then
          val transformedArgs = args.map(transform)
          Some(Apply(selectStateMethod(state, target, transformedArgs), transformedArgs))
        else None
      }

    def transformMoar(term: Term, state: Term): Term =
      val mapper = new TreeMap:
        override def transformTerm(tree: Term)(owner: Symbol): Term =
          def go(t: Term): Term = transformTerm(t)(owner)
          tree match
            // f(args)(implicits) — e.g. setCookie(...)(encoder)
            case Apply(inner @ Apply(fun, args), implicits)
                if methodName(fun).flatMap(moarMethodName).isDefined &&
                  (isMoarFun(fun) || fun.symbol.owner.name == "moar" || fun.symbol.owner.fullName.contains(".moar")) =>
              val transformedArgs = args.map(go)
              val call = Apply(selectStateMethod(state, moarMethodName(methodName(fun).get).get, transformedArgs), transformedArgs)
              Apply(call, implicits.map(go))
            case Apply(fun, args) =>
              rewriteMoarApply(state, fun, args, owner, go).getOrElse(super.transformTerm(tree)(owner))
            case _ =>
              super.transformTerm(tree)(owner)
      mapper.transformTerm(term)(Symbol.spliceOwner)

    def returnTypeOf(tpe: TypeRepr): TypeRepr = tpe match
      case AppliedType(_, args) if args.nonEmpty => args.last
      case other                                 => other

    def paramTypesOf(tpe: TypeRepr): List[TypeRepr] = tpe match
      case AppliedType(fn, args)
          if fn.typeSymbol.fullName.startsWith("scala.Function") && args.size > 1 =>
        args.init
      case AppliedType(fn, _) if fn.typeSymbol.fullName == "scala.Function0" =>
        Nil
      case _ =>
        unwrap(body.asTerm) match
          case Lambda(params, _) => params.map(_.tpt.tpe)
          case d: DefDef         => d.termParamss.flatMap(_.params).map(_.tpt.tpe)
          case _                 => Nil

    val fTpe = TypeRepr.of[F].widen.dealias
    val retTpe = returnTypeOf(fTpe)
    val paramTpes = paramTypesOf(fTpe)

    retTpe.asType match
      case '[r] =>
        val conv = Expr.summon[r => AsyncResult].getOrElse {
          report.errorAndAbort(
            "Cannot convert route result to AsyncResult. Import com.github.fntz.omhs.AsyncResult.Implicits._"
          )
        }

        def wrapWithState(applied: Term, stateRef: Expr[MutableState]): Expr[AsyncResult] =
          val rewritten = transformMoar(applied, stateRef.asTerm).asExprOf[r]
          '{ $stateRef.transform($conv($rewritten)) }

        val result: Expr[Any] = paramTpes match
          case Nil =>
            '{
              () => {
                val state = MutableState.empty($stateClass)
                ${
                  val applied = Apply(Select.unique(body.asTerm, "apply"), Nil)
                  wrapWithState(applied, 'state)
                }
              }
            }

          case List(t1) =>
            t1.asType match
              case '[a1] =>
                '{
                  (v1: a1) => {
                    val state = MutableState.empty($stateClass)
                    ${
                      val applied = Apply(Select.unique(body.asTerm, "apply"), List('{ v1 }.asTerm))
                      wrapWithState(applied, 'state)
                    }
                  }
                }

          case List(t1, t2) =>
            (t1.asType, t2.asType) match
              case ('[a1], '[a2]) =>
                '{
                  (v1: a1, v2: a2) => {
                    val state = MutableState.empty($stateClass)
                    ${
                      val applied = Apply(
                        Select.unique(body.asTerm, "apply"),
                        List('{ v1 }.asTerm, '{ v2 }.asTerm)
                      )
                      wrapWithState(applied, 'state)
                    }
                  }
                }

          case params =>
            // General arity: build FunctionN via Lambda
            val methodType = MethodType(params.indices.map(i => s"v$i").toList)(
              _ => params,
              _ => TypeRepr.of[AsyncResult]
            )
            val lambda = Lambda(Symbol.spliceOwner, methodType, { (owner, args) =>
              val stateSym = Symbol.newVal(
                owner,
                "state",
                TypeRepr.of[MutableState],
                Flags.EmptyFlags,
                Symbol.noSymbol
              )
              val stateVal = ValDef(
                stateSym,
                Some('{ MutableState.empty($stateClass) }.asTerm.changeOwner(stateSym))
              )
              val stateRef = Ref(stateSym)
              val applied = Apply(Select.unique(body.asTerm, "apply"), args.map(_.asInstanceOf[Term]))
              val rewritten = transformMoar(applied, stateRef)
              val transformCall = Apply(
                Select.unique(stateRef, "transform"),
                List('{ $conv(${ rewritten.asExprOf[r] }) }.asTerm)
              )
              Block(List(stateVal), transformCall).changeOwner(owner)
            })
            lambda.asExpr

        logger.verbose(result.asTerm.show)
        result
  }
}
