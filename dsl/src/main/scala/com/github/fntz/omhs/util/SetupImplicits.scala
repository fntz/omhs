package com.github.fntz.omhs.util

import com.github.fntz.omhs.{CookieDecoderStrategies, Setup}
import io.netty.handler.codec.http.{FullHttpRequest, HttpHeaderNames}
import io.netty.handler.codec.http.cookie.{Cookie, ServerCookieDecoder}

object SetupImplicits {
  import CollectionsConverters._

  implicit class SetupExt(val setup: Setup) extends AnyVal {
    def decode(request: FullHttpRequest): Iterable[Cookie] = {
      Option(request.headers.get(HttpHeaderNames.COOKIE)).map { x =>
        val decoder = setup.cookieDecoderStrategy match {
          case CookieDecoderStrategies.Strict =>
            ServerCookieDecoder.STRICT
          case CookieDecoderStrategies.Lax =>
            ServerCookieDecoder.LAX
        }
        decoder.decode(x).toScala
      }.getOrElse(Set.empty)
    }
  }

}
