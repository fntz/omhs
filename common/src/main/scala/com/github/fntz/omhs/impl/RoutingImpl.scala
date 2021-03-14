package com.github.fntz.omhs.impl

import com.github.fntz.omhs._
import com.github.fntz.omhs.internal._
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.codec.http.multipart.FileUpload

import java.util.UUID
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

private[omhs] object RoutingImpl {
  import Shared._

  def run0[R: c.WeakTypeTag](c: whitebox.Context)
                           (f: c.Expr[() => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }

  def run1[T1: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                               (f: c.Expr[(T1) => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }


  def run2[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                        (f: c.Expr[(T1, T2) => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }


  def run3[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
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


  def run10[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag,
    T7: c.WeakTypeTag,
    T8: c.WeakTypeTag,
    T9: c.WeakTypeTag,
    T10: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                         (f: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }


  def run11[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag,
    T7: c.WeakTypeTag,
    T8: c.WeakTypeTag,
    T9: c.WeakTypeTag,
    T10: c.WeakTypeTag,
    T11: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                         (f: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }


  def run12[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag,
    T7: c.WeakTypeTag,
    T8: c.WeakTypeTag,
    T9: c.WeakTypeTag,
    T10: c.WeakTypeTag,
    T11: c.WeakTypeTag,
    T12: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                         (f: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }


  def run13[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag,
    T7: c.WeakTypeTag,
    T8: c.WeakTypeTag,
    T9: c.WeakTypeTag,
    T10: c.WeakTypeTag,
    T11: c.WeakTypeTag,
    T12: c.WeakTypeTag,
    T13: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                         (f: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }


  def run14[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag,
    T7: c.WeakTypeTag,
    T8: c.WeakTypeTag,
    T9: c.WeakTypeTag,
    T10: c.WeakTypeTag,
    T11: c.WeakTypeTag,
    T12: c.WeakTypeTag,
    T13: c.WeakTypeTag,
    T14: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                         (f: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }


  def run15[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag,
    T7: c.WeakTypeTag,
    T8: c.WeakTypeTag,
    T9: c.WeakTypeTag,
    T10: c.WeakTypeTag,
    T11: c.WeakTypeTag,
    T12: c.WeakTypeTag,
    T13: c.WeakTypeTag,
    T14: c.WeakTypeTag,
    T15: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                         (f: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }


  def run16[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag,
    T7: c.WeakTypeTag,
    T8: c.WeakTypeTag,
    T9: c.WeakTypeTag,
    T10: c.WeakTypeTag,
    T11: c.WeakTypeTag,
    T12: c.WeakTypeTag,
    T13: c.WeakTypeTag,
    T14: c.WeakTypeTag,
    T15: c.WeakTypeTag,
    T16: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                         (f: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }


  def run17[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag,
    T7: c.WeakTypeTag,
    T8: c.WeakTypeTag,
    T9: c.WeakTypeTag,
    T10: c.WeakTypeTag,
    T11: c.WeakTypeTag,
    T12: c.WeakTypeTag,
    T13: c.WeakTypeTag,
    T14: c.WeakTypeTag,
    T15: c.WeakTypeTag,
    T16: c.WeakTypeTag,
    T17: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                         (f: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }


  def run18[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag,
    T7: c.WeakTypeTag,
    T8: c.WeakTypeTag,
    T9: c.WeakTypeTag,
    T10: c.WeakTypeTag,
    T11: c.WeakTypeTag,
    T12: c.WeakTypeTag,
    T13: c.WeakTypeTag,
    T14: c.WeakTypeTag,
    T15: c.WeakTypeTag,
    T16: c.WeakTypeTag,
    T17: c.WeakTypeTag,
    T18: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                         (f: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18) => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }


  def run19[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag,
    T7: c.WeakTypeTag,
    T8: c.WeakTypeTag,
    T9: c.WeakTypeTag,
    T10: c.WeakTypeTag,
    T11: c.WeakTypeTag,
    T12: c.WeakTypeTag,
    T13: c.WeakTypeTag,
    T14: c.WeakTypeTag,
    T15: c.WeakTypeTag,
    T16: c.WeakTypeTag,
    T17: c.WeakTypeTag,
    T18: c.WeakTypeTag,
    T19: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                         (f: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }


  def run20[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag,
    T7: c.WeakTypeTag,
    T8: c.WeakTypeTag,
    T9: c.WeakTypeTag,
    T10: c.WeakTypeTag,
    T11: c.WeakTypeTag,
    T12: c.WeakTypeTag,
    T13: c.WeakTypeTag,
    T14: c.WeakTypeTag,
    T15: c.WeakTypeTag,
    T16: c.WeakTypeTag,
    T17: c.WeakTypeTag,
    T18: c.WeakTypeTag,
    T19: c.WeakTypeTag,
    T20: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                         (f: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }


  def run21[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag,
    T7: c.WeakTypeTag,
    T8: c.WeakTypeTag,
    T9: c.WeakTypeTag,
    T10: c.WeakTypeTag,
    T11: c.WeakTypeTag,
    T12: c.WeakTypeTag,
    T13: c.WeakTypeTag,
    T14: c.WeakTypeTag,
    T15: c.WeakTypeTag,
    T16: c.WeakTypeTag,
    T17: c.WeakTypeTag,
    T18: c.WeakTypeTag,
    T19: c.WeakTypeTag,
    T20: c.WeakTypeTag,
    T21: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                         (f: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21) => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }


  def run22[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag,
    T7: c.WeakTypeTag,
    T8: c.WeakTypeTag,
    T9: c.WeakTypeTag,
    T10: c.WeakTypeTag,
    T11: c.WeakTypeTag,
    T12: c.WeakTypeTag,
    T13: c.WeakTypeTag,
    T14: c.WeakTypeTag,
    T15: c.WeakTypeTag,
    T16: c.WeakTypeTag,
    T17: c.WeakTypeTag,
    T18: c.WeakTypeTag,
    T19: c.WeakTypeTag,
    T20: c.WeakTypeTag,
    T21: c.WeakTypeTag,
    T22: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                         (f: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22) => R]): c.Expr[ExecutableRule] = {
    generate(c)(f.tree)
  }


  private def generate(c: whitebox.Context)(f: c.Tree): c.Expr[ExecutableRule] = {
    import c.universe._

    val logger = new Logger(c)

    val focus = c.enclosingPosition.focus

    guardSbtOptions(c)

    sealed trait ParamToken {
      def isBody = false
      def isFile = false
      def isQuery = false
    }
    case object StringToken extends ParamToken
    case object LongToken extends ParamToken
    case object UUIDToken extends ParamToken
    case object RegexToken extends ParamToken
    case object AlternativeToken extends ParamToken
    case object TailToken extends ParamToken
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
        case TailToken => c.typeTag[List[String]].tpe
        case AlternativeToken => c.typeTag[String].tpe
        case BodyToken(tpt, _) => tpt.asInstanceOf[c.universe.Type]
        case HeaderToken => c.typeTag[String].tpe
        case CurrentRequestToken => c.typeTag[CurrentHttpRequest].tpe
        case FileToken => c.typeTag[List[FileUpload]].tpe
        case CookieToken => c.typeTag[Cookie].tpe
        case QueryToken(tpt, _) => tpt.asInstanceOf[c.universe.Type]
      }
    }

    val availableParams = Map(
      "string" -> StringToken,
      "long" -> LongToken,
      "uuid" -> UUIDToken,
      "regex" -> RegexToken,
      "header" -> HeaderToken,
      "cookie" -> CookieToken,
      "file" -> FileToken,
      "$times" -> TailToken
    )

    val complex: Vector[String] = Vector("body", "query")

    // because helper methods (query, body, long) in the same package as implicits
    val banned: Vector[String] =
      Vector(
        "StringExt",
        "PathParamExt",
        "ExecutableRuleExtensions",
        "post", "get", "head", "put", "patch", "delete"
      )

    val ignored = banned ++ complex

    val tokens = c.prefix.tree.collect {
      case Select(
        Apply(Select(Select(Select(_, TermName("omhs")), TermName("RoutingDSL")),
        TermName("StringExt")),
          List(_)), TermName("$bar")) =>
        AlternativeToken

      case Apply(
        TypeApply(Select(Select(Select(_, TermName("omhs")), TermName("RoutingDSL")),
          TermName("body")), List(tpt)), List(reader)) =>
        BodyToken(tpt.tpe, reader)

      case Apply(
        TypeApply(Select(Select(Select(_, TermName("omhs")), TermName("RoutingDSL")),
        TermName("query")), List(tpt)), List(reader)) =>
          QueryToken(tpt.tpe, reader)

      case Select(Select(Select(_, TermName("omhs")),
        TermName("RoutingDSL")), TermName(term)) if !ignored.contains(term) =>
        availableParams.get(term) match {
          case Some(value) => value
          case None =>
           c.abort(focus, s"Unexpected term: $term, available: ${availableParams.keys.mkString(", ")}")
        }
    }.toBuffer[ParamToken]

    logger.info(s"Found ${tokens.size} arguments for rule")

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

    val isEmptyFunction = actualFunctionParameters.isEmpty

    val reqType = typeOf[com.github.fntz.omhs.CurrentHttpRequest]
    def isReqParam(x: c.Type): Boolean = x.typeSymbol.asType.toType =:= reqType
    val isReqParamNeeded = actualFunctionParameters.exists(x => isReqParam(x._1))

    logger.info(
      s"""
         |isEmptyFunction? $isEmptyFunction
         |need to pass requestParam? $isReqParamNeeded
         |detected tokens: ${tokens.mkString(", ")}
         |actual parameters: ${actualFunctionParameters.map(x => s"${x._2}: ${x._1}").mkString(", ")}
         |
         |""".stripMargin
    )

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
          (pq"_root_.com.github.fntz.omhs.internal.StringDef($valName)", valName, ParamDef.string)
        case LongToken =>
          (pq"_root_.com.github.fntz.omhs.internal.LongDef($valName : Long)", valName, ParamDef.long)
        case RegexToken =>
          (pq"_root_.com.github.fntz.omhs.internal.RegexDef($valName)", valName, ParamDef.regex)
        case UUIDToken =>
          (pq"_root_.com.github.fntz.omhs.internal.UUIDDef($valName)", valName, ParamDef.uuid)
        case TailToken =>
          (pq"_root_.com.github.fntz.omhs.internal.TailDef($valName)", valName, ParamDef.tail)
        case AlternativeToken =>
          (pq"_root_.com.github.fntz.omhs.internal.AlternativeDef($valName)", valName, ParamDef.alternative)
        case BodyToken(tpt, _) =>
          (pq"_root_.com.github.fntz.omhs.internal.BodyDef($valName: ${tpt.typeSymbol})", valName, ParamDef.body)
        case HeaderToken =>
          (pq"_root_.com.github.fntz.omhs.internal.HeaderDef($valName)", valName, ParamDef.header)
        case CurrentRequestToken =>
          (pq"_root_.com.github.fntz.omhs.internal.CurrentHttpRequestDef($valName)", valName, ParamDef.request)
        case FileToken =>
          (pq"_root_.com.github.fntz.omhs.internal.FileDef($valName)", valName, ParamDef.file)
        case CookieToken =>
          (pq"_root_.com.github.fntz.omhs.internal.CookieDef($valName)", valName, ParamDef.cookie)
        case QueryToken(tpt, _) =>
          (pq"_root_.com.github.fntz.omhs.internal.QueryDef($valName: ${tpt.typeSymbol})", valName, ParamDef.query)
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

    // note:
    // orig: string << header <<< body
    // defs: List(string, body, header) <- after apply to rule (@see RequestHelper)
    // cause: header, string, body
    // also cookie and header has the same weight and it doesn't possible to define good order ?
    // maybe I need to rewrite sorting in more effective way
    val instance =
      q"""
        {
            def $funName() = {
              val rule = ${c.prefix.tree}.rule

              if ($isReqParamNeeded) {
                rule.withRequest()
              }

              val rf = new _root_.com.github.fntz.omhs.internal.ExecutableRule(rule) {
                override def run(defs: List[_root_.com.github.fntz.omhs.internal.ParamDef[_]]): _root_.com.github.fntz.omhs.AsyncResult = {
                  val defsMap = defs.groupBy(_.sortProp).map { x =>
                    x._1 -> x._2.toBuffer[_root_.com.github.fntz.omhs.internal.ParamDef[_]]
                  }
                  val sorted = $defsPositions.map { x => defsMap(x).remove(0) }

                  defs match {
                    case $caseClause =>
                      $callFunction
                    case _ =>
                      println("======TODO==============")
                      _root_.com.github.fntz.omhs.AsyncResult.completed(
                        _root_.com.github.fntz.omhs.CommonResponse.empty
                      )
                  }
                }
              }
              rf
            }
            $funName()
        }
        """

    logger.verbose(s"$instance")

    c.Expr[ExecutableRule](instance)
  }


}
