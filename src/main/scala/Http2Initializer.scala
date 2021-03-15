import io.netty.channel.{ChannelHandlerContext, ChannelInitializer, SimpleChannelInboundHandler}
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpServerUpgradeHandler.UpgradeCodecFactory
import io.netty.handler.codec.http.{HttpMessage, HttpObjectAggregator, HttpServerCodec, HttpServerUpgradeHandler}
import io.netty.handler.codec.http2.{CleartextHttp2ServerUpgradeHandler, Http2CodecUtil, Http2ServerUpgradeCodec}
import io.netty.handler.ssl.SslContext
import io.netty.util.{AsciiString, ReferenceCountUtil}

class Http2Initializer(val ssl: Option[SslContext], maxContentLength: Int = 16*1024) extends ChannelInitializer[SocketChannel] {
  override def initChannel(ch: SocketChannel): Unit = {
    ssl match {
      case Some(sslContext) =>
        configureSsl(ch, sslContext)
      case _ =>
        configureClearText(ch)
    }
  }

  private def configureSsl(ch: SocketChannel, ctx: SslContext): Unit = {
    ch.pipeline().addLast(ctx.newHandler(ch.alloc()), new Http2OrHttpHandler)
  }

  import io.netty.handler.codec.http.HttpServerUpgradeHandler.UpgradeCodecFactory
  import io.netty.handler.codec.http2.Http2CodecUtil
  import io.netty.handler.codec.http2.Http2ServerUpgradeCodec
  import io.netty.util.AsciiString

  private def configureClearText(ch: SocketChannel) = {
    val p = ch.pipeline()
    val codec = new HttpServerCodec()
    val upgradeHandler =
      new HttpServerUpgradeHandler(codec, upgradeCodecFactory(_))
    val cleartextHttp2ServerUpgradeHandler = new CleartextHttp2ServerUpgradeHandler(
      codec, upgradeHandler,
      new AbstractHttp2Builder().build()
    )
    p.addLast(cleartextHttp2ServerUpgradeHandler)
    p.addLast(new SimpleChannelInboundHandler[HttpMessage] {
      override def channelRead0(ctx: ChannelHandlerContext, msg: HttpMessage): Unit = {
        System.err.println("Directly talking: " +
          msg.protocolVersion + " (no upgrade was attempted)")
        val pipeline = ctx.pipeline
        pipeline.addAfter(ctx.name, null,
          new CustomHttpHandler())
        pipeline.addAfter(ctx.name, null,
          new HttpObjectAggregator(1024*512))
        ctx.fireChannelRead(ReferenceCountUtil.retain(msg))
        pipeline.remove(this)
      }
    })

  }

  private def upgradeCodecFactory(protocol: CharSequence) = {
    if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol)) {
      new Http2ServerUpgradeCodec(new AbstractHttp2Builder().build())
    } else {
      null
    }
  }

}
