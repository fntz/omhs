package com.github.fntz.omhs.methods

import scala.language.experimental.macros
import com.github.fntz.omhs.{BodyWriter, _}

import java.util.UUID
import scala.reflect.api.Trees
import scala.reflect.macros.whitebox

object Methods {

  implicit class PExt(val obj: p) extends AnyVal {
    implicit def ~>[R](f: () => R): RuleAndF =
    macro MethodsImpl.run0[R]
    implicit def ~>[T, R](f: T => R): RuleAndF =
      macro MethodsImpl.run1[T, R]
    implicit def ~>[T1, T2, R](f: (T1, T2) => R): RuleAndF =
      macro MethodsImpl.run2[T1, T2, R]
    implicit def ~>[T1, T2, T3, R](f: (T1, T2, T3) => R): RuleAndF =
      macro MethodsImpl.run3[T1, T2, T3, R]
    implicit def ~>[T1, T2, T3, T4, R](f: (T1, T2, T3, T4) => R): RuleAndF =
      macro MethodsImpl.run4[T1, T2, T3, T4,  R]
    implicit def ~>[T1, T2, T3, T4, T5, R](f: (T1, T2, T3, T4, T5) => R): RuleAndF =
      macro MethodsImpl.run5[T1, T2, T3, T4, T5, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, R](f: (T1, T2, T3, T4, T5, T6) => R): RuleAndF =
      macro MethodsImpl.run6[T1, T2, T3, T4, T5, T6, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, T7, R](f: (T1, T2, T3, T4, T5, T6, T7) => R): RuleAndF =
      macro MethodsImpl.run7[T1, T2, T3, T4, T5, T6, T7, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, T7, T8, R](f: (T1, T2, T3, T4, T5, T6, T7, T8) => R): RuleAndF =
      macro MethodsImpl.run8[T1, T2, T3, T4, T5, T6, T7, T8, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9) => R): RuleAndF =
      macro MethodsImpl.run9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R]
  }


}


object MethodsImpl {

  def run0[R: c.WeakTypeTag](c: whitebox.Context)
                           (f: c.Expr[() => R]): c.Expr[RuleAndF] = {
    generate(c)(f.tree)
  }

  def run1[T: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                              (f: c.Expr[T => R]): c.Expr[RuleAndF] = {
    generate(c)(f.tree)
  }

  def run2[T1: c.WeakTypeTag, T2: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                              (f: c.Expr[(T1, T2) => R]): c.Expr[RuleAndF] = {
    generate(c)(f.tree)
  }

  def run3[T1: c.WeakTypeTag, T2: c.WeakTypeTag, T3: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                                                  (f: c.Expr[(T1, T2, T3) => R]): c.Expr[RuleAndF] = {
    generate(c)(f.tree)
  }

  def run4[T1: c.WeakTypeTag,
           T2: c.WeakTypeTag,
           T3: c.WeakTypeTag,
           T4: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                  (f: c.Expr[(T1, T2, T3, T4) => R]): c.Expr[RuleAndF] = {
    generate(c)(f.tree)
  }

  def run5[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                        (f: c.Expr[(T1, T2, T3, T4, T5) => R]): c.Expr[RuleAndF] = {
    generate(c)(f.tree)
  }

  def run6[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                        (f: c.Expr[(T1, T2, T3, T4, T5, T6) => R]): c.Expr[RuleAndF] = {
    generate(c)(f.tree)
  }

  def run7[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag,
    T7: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                        (f: c.Expr[(T1, T2, T3, T4, T5, T6, T7) => R]): c.Expr[RuleAndF] = {
    generate(c)(f.tree)
  }

  def run8[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag,
    T7: c.WeakTypeTag,
    T8: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                        (f: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8) => R]): c.Expr[RuleAndF] = {
    generate(c)(f.tree)
  }

  def run9[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag,
    T7: c.WeakTypeTag,
    T8: c.WeakTypeTag,
    T9: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                        (f: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9) => R]): c.Expr[RuleAndF] = {
    generate(c)(f.tree)
  }


  private def generate(c: whitebox.Context)(f: c.Tree): c.Expr[RuleAndF] = {
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
    case object CurrentRequestToken extends ParamToken

    def getType(c: whitebox.Context, p: ParamToken): c.universe.Type = {
      p match {
        case StringToken => c.typeTag[String].tpe
        case LongToken => c.typeTag[Long].tpe
        case UUIDToken => c.typeTag[UUID].tpe
        case RegexToken => c.typeTag[String].tpe
        case RestToken => c.typeTag[List[String]].tpe
        case BodyToken(tpt, _) => tpt.asInstanceOf[c.universe.Type]
        case HeaderToken => c.typeTag[String].tpe
        case CurrentRequestToken => c.typeTag[CurrentHttpRequest].tpe
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
    }.to[scala.collection.mutable.ArrayBuffer]

    if (tokens.count(_.isBody) > 1) {
      val where = tokens.collect {
        case BodyToken(tpe, _) => tpe.resultType.toString
      }
      c.abort(focus, s"Too many BodyParam(${where.mkString(", ")}) arguments, but expected one")
    }

    val actualFunctionParameters = f match {
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

    val reqType = typeOf[com.github.fntz.omhs.CurrentHttpRequest]
    def isReqParam(x: c.Type): Boolean = x.typeSymbol.asType.toType =:= reqType
    val isReqParamNeeded = actualFunctionParameters.exists(x => isReqParam(x._1))

    println(s"need to pass requestParam? $isReqParamNeeded")

    if (!(isReqParamNeeded && isReqParam(actualFunctionParameters.last._1))) {
      c.error(focus, s"${reqType} must the last argument in the function")
    }

    if (isReqParamNeeded) {
      tokens += CurrentRequestToken
    }

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
        case CurrentRequestToken =>
          (pq"_root_.com.github.fntz.omhs.CurrentHttpRequestDef($n)", n)
      }
    }

    val caseClause = q"List(..${ts.map(_._1)})"
    val args = ts.map(_._2)

    // skip cookies for now
    val funName = TermName(c.freshName())
    // todo we should sort defs by tokens:
    // (header / long) => {str, lng => }
    // but currently I will pass defs in lng/header positions => runtime error
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

              if ($isReqParamNeeded) {
                rule.withRequest()
              }

              val rf = new _root_.com.github.fntz.omhs.RuleAndF(rule) {
                override def run(defs: List[_root_.com.github.fntz.omhs.ParamDef[_]]): _root_.com.github.fntz.omhs.AsyncResult = {
                  println(defs)
                  defs match {
                    case $caseClause =>
                      $f(..$args)
                    case _ =>
                      println("======TODO==============")
                      _root_.com.github.fntz.omhs.AsyncResult.completed(
                        com.github.fntz.omhs.CommonResponse.empty
                      )
                  }
                }
              }
              rf
            }
            $funName()
        }
        """

    println(instance)

    c.Expr[RuleAndF](instance)
  }


}
