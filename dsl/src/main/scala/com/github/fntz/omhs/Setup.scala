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
 * @param chunkSize - bytes in every chunk. Let's say you set chunkSize into 10, then write 100 bytes,
 *                  as a result the library will split 100 bytes into 10 responses (100/10)
 * @param sendXSSProtection - @see https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-XSS-Protection
 *                            @note only for http1.1
 * @param mode - http2/http1.1 or mixed
 **/
case class Setup(
                  timeFormatter: DateTimeFormatter,
                  sendServerHeader: Boolean,
                  cookieDecoderStrategy: CookieDecoderStrategy,
                  maxContentLength: Int,
                  enableCompression: Boolean,
                  chunkSize: Int,
                  sendXSSProtection: Boolean,
                  mode: WorkMode
                ) {
  def withSendXSSProtection(flag: Boolean): Setup = {
    copy(sendXSSProtection = flag)
  }

  def withSendServerHeader(flag: Boolean): Setup = {
    copy(sendServerHeader = flag)
  }

  def h2: Setup = {
    copy(mode = WorkModes.Http2)
  }
}
object Setup {
  val default: Setup = Setup(
    timeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME
      .withZone(ZoneOffset.UTC).withLocale(Locale.US),
    sendServerHeader = true,
    cookieDecoderStrategy = CookieDecoderStrategies.Strict,
    maxContentLength = 512*1024,
    enableCompression = true,
    chunkSize = 1024,
    sendXSSProtection = true,
    mode = WorkModes.Http11
  )
}