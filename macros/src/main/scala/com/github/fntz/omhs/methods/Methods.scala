package com.github.fntz.omhs.methods

import scala.language.experimental.macros
import com.github.fntz.omhs._
import io.netty.handler.codec.http.multipart.MixedFileUpload
import io.netty.handler.codec.http.cookie.Cookie

import java.util.UUID
import scala.reflect.macros.whitebox

object Methods {

  implicit class PExt(val obj: p) extends AnyVal {
    implicit def ~>[R](f: () => R): ExecutableRule =
    macro MethodsImpl.run0[R]
    implicit def ~>[T, R](f: T => R): ExecutableRule =
      macro MethodsImpl.run1[T, R]
    implicit def ~>[T1, T2, R](f: (T1, T2) => R): ExecutableRule =
      macro MethodsImpl.run2[T1, T2, R]
    implicit def ~>[T1, T2, T3, R](f: (T1, T2, T3) => R): ExecutableRule =
      macro MethodsImpl.run3[T1, T2, T3, R]
    implicit def ~>[T1, T2, T3, T4, R](f: (T1, T2, T3, T4) => R): ExecutableRule =
      macro MethodsImpl.run4[T1, T2, T3, T4,  R]
    implicit def ~>[T1, T2, T3, T4, T5, R](f: (T1, T2, T3, T4, T5) => R): ExecutableRule =
      macro MethodsImpl.run5[T1, T2, T3, T4, T5, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, R](f: (T1, T2, T3, T4, T5, T6) => R): ExecutableRule =
      macro MethodsImpl.run6[T1, T2, T3, T4, T5, T6, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, T7, R](f: (T1, T2, T3, T4, T5, T6, T7) => R): ExecutableRule =
      macro MethodsImpl.run7[T1, T2, T3, T4, T5, T6, T7, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, T7, T8, R](f: (T1, T2, T3, T4, T5, T6, T7, T8) => R): ExecutableRule =
      macro MethodsImpl.run8[T1, T2, T3, T4, T5, T6, T7, T8, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9) => R): ExecutableRule =
      macro MethodsImpl.run9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R]
  }


}


object MethodsImpl {

  def run0[R: c.WeakTypeTag](c: whitebox.Context)
                           (f: c.Expr[() => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }

  def run1[T: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                              (f: c.Expr[T => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }

  def run2[T1: c.WeakTypeTag, T2: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                              (f: c.Expr[(T1, T2) => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }

  def run3[T1: c.WeakTypeTag, T2: c.WeakTypeTag, T3: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                                                  (f: c.Expr[(T1, T2, T3) => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }

  def run4[T1: c.WeakTypeTag,
           T2: c.WeakTypeTag,
           T3: c.WeakTypeTag,
           T4: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                  (f: c.Expr[(T1, T2, T3, T4) => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }

  def run5[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                        (f: c.Expr[(T1, T2, T3, T4, T5) => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }

  def run6[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                        (f: c.Expr[(T1, T2, T3, T4, T5, T6) => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }

  def run7[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag,
    T7: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                        (f: c.Expr[(T1, T2, T3, T4, T5, T6, T7) => R]): c.Expr[ExecutableRule] = {
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
                                        (f: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8) => R]): c.Expr[ExecutableRule] = {
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
                                        (f: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9) => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }


  private def generate(c: whitebox.Context)(f: c.Tree): c.Expr[ExecutableRule] = {
    import c.universe._
    val focus = c.enclosingPosition.focus

    // TODO make sharable
    sealed trait ParamToken {
      def isBody = false
      def isFile = false
      def isQuery = false
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
    case object FileToken extends ParamToken {
      override def isFile = true
    }
    case object CookieToken extends ParamToken
    case class QueryToken(tpe: c.universe.Type, reader: c.universe.Tree) extends ParamToken {
      override def isQuery: Boolean = true
    }

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
        case FileToken => c.typeTag[List[MixedFileUpload]].tpe
        case CookieToken => c.typeTag[Cookie].tpe
        case QueryToken(tpt, _) => tpt.asInstanceOf[c.universe.Type]
      }
    }

    val paramDMap = Map(
      "string" -> StringToken,
      "long" -> LongToken,
      "uuid" -> UUIDToken,
      "regex" -> RegexToken,
      "header" -> HeaderToken,
      "cookie" -> CookieToken,
      "file" -> FileToken
    )

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
      case q"com.github.fntz.omhs.CookieParam" =>
        CookieToken

      case  Apply(TypeApply(
        Select(Select(Select(_, TermName("omhs")), TermName("ParamD")),
        TermName("query")), List(tpt)), List(reader)) =>
          QueryToken(tpt.tpe, reader)

      case Select(Select(Select(_, TermName("omhs")), TermName("ParamD")), TermName(term)) if term != "query" =>
        paramDMap.get(term) match {
          case Some(value) => value
          case None =>
           c.abort(focus, s"Unexpected term: $term, available: ${paramDMap.keys.mkString(", ")}")
        }

      case q"com.github.fntz.omhs.FileParam" =>
        FileToken
      case Apply(Apply(TypeApply(
        Select(Select(_, TermName("BodyParam")), TermName("apply")), List(tpt)), _), List(reader)) =>
        BodyToken(tpt.tpe, reader)

    }.to[scala.collection.mutable.ArrayBuffer]

    println(s"=============> ${tokens.size}")

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

    // or currentrequest todo
    val isEmptyFunction = actualFunctionParameters.isEmpty

    println(s"isEmptyFunction: $isEmptyFunction")

    println(s"actual parameters: ${actualFunctionParameters.map(x => s"${x._2}: ${x._1}").mkString(", ")}")

    val reqType = typeOf[com.github.fntz.omhs.CurrentHttpRequest]
    def isReqParam(x: c.Type): Boolean = x.typeSymbol.asType.toType =:= reqType
    val isReqParamNeeded = actualFunctionParameters.exists(x => isReqParam(x._1))

    println(s"need to pass requestParam? $isReqParamNeeded")

    if (isReqParamNeeded) {
      if (!isReqParam(actualFunctionParameters.last._1)) {
        c.error(focus, s"$reqType must be the last argument in the function")
      } else {
        tokens += CurrentRequestToken
      }
    }

    if (tokens.size != actualFunctionParameters.size && !isEmptyFunction) {
      c.error(focus, "Args lengths are not the same")
    }

    // otherwise just ignore all parameters
    if (!isEmptyFunction) {
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
    }

    val bodyCount = tokens.count(x => x.isBody)
    if (bodyCount > 1) {
      c.abort(focus, s"BodyParam must be one per rule, given: $bodyCount")
    }
    val fileCount = tokens.count(x => x.isFile)
    if (fileCount > 1) {
      c.abort(focus, s"FileParam must be one per rule, given: $fileCount")
    }

    if (fileCount == 1 && bodyCount == 1) {
      c.abort(focus, s"You can not mix BodyParam with FileParam, choose one")
    }

    val ts = tokens.map { token  =>
      val valName = TermName(c.freshName())
      token match {
        case StringToken =>
          (pq"_root_.com.github.fntz.omhs.StringDef($valName)", valName, StringDef.sortProp)
        case LongToken =>
          (pq"_root_.com.github.fntz.omhs.LongDef($valName : Long)", valName, LongDef.sortProp)
        case RegexToken =>
          (pq"_root_.com.github.fntz.omhs.RegexDef($valName)", valName, RegexDef.sortProp)
        case UUIDToken =>
          (pq"_root_.com.github.fntz.omhs.UUIDDef($valName)", valName, UUIDDef.sortProp)
        case RestToken =>
          (pq"_root_.com.github.fntz.omhs.TailDef($valName)", valName, TailDef.sortProp)
        case BodyToken(tpt, _) =>
          (pq"_root_.com.github.fntz.omhs.BodyDef($valName: ${tpt.typeSymbol})", valName, BodyDef.sortProp)
        case HeaderToken =>
          (pq"_root_.com.github.fntz.omhs.HeaderDef($valName)", valName, HeaderDef.sortProp)
        case CurrentRequestToken =>
          (pq"_root_.com.github.fntz.omhs.CurrentHttpRequestDef($valName)", valName, CurrentHttpRequestDef.sortProp)
        case FileToken =>
          (pq"_root_.com.github.fntz.omhs.FileDef($valName)", valName, FileDef.sortProp)
        case CookieToken =>
          (pq"_root_.com.github.fntz.omhs.CookieDef($valName)", valName, CookieDef.sortProp)
        case QueryToken(tpt, _) =>
          (pq"_root_.com.github.fntz.omhs.QueryDef($valName: ${tpt.typeSymbol})", valName, QueryDef.sortProp)
      }
    }

    val caseClause = q"List(..${ts.map(_._1)})"
    val args = ts.map(_._2)
    val defsPositions = ts.map(_._3).toList

    // skip cookies for now
    val funName = TermName(c.freshName())

    // generate according to parameters or ignore them
    val callFunction = if (isEmptyFunction) {
      q"$f()"
    } else {
      q"$f(..$args)"
    }

    // orig: header / string / long
    // defs: List(long, string, header) <- after apply to rule
    // cause: header, string, long
    // maybe I need to rewrite sorting in more effective way
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
                case q: _root_.com.github.fntz.omhs.QueryParam[_] =>
                  rule.query()(q.reader)
                case h: _root_.com.github.fntz.omhs.HeaderParam =>
                  rule.header(h)
                case h: _root_.com.github.fntz.omhs.CookieParam =>
                  rule.cookie(h)
                case f: _root_.com.github.fntz.omhs.FileParam =>
                  rule.withFiles(f)
                case param: _root_.com.github.fntz.omhs.PathParam =>
                  rule.path(param)
              }

              if ($isReqParamNeeded) {
                rule.withRequest()
              }

              val rf = new _root_.com.github.fntz.omhs.ExecutableRule(rule) {
                override def run(defs: List[_root_.com.github.fntz.omhs.ParamDef[_]]): _root_.com.github.fntz.omhs.AsyncResult = {
                  println(defs)
                  val defsMap = defs.groupBy(_.sortProp).map { x =>
                    x._1 -> x._2.to[scala.collection.mutable.ArrayBuffer]
                  }
                  val sorted = $defsPositions.map { x => defsMap(x).remove(0) }

                  sorted match {
                    case $caseClause =>
                      $callFunction
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

    c.Expr[ExecutableRule](instance)
  }


}
