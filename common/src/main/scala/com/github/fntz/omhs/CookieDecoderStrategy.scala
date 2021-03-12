package com.github.fntz.omhs

/**
 * @see io.netty.handler.codec.http.cookie.ServerCookieDecoder
 */
sealed trait CookieDecoderStrategy
case object CookieDecoderStrategies {
  case object Strict extends CookieDecoderStrategy
  case object Lax extends CookieDecoderStrategy
}
