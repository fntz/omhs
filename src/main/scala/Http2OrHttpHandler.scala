import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpServerCodec}
import io.netty.handler.codec.http2.{AbstractHttp2ConnectionHandlerBuilder, Http2ConnectionDecoder, Http2ConnectionEncoder, Http2FrameCodecBuilder, Http2Settings}
import io.netty.handler.ssl.{ApplicationProtocolNames, ApplicationProtocolNegotiationHandler, SslHandshakeCompletionEvent}

class Http2OrHttpHandler extends ApplicationProtocolNegotiationHandler(ApplicationProtocolNames.HTTP_1_1) {
  override def configurePipeline(ctx: ChannelHandlerContext, protocol: String): Unit = {
    println("~"*100)
    if (ApplicationProtocolNames.HTTP_2 == protocol) {
      ctx.pipeline().addLast(
        Http2FrameCodecBuilder.forServer().build(),
        new Http2HandlerX()
      )
      //ctx.pipeline().addLast(new AbstractHttp2Builder().build())
    } else if (ApplicationProtocolNames.HTTP_1_1 == protocol) {
      ctx.pipeline().addLast(
        new HttpServerCodec(),
        new HttpObjectAggregator(512*1024),
        new CustomHttpHandler
      )
    } else {
      throw new IllegalStateException(s"Unknown protocol: $protocol")
    }
  }

  override def handshakeFailure(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    super.handshakeFailure(ctx, cause)
  }
}

class AbstractHttp2Builder
  extends AbstractHttp2ConnectionHandlerBuilder[Http2Handler, AbstractHttp2Builder] {
  override def build(decoder: Http2ConnectionDecoder, encoder: Http2ConnectionEncoder, initialSettings: Http2Settings): Http2Handler = {
    new Http2Handler(decoder, encoder, initialSettings)
  }

  override def build(): Http2Handler = {
    val z = super.build()
    println("^"*100)
    z
  }
}