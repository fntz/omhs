package com.github.fntz.omhs

import com.github.fntz.omhs.handlers.{HttpHandler, ServerInitializer}
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{ChannelFuture, ChannelInitializer, ChannelPipeline}
import io.netty.handler.codec.http2.Http2SecurityUtil
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.handler.ssl.ApplicationProtocolConfig.{SelectedListenerFailureBehavior, SelectorFailureBehavior}
import io.netty.handler.ssl.util.SelfSignedCertificate
import io.netty.handler.ssl._
import org.slf4j.LoggerFactory

import java.net.InetSocketAddress

object OMHSServer {

  type S2S = ServerBootstrap => ServerBootstrap

  def noServerBootstrapChanges: S2S = (s: ServerBootstrap) => s

  def getJdkSslContext: SslContext = {
    getSslContext(SslProvider.JDK)
  }

  def getOpenSslSslContext: SslContext = {
    getSslContext(SslProvider.OPENSSL)
  }

  def getSslContext(provider: SslProvider): SslContext  = {
    val cert = new SelfSignedCertificate()
    SslContextBuilder.forServer(cert.certificate(), cert.privateKey())
      .sslProvider(provider)
      .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
      .applicationProtocolConfig(new ApplicationProtocolConfig(
        ApplicationProtocolConfig.Protocol.ALPN,
        SelectorFailureBehavior.NO_ADVERTISE,
        SelectedListenerFailureBehavior.ACCEPT,
        ApplicationProtocolNames.HTTP_2,
        ApplicationProtocolNames.HTTP_1_1
      )).build()
  }
  // todo runNow and run diff (block thread)
  def run(address: InetSocketAddress,
          handler: HttpHandler,
          sslContext: Option[SslContext],
          serverBootstrapChanges: S2S
         ): ChannelFuture = {
    val setup = handler.setup
    val boss = new NioEventLoopGroup()
    val worker = new NioEventLoopGroup()
    val b = new ServerBootstrap()
    try {
      b.group(boss, worker)
        .channel(classOf[NioServerSocketChannel])
        .childHandler(new ChannelInitializer[SocketChannel] {
          override def initChannel(ch: SocketChannel): Unit = {
            ch.pipeline()
              .addLast(new ServerInitializer(sslContext, setup, handler))
          }
        })
      val f = serverBootstrapChanges(b).bind(address).sync()
      f.channel().closeFuture().sync()
    } finally {
      worker.shutdownGracefully()
      boss.shutdownGracefully()
    }
  }

  def run(host: String, port: Int,
          handler: HttpHandler,
          sslContext: Option[SslContext],
          serverBootstrapChanges: S2S
         ): ChannelFuture = {
    run(
      address = new InetSocketAddress(host, port),
      handler = handler,
      sslContext = sslContext,
      serverBootstrapChanges = serverBootstrapChanges
    )
  }

  def run(port: Int,
          handler: HttpHandler,
          sslContext: Option[SslContext],
          serverBootstrapChanges: S2S = noServerBootstrapChanges
         ): ChannelFuture = {
    run(
      address = new InetSocketAddress("127.0.0.1", port),
      handler = handler,
      sslContext = sslContext,
      serverBootstrapChanges = serverBootstrapChanges
    )
  }

}
