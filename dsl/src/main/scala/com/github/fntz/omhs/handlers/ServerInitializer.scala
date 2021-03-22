package com.github.fntz.omhs.handlers

import com.github.fntz.omhs.Setup
import com.github.fntz.omhs.handlers.http2.Http2MessageDecoder
import io.netty.channel.{AbstractChannel, ChannelHandlerContext, ChannelInitializer, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._
import io.netty.handler.codec.http2.{CleartextHttp2ServerUpgradeHandler, Http2CodecUtil, Http2FrameCodecBuilder, Http2ServerUpgradeCodec}
import io.netty.handler.ssl.SslContext
import io.netty.util.AsciiString
import org.slf4j.LoggerFactory

class ServerInitializer(sslContext: Option[SslContext],
                        setup: Setup,
                        handler: HttpHandler
                      ) extends ChannelInitializer[AbstractChannel] {

  import ServerInitializer._
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
        p.addLast(Ssl, ssl.newHandler(ch.alloc()))
      }
      p.addLast(Codec, new HttpServerCodec())
      p.addLast(Aggregator, new HttpObjectAggregator(setup.maxContentLength))
      p.addLast(Omhs, handler)
      if (setup.enableCompression) {
        p.addLast(Compressor, new HttpContentCompressor())
      }
    }
  }

  private def configureSsl(ch: AbstractChannel, ssl: SslContext): Unit = {
    ch.pipeline().addLast(Ssl, ssl.newHandler(ch.alloc()))
      .addLast(Mixed, new HttpMixedHandler(handler, setup))
  }

  private def configureClearText(ch: AbstractChannel): Unit = {
    val p = ch.pipeline()
    val codec = new HttpServerCodec()
    val upgradeFactory = new HttpServerUpgradeHandler.UpgradeCodecFactory {
      override def newUpgradeCodec(protocol: CharSequence): HttpServerUpgradeHandler.UpgradeCodec = {
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
    }

    val upgradeHandler = new HttpServerUpgradeHandler(codec, upgradeFactory, setup.maxContentLength)

    p.addLast(new CleartextHttp2ServerUpgradeHandler(codec, upgradeHandler,
      new HttpMixedHandler(handler, setup)))

    p.addLast(new SimpleChannelInboundHandler[HttpMessage] {
      override def channelRead0(ctx: ChannelHandlerContext, msg: HttpMessage): Unit = {
        val p = ctx.pipeline()
        val current = p.context(this)
        p.remove(upgradeHandler)
        p.addAfter(current.name(), null, handler)
        p.replace(this, Aggregator, new HttpObjectAggregator(setup.maxContentLength))
        if (setup.enableCompression) {
          p.addLast(Compressor, new HttpContentCompressor())
        }
        ctx.fireChannelRead(msg)
      }
    })

  }
}

object ServerInitializer {
  val Ssl = "ssl"
  val Aggregator = "aggregator"
  val Compressor = "compressor"
  val Codec = "codec"
  val Omhs = "omhs"
  val Codec2 = "codec2"
  val Decoder = "decoder"
  val Mixed = "mixed"
}
