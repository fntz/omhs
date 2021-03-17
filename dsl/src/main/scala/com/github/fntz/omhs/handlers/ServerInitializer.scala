package com.github.fntz.omhs.handlers

import com.github.fntz.omhs.Setup
import com.github.fntz.omhs.handlers.http2.Http2MessageDecoder
import io.netty.channel.{ChannelHandlerContext, ChannelInitializer, ChannelPipeline, SimpleChannelInboundHandler}
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.{HttpContentCompressor, HttpMessage, HttpObjectAggregator, HttpServerCodec, HttpServerUpgradeHandler}
import io.netty.handler.codec.http2.{Http2CodecUtil, Http2FrameCodecBuilder, Http2ServerUpgradeCodec}
import io.netty.handler.ssl.SslContext
import io.netty.handler.stream.ChunkedWriteHandler
import io.netty.util.AsciiString
import org.slf4j.LoggerFactory

class ServerInitializer(sslContext: Option[SslContext],
                        setup: Setup,
                        handler: HttpHandler,
                        pipeLineChanges: ChannelPipeline => ChannelPipeline
                      ) extends ChannelInitializer[SocketChannel] {

  private val logger = LoggerFactory.getLogger(getClass)

  override def initChannel(ch: SocketChannel): Unit = {
    logger.debug(s"http2: ${setup.mode.isH2}, ssl: ${sslContext.isDefined}")
    if (setup.mode.isH2) {
      sslContext match {
        case Some(ssl) =>
          configureSsl(ch, ssl)
        case _ =>
          configureClearText(ch)
      }
    } else {
      // plain
      val p = ch.pipeline()
      sslContext.foreach { ssl =>
        p.addLast(ssl.newHandler(ch.alloc()))
      }
      p.addLast("codec", new HttpServerCodec())
      p.addLast("aggregator", new HttpObjectAggregator(setup.maxContentLength))

      pipeLineChanges(p)

      if (setup.enableCompression) {
        p.addLast("compressor", new HttpContentCompressor())
      }
      p.addLast("omhs", handler)
    }
  }

  private def configureSsl(ch: SocketChannel, ssl: SslContext): Unit = {
    ch.pipeline().addLast(
      ssl.newHandler(ch.alloc()),
      new HttpMixedHandler(setup, handler))
      pipeLineChanges(ch.pipeline())
  }

  private def configureClearText(ch: SocketChannel): Unit = {
    val p = ch.pipeline()
    val codec = new HttpServerCodec()
    p.addLast(codec)
    p.addLast(new HttpServerUpgradeHandler(codec, new HttpServerUpgradeHandler.UpgradeCodecFactory {
      override def newUpgradeCodec(protocol: CharSequence): HttpServerUpgradeHandler.UpgradeCodec = {
        if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol)) {
          new Http2ServerUpgradeCodec(
            Http2FrameCodecBuilder.forServer().build(),
            new Http2MessageDecoder(),
            new Http2Handler(setup)
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
        p.addAfter(current.name(), null, handler)
        p.replace(this, null, new ChunkedWriteHandler())
        ctx.fireChannelRead(msg)
      }
    })
  }
}
