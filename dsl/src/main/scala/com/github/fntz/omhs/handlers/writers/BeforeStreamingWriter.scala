package com.github.fntz.omhs.handlers.writers

import com.github.fntz.omhs.internal.{ParamDef, StreamDef}
import com.github.fntz.omhs.util.{Http2HeadersImplicits, ResponseImplicits}
import com.github.fntz.omhs.{Route, Setup, UnhandledReason}
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http._
import io.netty.handler.codec.http2.{DefaultHttp2Headers, DefaultHttp2HeadersFrame, Http2FrameStream}
import org.slf4j.LoggerFactory

import java.time.ZonedDateTime

case class BeforeStreamingWriter(
                                route: Route,
                                setup: Setup
                                ) {

  import Http2HeadersImplicits._
  import ResponseImplicits._
  import ServerVersionHelper._

  private val logger = LoggerFactory.getLogger(getClass)

  def write(
             ctx: ChannelHandlerContext,
             request: FullHttpRequest,
             http2Stream: Option[Http2FrameStream],
             materialized: Either[UnhandledReason, List[ParamDef[_]]]
           ): Unit = {
    // if user wants to stream data, I should send empty response first
    val hasStream = materialized.getOrElse(Nil).collectFirst {
      case _: StreamDef => true
    }.getOrElse(false)
    if (hasStream) {
      logger.debug("Streaming detected, write empty response")
      http2Stream match {
        case Some(stream) =>
          writeEmptyOnStream(ctx, stream)
        case _ =>
          writeEmptyOnStream(ctx, request)
      }
    }
  }

  private def writeEmptyOnStream(ctx: ChannelHandlerContext, http2Stream: Http2FrameStream) = {
    val headers = new DefaultHttp2Headers().status(HttpResponseStatus.OK.codeAsText())
      .withContentType(HttpHeaderValues.APPLICATION_OCTET_STREAM.toString)
      .withDate(ZonedDateTime.now().format(setup.timeFormatter))
      .withServer(ServerVersion, setup.sendServerHeader)

    ctx.write(route.rewrite(new DefaultHttp2HeadersFrame(headers, false)
      .stream(http2Stream)))
  }

  private def writeEmptyOnStream(ctx: ChannelHandlerContext, request: FullHttpRequest) = {
    val response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK)

    response
      .withContentType(HttpHeaderValues.APPLICATION_OCTET_STREAM.toString)
      .chunked
      .withDate(ZonedDateTime.now().format(setup.timeFormatter))
      .processKeepAlive(HttpUtil.isKeepAlive(request), request)
      .withServer(ServerVersion, setup.sendServerHeader)

    ctx.write(route.rewrite(response)) // will be flushed with first chunk
  }
}
