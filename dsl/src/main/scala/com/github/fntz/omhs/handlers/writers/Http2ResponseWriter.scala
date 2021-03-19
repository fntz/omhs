package com.github.fntz.omhs.handlers.writers

import com.github.fntz.omhs.handlers.ResourceResultContainer
import com.github.fntz.omhs.handlers.http2.AggregatedHttp2Message
import com.github.fntz.omhs.streams.ChunkedOutputStream
import com.github.fntz.omhs.util.{Http2HeadersImplicits, FullHttpRequestImplicits}
import com.github.fntz.omhs.{CommonResponse, Route, Setup, StreamResponse}
import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelFuture, ChannelHandlerContext}
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http2.{DefaultHttp2DataFrame, DefaultHttp2Headers, DefaultHttp2HeadersFrame}
import io.netty.util.concurrent.{Future, GenericFutureListener}

import java.time.ZonedDateTime

case class Http2ResponseWriter(route: Route,
                               setup: Setup,
                               request: FullHttpRequest,
                               ctx: ChannelHandlerContext,
                               agg: AggregatedHttp2Message
                ) extends FileCleaner with CommonWriter {

  import ServerVersionHelper._
  import Http2HeadersImplicits._
  import FullHttpRequestImplicits._

  def write(result: ResourceResultContainer): Unit = {
    result.asyncResult.onComplete {
      case outResponse: CommonResponse =>
        write(outResponse).addListener(fileCleaner(result.files))

      case streamResponse: StreamResponse =>
        write(streamResponse.stream)
    }
  }

  private def write(userResponse: CommonResponse): ChannelFuture = {
    checkContentTypeOnTraceMethod(userResponse)
    val content = toContent(userResponse)
    //??? content.writeBytes(Unpooled.EMPTY_BUFFER.duplicate())

    val headers = new DefaultHttp2Headers().status(userResponse.status.codeAsText())
      .withContentType(userResponse.contentType)
      .withUserHeaders(userResponse.headers)
      .withDate(ZonedDateTime.now().format(setup.timeFormatter))
      .withLength(userResponse.content.length)
      .withServer(ServerVersion, setup.sendServerHeader)

    ctx.write(route.rewrite(new DefaultHttp2HeadersFrame(headers).stream(agg.stream)))
    ctx.write(new DefaultHttp2DataFrame(content, true).stream(agg.stream))
  }

  private def write(stream: ChunkedOutputStream): ChannelFuture = {
    stream.flush()

    ctx.write(new DefaultHttp2DataFrame(Unpooled.EMPTY_BUFFER, true)
      .stream(agg.stream)).addListener(new GenericFutureListener[Future[_ >: Void]] {
      override def operationComplete(future: Future[_ >: Void]): Unit = {
        stream.close()
      }
    })
  }


}
