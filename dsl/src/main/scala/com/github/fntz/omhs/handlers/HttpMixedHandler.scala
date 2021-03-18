package com.github.fntz.omhs.handlers

import com.github.fntz.omhs.Setup
import com.github.fntz.omhs.handlers.http2.Http2MessageDecoder
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.{HttpContentCompressor, HttpObjectAggregator, HttpServerCodec}
import io.netty.handler.codec.http2.Http2FrameCodecBuilder
import io.netty.handler.ssl.{ApplicationProtocolNames => APN, ApplicationProtocolNegotiationHandler => APNH}

class HttpMixedHandler(handler: HttpHandler, setup: Setup)
  extends APNH(APN.HTTP_1_1) {

  override def configurePipeline(ctx: ChannelHandlerContext, protocol: String): Unit = {
    protocol match {
      case APN.HTTP_2 =>
        ctx.pipeline.addLast(
        Http2FrameCodecBuilder.forServer().build(),
          new Http2MessageDecoder(setup.maxContentLength),
          handler
        )
      case APN.HTTP_1_1 =>
        val p = ctx.pipeline()
        p.addLast("codec", new HttpServerCodec())
        p.addLast("aggregator", new HttpObjectAggregator(setup.maxContentLength))

        if (setup.enableCompression) {
          p.addLast("compressor", new HttpContentCompressor())
        }
        p.addLast("omhs", handler)

      case _ =>
        throw new IllegalStateException(s"Unknown protocol: $protocol")
    }
  }

}


