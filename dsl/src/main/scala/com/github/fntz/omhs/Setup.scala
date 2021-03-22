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
 * @param mode - http2/http1.1 or mixed
 **/
case class Setup(
                  timeFormatter: DateTimeFormatter,
                  sendServerHeader: Boolean,
                  cookieDecoderStrategy: CookieDecoderStrategy,
                  maxContentLength: Int,
                  enableCompression: Boolean,
                  chunkSize: Int,
                  isSupportHttp2: Boolean
                ) {
  def withSendServerHeader(flag: Boolean): Setup = {
    copy(sendServerHeader = flag)
  }

  def h2: Setup = {
    copy(isSupportHttp2 = true)
  }

  def h1: Setup = {
    copy(isSupportHttp2 = false)
  }

  def withoutCompression: Setup = {
    copy(enableCompression = false)
  }

  def isH2: Boolean = isSupportHttp2
  def isH1: Boolean = !isSupportHttp2
}
object Setup {
  val default: Setup = Setup(
    timeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME
      .withZone(ZoneOffset.UTC).withLocale(Locale.US),
    sendServerHeader = true,
    cookieDecoderStrategy = CookieDecoderStrategies.Strict,
    maxContentLength = 512*1024,
    enableCompression = true,
    chunkSize = 512,
    isSupportHttp2 = true
  )
}