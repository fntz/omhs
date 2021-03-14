package com.github.fntz.omhs.util

import io.netty.handler.codec.http.{FullHttpRequest, HttpHeaderNames, HttpHeaderValues, HttpResponse}

private [omhs] object ResponseImplicits {
  import CollectionsConverters._
  import AdditionalHeaders._

  implicit class HttpHeadersImplicits(val response: HttpResponse) extends AnyVal {
    def withContentType(contentType: String): HttpResponse = {
      response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType)
      response
    }
    def withLength(length: Int): HttpResponse = {
      response.headers.set(HttpHeaderNames.CONTENT_LENGTH, length)
      response
    }
    def chunked: HttpResponse = {
      response.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED)
      response
    }
    def withDate(dateStr: String): HttpResponse = {
      response.headers().set(HttpHeaderNames.DATE, dateStr)
      response
    }

    def withUserHeaders(headers: Iterable[(String, String)]): HttpResponse = {
      headers.groupBy(_._1).foreach { case (h, vs) =>
        response.headers().set(h, vs.map(_._2).toJava)
      }
      response
    }

    def processKeepAlive(isKeepAlive: Boolean, request: FullHttpRequest): HttpResponse = {
      if (isKeepAlive) {
        if (!request.protocolVersion.isKeepAliveDefault) {
          response.headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
        }
      } else {
        response.headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE)
      }
      response
    }

    def withServer(value: String, isNeedToModify: Boolean): HttpResponse = {
      if (isNeedToModify) {
        response.headers.set(HttpHeaderNames.SERVER, value)
      }
      response
    }

    def withXSSProtection(isNeedToSend: Boolean): HttpResponse = {
      if (isNeedToSend) {
        response.headers().set(xssProtection, xssProtectionValue)
      }
      response
    }

  }

}
