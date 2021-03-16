import io.netty.channel.socket.SocketChannel
import io.netty.channel.{ChannelHandlerContext, ChannelInitializer, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.{HttpMessage, HttpServerCodec, HttpServerUpgradeHandler}
import io.netty.handler.codec.http2.{Http2CodecUtil, Http2FrameCodecBuilder, Http2ServerUpgradeCodec}
import io.netty.handler.ssl.SslContext
import io.netty.handler.stream.ChunkedWriteHandler
import io.netty.util.AsciiString

class HttpSInitializer(ssl: Option[SslContext]) extends ChannelInitializer[SocketChannel] {
  override def initChannel(ch: SocketChannel): Unit = {
    ssl match {
      case Some(ctx) =>
        configureSsl(ctx, ch)
      case _ =>
        configurePlain(ch)
    }
  }

  def configurePlain(ch: SocketChannel) = {
    val p = ch.pipeline()
    val codec = new HttpServerCodec()
    p.addLast(codec)
    p.addLast(new HttpServerUpgradeHandler(codec, new HttpServerUpgradeHandler.UpgradeCodecFactory {
      override def newUpgradeCodec(protocol: CharSequence): HttpServerUpgradeHandler.UpgradeCodec = {
        if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol)) {
          new Http2ServerUpgradeCodec(
            Http2FrameCodecBuilder.forServer().build(),
            new Http2ServerHandler
          )
        } else {
          null
        }
      }
    }))
    p.addLast(new SimpleChannelInboundHandler[HttpMessage] {
      override def channelRead0(ctx: ChannelHandlerContext, msg: HttpMessage): Unit = {
        val p = ctx.pipeline()
        val current = p.context(this)
        p.addAfter(current.name(), null, new CustomHttpHandler)
        p.replace(this, null, new ChunkedWriteHandler())
        ctx.fireChannelRead(msg)
      }
    })
  }

  def configureSsl(sslContext: SslContext, ch: SocketChannel) = {
    ch.pipeline().addLast(
      sslContext.newHandler(ch.alloc()),
      new CustomHttp2MessageDecoder(),
      new HttpVSelector())
  }
}
