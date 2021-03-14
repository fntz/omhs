package com.github.fntz.omhs

import com.github.fntz.omhs.impl.MoarImpl
import io.netty.handler.codec.http.HttpResponseStatus

import scala.language.experimental.macros

package object moar {

  def contentType(str: String): Unit = ???

  def status(responseStatus: HttpResponseStatus): Unit = ???

  def route[R](body: () => R): () => AsyncResult = macro MoarImpl.routeImpl0[R]

  def route[T1, R](body: (T1) => R): (T1) => AsyncResult = macro MoarImpl.routeImpl1[T1, R]

  def route[T1, T2, R](body: (T1, T2) => R): (T1, T2) => AsyncResult = macro MoarImpl.routeImpl2[T1, T2, R]

  def route[T1, T2, T3, R](body: (T1, T2, T3) => R): (T1, T2, T3) => AsyncResult = macro MoarImpl.routeImpl3[T1, T2, T3, R]

  def route[T1, T2, T3, T4, R](body: (T1, T2, T3, T4) => R): (T1, T2, T3, T4) => AsyncResult = macro MoarImpl.routeImpl4[T1, T2, T3, T4, R]

  def route[T1, T2, T3, T4, T5, R](body: (T1, T2, T3, T4, T5) => R): (T1, T2, T3, T4, T5) => AsyncResult = macro MoarImpl.routeImpl5[T1, T2, T3, T4, T5, R]

  def route[T1, T2, T3, T4, T5, T6, R](body: (T1, T2, T3, T4, T5, T6) => R): (T1, T2, T3, T4, T5, T6) => AsyncResult = macro MoarImpl.routeImpl6[T1, T2, T3, T4, T5, T6, R]

  def route[T1, T2, T3, T4, T5, T6, T7, R](body: (T1, T2, T3, T4, T5, T6, T7) => R): (T1, T2, T3, T4, T5, T6, T7) => AsyncResult = macro MoarImpl.routeImpl7[T1, T2, T3, T4, T5, T6, T7, R]

  def route[T1, T2, T3, T4, T5, T6, T7, T8, R](body: (T1, T2, T3, T4, T5, T6, T7, T8) => R): (T1, T2, T3, T4, T5, T6, T7, T8) => AsyncResult = macro MoarImpl.routeImpl8[T1, T2, T3, T4, T5, T6, T7, T8, R]

  def route[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](body: (T1, T2, T3, T4, T5, T6, T7, T8, T9) => R): (T1, T2, T3, T4, T5, T6, T7, T8, T9) => AsyncResult = macro MoarImpl.routeImpl9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R]

  def route[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R](body: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => R): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => AsyncResult = macro MoarImpl.routeImpl10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R]

  def route[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R](body: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) => R): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) => AsyncResult = macro MoarImpl.routeImpl11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R]

  def route[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R](body: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) => R): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) => AsyncResult = macro MoarImpl.routeImpl12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R]

  def route[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R](body: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) => R): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) => AsyncResult = macro MoarImpl.routeImpl13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R]

  def route[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R](body: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) => R): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) => AsyncResult = macro MoarImpl.routeImpl14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R]

  def route[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R](body: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) => R): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) => AsyncResult = macro MoarImpl.routeImpl15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R]

  def route[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R](body: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) => R): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) => AsyncResult = macro MoarImpl.routeImpl16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R]

  def route[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R](body: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) => R): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) => AsyncResult = macro MoarImpl.routeImpl17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R]

  def route[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R](body: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18) => R): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18) => AsyncResult = macro MoarImpl.routeImpl18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R]

  def route[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R](body: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) => R): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) => AsyncResult = macro MoarImpl.routeImpl19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R]

  def route[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R](body: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) => R): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) => AsyncResult = macro MoarImpl.routeImpl20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R]

  def route[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R](body: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21) => R): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21) => AsyncResult = macro MoarImpl.routeImpl21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R]

  def route[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R](body: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22) => R): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22) => AsyncResult = macro MoarImpl.routeImpl22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R]

}
