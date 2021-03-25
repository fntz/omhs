package com.github.fntz.omhs.handlers.writers

import com.github.fntz.omhs.util.Http2HeadersImplicits
import com.github.fntz.omhs.{Route, Setup}
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http2.{DefaultHttp2Headers, DefaultHttp2HeadersFrame, Http2FrameStream}

import java.time.ZonedDateTime

case class ErrorWriters(route: Route, setup: Setup) {
  import Http2HeadersImplicits._
  import ServerVersionHelper._

  def write413(ctx: ChannelHandlerContext, stream: Http2FrameStream) = {
    val headers = new DefaultHttp2Headers()
      .status(HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE.codeAsText())
      .withDate(ZonedDateTime.now().format(setup.timeFormatter))
      .withServer(ServerVersion, setup.sendServerHeader)

    ctx.writeAndFlush(route.rewrite(new DefaultHttp2HeadersFrame(headers, true).stream(stream)))
  }

}
