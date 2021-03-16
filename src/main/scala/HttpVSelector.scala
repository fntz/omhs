import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http2.Http2FrameCodecBuilder
import io.netty.handler.ssl.{ApplicationProtocolNames, ApplicationProtocolNegotiationHandler}
import io.netty.handler.stream.ChunkedWriteHandler

class HttpVSelector
  extends ApplicationProtocolNegotiationHandler(ApplicationProtocolNames.HTTP_1_1) {

  override def configurePipeline(ctx: ChannelHandlerContext, protocol: String): Unit = {

    if (ApplicationProtocolNames.HTTP_2 == protocol) {
      ctx.pipeline.addLast(
        Http2FrameCodecBuilder.forServer().build(),
        new CustomHttp2MessageDecoder(),
        new Http2ServerHandler
      )
      return
    }

    if (ApplicationProtocolNames.HTTP_1_1 == protocol) {
      ctx.pipeline.addLast(new HttpServerCodec,
        new ChunkedWriteHandler,
        new CustomHttpHandler())
      return
    }

    throw new IllegalStateException("Unknown protocol: " + protocol)
  }
}
