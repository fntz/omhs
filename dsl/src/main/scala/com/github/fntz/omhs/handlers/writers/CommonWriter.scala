package com.github.fntz.omhs.handlers.writers

import com.github.fntz.omhs.CommonResponse
import com.github.fntz.omhs.internal.FileDef
import com.github.fntz.omhs.util.FullHttpRequestImplicits
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.channel.{ChannelFuture, ChannelFutureListener}
import io.netty.handler.codec.http.FullHttpRequest
import org.slf4j.LoggerFactory

trait CommonWriter {
  val request: FullHttpRequest

  private val expectedContentType = "message/http"

  private val logger = LoggerFactory.getLogger(getClass)

  import FullHttpRequestImplicits._

  protected def toContent(userResponse: CommonResponse): ByteBuf = {
    if (request.isHead) {
      // https://tools.ietf.org/html/rfc7231#page-25
      // @note I do not remove content* headers:
      // https://tools.ietf.org/html/rfc7231#section-3.3
      if (userResponse.content.nonEmpty) {
        logger.warn("You try to send non-empty response with `head`-method")
      }
      Unpooled.EMPTY_BUFFER
    } else {
      Unpooled.copiedBuffer(userResponse.content)
    }
  }

  protected def checkContentTypeOnTraceMethod(userResponse: CommonResponse): Unit = {
    // https://tools.ietf.org/html/rfc7231#section-4.3.8
    if (request.isTrace && userResponse.contentType.toLowerCase != expectedContentType) {
      logger.warn(s"You use `trace`-method with illegal content-type: ${userResponse.contentType} " +
        s"expected is '$expectedContentType'")
    }
  }

  protected def fileCleaner(files: List[FileDef]): ChannelFutureListener = {
    (_: ChannelFuture) => {
      files.flatMap(_.value).filter(_.refCnt() != 0).map(_.release())
    }
  }
}
