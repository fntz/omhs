package com.github.fntz.omhs.handlers

import com.github.fntz.omhs.Setup
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http2.Http2FrameCodecBuilder
import io.netty.handler.ssl.{ApplicationProtocolNames => APN, ApplicationProtocolNegotiationHandler => APNH}
import io.netty.handler.stream.ChunkedWriteHandler

class HttpMixedHandler(setup: Setup, handler: HttpHandler)
  extends APNH(APN.HTTP_1_1) {

  override def configurePipeline(ctx: ChannelHandlerContext, protocol: String): Unit = {
    protocol match {
      case APN.HTTP_2 =>
        ctx.pipeline.addLast(
          Http2FrameCodecBuilder.forServer().build(),
          new Http2Handler(setup)
        )
      case APN.HTTP_1_1 =>
        ctx.pipeline.addLast(new HttpServerCodec,
          new ChunkedWriteHandler,
          handler)

      case _ =>
        throw new IllegalStateException(s"Unknown protocol: $protocol")
    }
  }

}


