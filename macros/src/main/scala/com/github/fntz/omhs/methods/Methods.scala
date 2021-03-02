package com.github.fntz.omhs.methods

import scala.language.experimental.macros
import com.github.fntz.omhs.{BodyWriter, _}

import java.util.UUID
import scala.annotation.implicitNotFound
import scala.reflect.macros.whitebox


abstract class Req {}

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

    if (tokens.size != actualFunctionParameters.size) {
      c.error(focus, "Args lengths are not the same")
    }

    println(s"======> ${actualFunctionParameters}")

    tokens.zip(actualFunctionParameters).foreach { case (p, (fp, argName)) =>
      // check against arguments
      val at = getType(c, p)
      if (at.toString != fp.toString) {
        c.abort(focus, s"Incorrect type for `$argName`, " +
          s"required: ${at.typeSymbol.name}, given: ${fp}")
      }
//      TODO doesn't work with List[String]
//      if (!(fp.typeSymbol.asType.toType =:= at)) {
//        c.abort(focus, s"Incorrect type for `$argName`, " +
//              s"required: ${at.typeSymbol.name}, given: ${fp}")
//      }
    }

    // todo http method pass somehow

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
      }
    }

    val caseClause = ts.map(_._1)//.reduce((a, b) => q"$a::$b")
    val args = ts.map(_._2)//.map { x => tq"$x.value" }

    // skip body/headers/cookies for now
    val funName = TermName(c.freshName())
    val instance =
      q"""
        {
            def $funName() = {
              import Predef._
              val rule = new _root_.com.github.fntz.omhs.Rule(
                _root_.com.github.fntz.omhs.HttpMethod.GET
              )
              ${c.prefix.tree}.obj.xs.map { param =>
                rule.path(param)
              }

              val rf = new _root_.com.github.fntz.omhs.RuleAndF(rule) {
                override def run(defs: List[_root_.com.github.fntz.omhs.ParamDef[_]]): _root_.com.github.fntz.omhs.CommonResponse = {
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

        println(instance)

    c.Expr[RuleAndF](instance)
  }


  private def getType(c: whitebox.Context, p: ParamToken): c.universe.Type = {
    import c.universe._
    p match {
      case StringToken => typeTag[String].tpe
      case LongToken => typeTag[Long].tpe
      case UUIDToken => typeTag[UUID].tpe
      case RegexToken => typeTag[String].tpe
      case RestToken => typeTag[List[String]].tpe
    }
  }


}
