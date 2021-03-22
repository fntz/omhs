package com.github.fntz.omhs.handlers

import com.github.fntz.omhs.Setup
import com.github.fntz.omhs.handlers.http2.Http2MessageDecoder
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.{HttpContentCompressor, HttpObjectAggregator, HttpServerCodec}
import io.netty.handler.codec.http2.Http2FrameCodecBuilder
import io.netty.handler.ssl.{ApplicationProtocolNames => APN, ApplicationProtocolNegotiationHandler => APNH}

class HttpMixedHandler(handler: HttpHandler, setup: Setup) extends APNH(APN.HTTP_1_1) {

  import ServerInitializer._

  override def configurePipeline(ctx: ChannelHandlerContext, protocol: String): Unit = {
    val p = ctx.pipeline()

    protocol match {
      case APN.HTTP_2 =>
        p.addLast(Codec2, Http2FrameCodecBuilder.forServer().build())
        p.addLast(Decoder, new Http2MessageDecoder(setup.maxContentLength))
        p.addLast(Omhs, handler)

      case APN.HTTP_1_1 =>
        p.addLast(Codec, new HttpServerCodec())
        p.addLast(Aggregator, new HttpObjectAggregator(setup.maxContentLength))
        p.addLast(Omhs, handler)
        if (setup.enableCompression) {
          p.addLast(Compressor, new HttpContentCompressor())
        }

      case _ =>
        throw new IllegalStateException(s"Unknown protocol: $protocol")
    }
  }

}


