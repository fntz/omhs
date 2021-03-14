package com.github.fntz.omhs.impl

import com.github.fntz.omhs.AsyncResult

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

private[omhs] object MoarImpl {
  import Shared._

  def routeImpl0[R: c.WeakTypeTag](c: whitebox.Context)(body: c.Expr[() => R]): c.Expr[() => AsyncResult] = {
    c.Expr[() => AsyncResult](generate(c)(body.tree))
  }

  def routeImpl1[T1: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                                     (body: c.Expr[(T1) => R]): c.Expr[(T1) => AsyncResult] = {
    c.Expr[(T1) => AsyncResult](generate(c)(body.tree))
  }

  def routeImpl2[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                        (body: c.Expr[(T1, T2) => R]): c.Expr[(T1, T2) => AsyncResult] = {
    c.Expr[(T1, T2) => AsyncResult](generate(c)(body.tree))
  }

  def routeImpl3[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                        (body: c.Expr[(T1, T2, T3) => R]): c.Expr[(T1, T2, T3) => AsyncResult] = {
    c.Expr[(T1, T2, T3) => AsyncResult](generate(c)(body.tree))
  }

  def routeImpl4[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                        (body: c.Expr[(T1, T2, T3, T4) => R]): c.Expr[(T1, T2, T3, T4) => AsyncResult] = {
    c.Expr[(T1, T2, T3, T4) => AsyncResult](generate(c)(body.tree))
  }

  def routeImpl5[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                        (body: c.Expr[(T1, T2, T3, T4, T5) => R]): c.Expr[(T1, T2, T3, T4, T5) => AsyncResult] = {
    c.Expr[(T1, T2, T3, T4, T5) => AsyncResult](generate(c)(body.tree))
  }

  def routeImpl6[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                        (body: c.Expr[(T1, T2, T3, T4, T5, T6) => R]): c.Expr[(T1, T2, T3, T4, T5, T6) => AsyncResult] = {
    c.Expr[(T1, T2, T3, T4, T5, T6) => AsyncResult](generate(c)(body.tree))
  }

  def routeImpl7[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag,
    T7: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                        (body: c.Expr[(T1, T2, T3, T4, T5, T6, T7) => R]): c.Expr[(T1, T2, T3, T4, T5, T6, T7) => AsyncResult] = {
    c.Expr[(T1, T2, T3, T4, T5, T6, T7) => AsyncResult](generate(c)(body.tree))
  }

  def routeImpl8[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag,
    T7: c.WeakTypeTag,
    T8: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                        (body: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8) => R]): c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8) => AsyncResult] = {
    c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8) => AsyncResult](generate(c)(body.tree))
  }

  def routeImpl9[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag,
    T7: c.WeakTypeTag,
    T8: c.WeakTypeTag,
    T9: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                        (body: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9) => R]): c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9) => AsyncResult] = {
    c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9) => AsyncResult](generate(c)(body.tree))
  }

  def routeImpl10[T1: c.WeakTypeTag,
    T2: c.WeakTypeTag,
    T3: c.WeakTypeTag,
    T4: c.WeakTypeTag,
    T5: c.WeakTypeTag,
    T6: c.WeakTypeTag,
    T7: c.WeakTypeTag,
    T8: c.WeakTypeTag,
    T9: c.WeakTypeTag,
    T10: c.WeakTypeTag, R: c.WeakTypeTag](c: whitebox.Context)
                                         (body: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => R]): c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => AsyncResult] = {
    c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => AsyncResult](generate(c)(body.tree))
  }

  def routeImpl11[T1: c.WeakTypeTag,
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
                                         (body: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) => R]): c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) => AsyncResult] = {
    c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) => AsyncResult](generate(c)(body.tree))
  }

  def routeImpl12[T1: c.WeakTypeTag,
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
                                         (body: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) => R]): c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) => AsyncResult] = {
    c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) => AsyncResult](generate(c)(body.tree))
  }

  def routeImpl13[T1: c.WeakTypeTag,
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
                                         (body: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) => R]): c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) => AsyncResult] = {
    c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) => AsyncResult](generate(c)(body.tree))
  }

  def routeImpl14[T1: c.WeakTypeTag,
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
                                         (body: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) => R]): c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) => AsyncResult] = {
    c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) => AsyncResult](generate(c)(body.tree))
  }

  def routeImpl15[T1: c.WeakTypeTag,
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
                                         (body: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) => R]): c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) => AsyncResult] = {
    c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) => AsyncResult](generate(c)(body.tree))
  }

  def routeImpl16[T1: c.WeakTypeTag,
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
                                         (body: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) => R]): c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) => AsyncResult] = {
    c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) => AsyncResult](generate(c)(body.tree))
  }

  def routeImpl17[T1: c.WeakTypeTag,
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
                                         (body: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) => R]): c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) => AsyncResult] = {
    c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) => AsyncResult](generate(c)(body.tree))
  }

  def routeImpl18[T1: c.WeakTypeTag,
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
                                         (body: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18) => R]): c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18) => AsyncResult] = {
    c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18) => AsyncResult](generate(c)(body.tree))
  }

  def routeImpl19[T1: c.WeakTypeTag,
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
                                         (body: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) => R]): c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) => AsyncResult] = {
    c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) => AsyncResult](generate(c)(body.tree))
  }

  def routeImpl20[T1: c.WeakTypeTag,
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
                                         (body: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) => R]): c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) => AsyncResult] = {
    c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) => AsyncResult](generate(c)(body.tree))
  }

  def routeImpl21[T1: c.WeakTypeTag,
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
                                         (body: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21) => R]): c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21) => AsyncResult] = {
    c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21) => AsyncResult](generate(c)(body.tree))
  }

  def routeImpl22[T1: c.WeakTypeTag,
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
                                         (body: c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22) => R]): c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22) => AsyncResult] = {
    c.Expr[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22) => AsyncResult](generate(c)(body.tree))
  }


  private def generate(c: whitebox.Context)(body: c.Tree): c.Tree = {
    import c.universe._

    val focus = c.enclosingPosition

    guardSbtOptions(c)

    val logger = new Logger(c)

    val stateDef = TermName(c.freshName("state"))

    val (passedParameters, paramNames) = body match {
      case q"(..$params) => $_" =>
        val names = params.collect {
          case q"$_ val $tname: $_ = $_" =>
            tname
        }
        (params, names)

      case _ =>
        c.error(focus, "Function is required")
        (Seq.empty, Seq.empty)
    }

    val transformer = new Transformer {
      override def transform(tree: c.universe.Tree): c.universe.Tree = {
        tree match {
          case Apply(Select(
            Select(Select(Select(_, TermName("omhs")), TermName("moar")), _),
              TermName("contentType")), List(a)) =>
            q"$stateDef.setContentType(..$a)"

          case Apply(Select(
            Select(Select(Select(_, TermName("omhs")), TermName("moar")), _),
              TermName("status")), List(a)) =>
            q"$stateDef.setStatus(..$a)"

          case _ =>
            super.transform(tree)
        }
      }
    }

    val rewrittenTree = transformer.transform(body)

    val responseDefinition =
      q"""
        (..$passedParameters) => {
          val $stateDef = _root_.com.github.fntz.omhs.moar.MutableState.empty
          $stateDef.transform($rewrittenTree.apply(..$paramNames))
        }
        """

    logger.verbose(s"$responseDefinition")

    c.untypecheck(responseDefinition)
  }
}
