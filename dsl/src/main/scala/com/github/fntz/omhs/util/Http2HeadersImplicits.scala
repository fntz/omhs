package com.github.fntz.omhs.util

import io.netty.handler.codec.http.{HttpHeaderNames, HttpHeaderValues}
import io.netty.handler.codec.http2.Http2Headers

object Http2HeadersImplicits {
  import CollectionsConverters._
  import AdditionalHeadersConstants._

  implicit class Http2HeadersExt(val current: Http2Headers) extends AnyVal {
    def withUserHeaders(headers: Iterable[(String, String)]): Http2Headers = {
      headers.groupBy(_._1).foreach { case (h, vs) =>
        current.set(h, vs.map(_._2).toJava)
      }
      current
    }
    def withContentType(contentType: String): Http2Headers = {
      current.set(HttpHeaderNames.CONTENT_TYPE, contentType)
      current
    }

    def withLength(length: Int): Http2Headers = {
      current.set(HttpHeaderNames.CONTENT_LENGTH, length.toString)
      current
    }

    def withDate(dateStr: String): Http2Headers = {
      current.set(HttpHeaderNames.DATE, dateStr)
      current
    }

    def withServer(value: String, isNeedToModify: Boolean): Http2Headers = {
      if (isNeedToModify) {
        current.set(HttpHeaderNames.SERVER, value)
      }
      current
    }
  }

}
