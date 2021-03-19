package com.github.fntz.omhs.handlers.writers

import com.github.fntz.omhs.CommonResponse
import com.github.fntz.omhs.util.FullHttpRequestImplicits
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.handler.codec.http.{FullHttpRequest, HttpHeaderNames, HttpResponse}
import org.slf4j.LoggerFactory

trait CommonWriter {
  val request: FullHttpRequest

  private val expectedContentType = "message/http"

  private val logger = LoggerFactory.getLogger(getClass)

  import FullHttpRequestImplicits._

  def toContent(userResponse: CommonResponse): ByteBuf = {
    if (request.isHead) {
      // https://tools.ietf.org/html/rfc7231#page-25
      // @note I do not remove content* headers:
      // https://tools.ietf.org/html/rfc7231#section-3.3
      Unpooled.EMPTY_BUFFER
    } else {
      if (userResponse.content.nonEmpty) {
        logger.warn("You try to send non-empty response with `head`-method")
      }
      Unpooled.copiedBuffer(userResponse.content)
    }
  }

  def checkContentTypeOnTraceMethod(userResponse: CommonResponse): Unit = {
    // https://tools.ietf.org/html/rfc7231#section-4.3.8
    if (request.isTrace && userResponse.contentType.toLowerCase != expectedContentType) {
      logger.warn(s"You use `trace`-method with illegal content-type: ${userResponse.contentType} " +
        s"expected is '$expectedContentType'")
    }
  }
}
