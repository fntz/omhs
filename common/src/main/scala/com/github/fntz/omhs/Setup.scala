package com.github.fntz.omhs

import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 *
 * @param timeFormatter - set current server time in Date header
 * @param sendServerHeader - send X-Server header
 * @param cookieDecoderStrategy - how to decode cookies @see ServerCookieDecoder
 * @param maxContentLength - max length in aggregated request @see OMHSServer
 * @param enableCompression - use HttpContentCompressor @see OMHSServer
 */
case class Setup(
                timeFormatter: DateTimeFormatter,
                sendServerHeader: Boolean,
                cookieDecoderStrategy: CookieDecoderStrategy,
                maxContentLength: Int,
                enableCompression: Boolean,
                chunkSize: Int
                )
object Setup {
  val default: Setup = Setup(
    timeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME
      .withZone(ZoneOffset.UTC).withLocale(Locale.US),
    sendServerHeader = true,
    cookieDecoderStrategy = CookieDecoderStrategies.Strict,
    maxContentLength = 512*1024,
    enableCompression = true,
    chunkSize = 1000
  )
}