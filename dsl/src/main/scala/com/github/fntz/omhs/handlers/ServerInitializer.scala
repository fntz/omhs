package com.github.fntz.omhs.handlers

import com.github.fntz.omhs.Setup
import com.github.fntz.omhs.handlers.http2.Http2MessageDecoder
import io.netty.channel.socket.SocketChannel
import io.netty.channel.{AbstractChannel, ChannelHandlerContext, ChannelInitializer, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._
import io.netty.handler.codec.http2.{Http2CodecUtil, Http2FrameCodecBuilder, Http2ServerUpgradeCodec}
import io.netty.handler.ssl.SslContext
import io.netty.handler.stream.ChunkedWriteHandler
import io.netty.util.AsciiString
import org.slf4j.LoggerFactory

class ServerInitializer(sslContext: Option[SslContext],
                        setup: Setup,
                        handler: HttpHandler
                      ) extends ChannelInitializer[AbstractChannel] {

  private val logger = LoggerFactory.getLogger(getClass)

  override def initChannel(ch: AbstractChannel): Unit = {
    logger.debug(s"http2: ${setup.mode.isH2}, ssl: ${sslContext.isDefined}")
    if (setup.mode.isH2) {
      sslContext match {
        case Some(ssl) =>
          configureSsl(ch, ssl)
        case _ =>
          configureClearText(ch)
      }
    } else {
      val p = ch.pipeline()
      sslContext.foreach { ssl =>
        p.addLast(ssl.newHandler(ch.alloc()))
      }
      p.addLast("codec", new HttpServerCodec())
      p.addLast("aggregator", new HttpObjectAggregator(setup.maxContentLength))
      p.addLast("omhs", handler)
      if (setup.enableCompression) {
        p.addLast("compressor", new HttpContentCompressor())
      }
    }
  }

  private def configureSsl(ch: AbstractChannel, ssl: SslContext): Unit = {
    ch.pipeline().addLast(
      ssl.newHandler(ch.alloc()),
      new HttpMixedHandler(handler, setup)
    )
  }

  private def configureClearText(ch: AbstractChannel): Unit = {
    val p = ch.pipeline()
    val codec = new HttpServerCodec()
    p.addLast(codec)
    p.addLast("aggregator", new HttpObjectAggregator(setup.maxContentLength))
    p.addLast(new HttpServerUpgradeHandler(codec, new HttpServerUpgradeHandler.UpgradeCodecFactory {
      override def newUpgradeCodec(protocol: CharSequence): HttpServerUpgradeHandler.UpgradeCodec = {
        println("$"*100 + s"--> ${protocol}")
        if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol)) {
          new Http2ServerUpgradeCodec(
            Http2FrameCodecBuilder.forServer().build(),
            new Http2MessageDecoder(setup.maxContentLength),
            handler
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
    if (setup.enableCompression) {
      p.addLast("compressor", new HttpContentCompressor())
    }
  }
}
