package com.github.fntz.omhs

import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

case class Setup(
                timeFormatter: DateTimeFormatter,
                sendServerHeader: Boolean,
                cookieDecoderStrategy: CookieDecoderStrategy
                )
object Setup {
  val default = Setup(
    timeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME
      .withZone(ZoneOffset.UTC).withLocale(Locale.US),
    sendServerHeader = true,
    cookieDecoderStrategy = CookieDecoderStrategies.Strict
  )
}

/**
 * @see io.netty.handler.codec.http.cookie.ServerCookieDecoder
 */
sealed trait CookieDecoderStrategy
case object CookieDecoderStrategies {
  case object Strict extends CookieDecoderStrategy
  case object Lax extends CookieDecoderStrategy
}
