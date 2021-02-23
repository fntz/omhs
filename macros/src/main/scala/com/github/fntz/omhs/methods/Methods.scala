package com.github.fntz.omhs.methods

import scala.language.experimental.macros
import com.github.fntz.omhs.{HardCodedParam, LongParam, Param, StringParam, p}

import scala.reflect.macros.whitebox


abstract class Req {}

/*
  case class Req()
  val r = get("test" / LongParam) { req =>  }
 */


object Methods {

  implicit class PExt(val x: p) extends AnyVal {

    implicit def ~>[T](f: T => Unit): Unit = macro MethodsImpl.run1[T]
    implicit def ~>[T1, T2](f: (T1, T2) => Unit): Unit = macro MethodsImpl.run2[T1, T2]
  }


}

object MethodsImpl {

  def run1[T: c.WeakTypeTag](c: whitebox.Context)
                            (f: c.Expr[T => Unit]): c.Expr[Unit] = {
    import c.universe._
    val focus = c.enclosingPosition.focus
    val params = c.eval(c.Expr(c.untypecheck(c.prefix.tree)))
      .asInstanceOf[Methods.PExt].x.xs.filterNot(_.isUserDefined)

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

    if (params.size != actualFunctionParameters.size) {
      c.error(focus, "Args lengths are not the same")
    }

    params.zip(actualFunctionParameters).foreach { case (p, (fp, argName)) =>
      // check against arguments // TODO
      fp match {
        case TypeRef(_, x, _) if x.asType.asClass != p.actualType.getClass =>
          c.abort(focus, s"Incorrect type for `$argName`, " +
            s"required: ${p.actualType.getName}, given: ${fp}")
      }
    }

    // parseValues

    // build netty request 


    c.Expr[Unit](q"()")
  }



  def run2[T1: c.WeakTypeTag, T2: c.WeakTypeTag](c: whitebox.Context)
                                                (f: c.Expr[(T1, T2) => Unit]): c.Expr[Unit] = {
    import c.universe._

    c.Expr[Unit](q"()")
  }


}
