package com.github.fntz.omhs

import com.github.fntz.omhs.impl.MoarImpl
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.cookie.{Cookie, ServerCookieEncoder}

package object moar {

  def contentType(str: String): Unit = ???

  def status(responseStatus: HttpResponseStatus): Unit = ???

  def status(intStatus: Int): Unit = ???

  def setCookie(name: String, value: String)(implicit enc: ServerCookieEncoder): Unit = ???

  def setCookie(cookie: Cookie)(implicit enc: ServerCookieEncoder): Unit = ???

  def setHeader(name: String, value: String): Unit = ???

  /**
   * Same surface DSL as Scala 2: `route { () => status(201); "ok" }`.
   * Rewrites moar helpers onto [[MutableState]] and returns a function to [[AsyncResult]].
   */
  transparent inline def route[F](inline body: F): Any =
    ${ MoarImpl.generate('body) }

}
