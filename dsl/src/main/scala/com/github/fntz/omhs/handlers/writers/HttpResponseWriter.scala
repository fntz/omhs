package com.github.fntz.omhs.handlers.writers

import com.github.fntz.omhs.handlers.ResourceResultContainer
import com.github.fntz.omhs.streams.ChunkedOutputStream
import com.github.fntz.omhs.util.ResponseImplicits
import com.github.fntz.omhs.{CommonResponse, Route, Setup, StreamResponse}
import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelFuture, ChannelFutureListener, ChannelHandlerContext}
import io.netty.handler.codec.http._
import io.netty.util.concurrent.{Future, GenericFutureListener}

import java.time.ZonedDateTime

case class HttpResponseWriter(route: Route,
                              setup: Setup,
                              ctx: ChannelHandlerContext,
                              request: FullHttpRequest
                             ) extends CommonWriter {

  import ServerVersionHelper._
  import HttpResponseWriter._
  import ResponseImplicits._

  private lazy val isKeepAlive = HttpUtil.isKeepAlive(request)

  def write(result: ResourceResultContainer): Unit = {
    result.asyncResult.onComplete {
      case outResponse: CommonResponse =>
        write(outResponse).addListener(fileCleaner(result.files))

      case streamResponse: StreamResponse =>
        write(streamResponse.stream).addListener(fileCleaner(result.files))
    }
  }

  private def write(userResponse: CommonResponse): ChannelFuture = {
    checkContentTypeOnTraceMethod(userResponse)
    val content = toContent(userResponse)
    val response = empty.replace(content)
      .processKeepAlive(isKeepAlive, request)
      .withUserHeaders(userResponse.headers)
      .setStatus(userResponse.status)
      .withContentType(userResponse.contentType)
      .withDate(ZonedDateTime.now().format(setup.timeFormatter))
      .withLength(userResponse.content.length)
      .withServer(ServerVersion, setup.sendServerHeader)

    val f = ctx.writeAndFlush(route.rewrite(response))

    if (!isKeepAlive) {
      f.addListener(ChannelFutureListener.CLOSE)
    }
    f
  }

  private def write(stream: ChunkedOutputStream): ChannelFuture = {

    stream.flush()

    val f = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
      .addListener(new GenericFutureListener[Future[_ >: Void]] {
        override def operationComplete(future: Future[_ >: Void]): Unit = {
          stream.close()
        }
      })
    if (!isKeepAlive) {
      f.addListener(ChannelFutureListener.CLOSE)
    }
    f
  }

}
object HttpResponseWriter {
  private def empty = new DefaultFullHttpResponse(
    HttpVersion.HTTP_1_1,
    HttpResponseStatus.OK,
    Unpooled.EMPTY_BUFFER
  )
}
