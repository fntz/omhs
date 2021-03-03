package com.github.fntz.omhs.methods

import scala.language.experimental.macros
import com.github.fntz.omhs.{BodyWriter, _}

import java.util.UUID
import scala.reflect.macros.whitebox

object Methods {

  implicit class PExt(val obj: p) extends AnyVal {
    implicit def ~>[T, R](f: T => R)(implicit w: BodyWriter[R]): RuleAndF = macro MethodsImpl.run1[T, R]
  }


}

object MethodsImpl {
  // val x = "test"
  // get(x / LongParam) => fail
  //    val params = c.eval(c.Expr(c.untypecheck(c.prefix.tree)))
  //      .asInstanceOf[Methods.PExt].x.xs.filterNot(_.isUserDefined)
  def run1[T: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                            (f: c.Expr[T => R])
                            (w: c.Expr[BodyWriter[R]]): c.Expr[RuleAndF] = {
    import c.universe._
    val focus = c.enclosingPosition.focus

    // TODO make sharable
    sealed trait ParamToken {
      def isBody = false
    }
    case object StringToken extends ParamToken
    case object LongToken extends ParamToken
    case object UUIDToken extends ParamToken
    case object RegexToken extends ParamToken
    case object RestToken extends ParamToken
    case class BodyToken(tpe: c.universe.Type, reader: c.universe.Tree)
      extends ParamToken {
      override def isBody: Boolean = true
    }
    case object HeaderToken extends ParamToken

    def getType(c: whitebox.Context, p: ParamToken): c.universe.Type = {
      import c.universe._
      p match {
        case StringToken => c.typeTag[String].tpe
        case LongToken => c.typeTag[Long].tpe
        case UUIDToken => c.typeTag[UUID].tpe
        case RegexToken => c.typeTag[String].tpe
        case RestToken => c.typeTag[List[String]].tpe
        case BodyToken(tpt, _) => tpt.asInstanceOf[c.universe.Type]
        case HeaderToken => c.typeTag[String].tpe
      }
    }


    val tokens = c.prefix.tree.collect {
      case q"com.github.fntz.omhs.UUIDParam" =>
        UUIDToken
      case q"com.github.fntz.omhs.StringParam" =>
        StringToken
      case q"com.github.fntz.omhs.LongParam" =>
        LongToken
      case q"com.github.fntz.omhs.RegexParam" =>
        RegexToken
      case q"com.github.fntz.omhs.*" =>
        RestToken
      case q"com.github.fntz.omhs.HeaderParam" =>
        HeaderToken
      case Apply(Apply(TypeApply(
          Select(Select(_, TermName("BodyParam")), TermName("apply")), List(tpt)), _), List(reader)) =>
        BodyToken(tpt.tpe, reader)
    }

    if (tokens.count(_.isBody) > 1) {
      val where = tokens.collect {
        case BodyToken(tpe, _) => tpe.resultType.toString
      }
      c.abort(focus, s"Too many BodyParam(${where.mkString(", ")}) arguments, but expected one")
    }

    val actualFunctionParameters = f.tree match {
      case q"(..$params) => $_" =>
        params.collect {
          case q"$_ val $tname: $tpt = $_" =>
            (tpt.tpe, tname.toString())
        }

      case _ =>
        c.error(focus, "Function is required")
        Seq.empty
    }
    println(s"actual parameters: $actualFunctionParameters")

    if (tokens.size != actualFunctionParameters.size) {
      c.error(focus, "Args lengths are not the same")
    }

    tokens.zip(actualFunctionParameters).foreach { case (paramToken, (funcTypeParam, argName)) =>
      // check against arguments
      val at = getType(c, paramToken)
      if (at.toString != funcTypeParam.toString) {
        c.abort(focus, s"Incorrect type for `$argName`, " +
          s"required: ${at.typeSymbol.name}, given: ${funcTypeParam}")
      }
//      TODO doesn't work with List[String]
//      if (!(fp.typeSymbol.asType.toType =:= at)) {
//        c.abort(focus, s"Incorrect type for `$argName`, " +
//              s"required: ${at.typeSymbol.name}, given: ${fp}")
//      }
    }

    // todo check on empty

    // todo with carrying
    // val fresh = f _
    // fresh(n)(n)(n) ???
    //
    val ts = tokens.map { t =>
      val n = TermName(c.freshName())
      t match {
        case StringToken =>
          (pq"_root_.com.github.fntz.omhs.StringDef($n)", n)
        case LongToken =>
          (pq"_root_.com.github.fntz.omhs.LongDef($n : Long)", n)
        case RegexToken =>
          (pq"_root_.com.github.fntz.omhs.RegexDef($n)", n)
        case UUIDToken =>
          (pq"_root_.com.github.fntz.omhs.UUIDDef($n)", n)
        case RestToken =>
          (pq"_root_.com.github.fntz.omhs.TailDef($n)", n)
        case BodyToken(tpt, _) =>
          (pq"_root_.com.github.fntz.omhs.BodyDef($n: ${tpt.typeSymbol})", n)
        case HeaderToken =>
          (pq"_root_.com.github.fntz.omhs.HeaderDef($n)", n)
      }
    }

    val caseClause = ts.map(_._1)
    val args = ts.map(_._2)

    // skip cookies for now
    val funName = TermName(c.freshName())
    val instance =
      q"""
        {
            def $funName() = {
              val rule = new _root_.com.github.fntz.omhs.Rule(
                ${c.prefix.tree}.obj.method
              )
              ${c.prefix.tree}.obj.xs.map {
                case b: _root_.com.github.fntz.omhs.BodyParam[_] =>
                  rule.body()(b.reader)
                case _root_.com.github.fntz.omhs.HeaderParam(value) =>
                  rule.header(value)
                case param: _root_.com.github.fntz.omhs.PathParam =>
                  rule.path(param)
              }

              val rf = new _root_.com.github.fntz.omhs.RuleAndF(rule) {
                override def run(defs: List[_root_.com.github.fntz.omhs.ParamDef[_]]): _root_.com.github.fntz.omhs.Response = {
                  println(defs)
                  defs match {
                    case ..$caseClause :: Nil =>
                      implicitly[BodyWriter[${c.weakTypeOf[R]}]].write($f(...$args))
                    case _ =>
                      println("======TODO==============")
                      com.github.fntz.omhs.CommonResponse.empty
                  }
                }
              }
              rf
            }
            $funName()
        }
        """

    c.Expr[RuleAndF](instance)
  }





}
