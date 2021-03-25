package com.github.fntz.omhs

import com.github.fntz.omhs.handlers.{HttpHandler, ServerInitializer}
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{ChannelFuture, ChannelInitializer}
import io.netty.handler.codec.http2.Http2SecurityUtil
import io.netty.handler.ssl.ApplicationProtocolConfig.{SelectedListenerFailureBehavior, SelectorFailureBehavior}
import io.netty.handler.ssl._
import io.netty.handler.ssl.util.SelfSignedCertificate
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

  class Instance(address: InetSocketAddress,
                 handler: HttpHandler,
                 sslContext: Option[SslContext],
                 serverBootstrapChanges: S2S) {

    private val logger = LoggerFactory.getLogger(getClass)

    private val setup = handler.setup
    private val boss = new NioEventLoopGroup()
    private val worker = new NioEventLoopGroup()
    private var future: ChannelFuture  = _

    def start(): Unit = {
      try {
        val b = new ServerBootstrap()
        b.group(boss, worker)
          .channel(classOf[NioServerSocketChannel])
          .childHandler(new ChannelInitializer[SocketChannel] {
            override def initChannel(ch: SocketChannel): Unit = {
              ch.pipeline().addLast(
                new ServerInitializer(sslContext, setup, handler)
              )
            }
          })
        future = serverBootstrapChanges(b).bind(address).sync()
        logger.debug(s"OMHS server started on $address")
      } catch {
        case ex:  Throwable =>
          logger.warn(s"Failed to start server: on $address", ex)
      }
    }

    def stop(): Unit = {
      try {
        worker.shutdownGracefully().sync()
        boss.shutdownGracefully().sync()
        future.channel().closeFuture().sync()
        logger.debug("OMHS server stopped")
      } catch {
        case ex: Throwable =>
          logger.warn(s"Fail in stop server on $address", ex)
      }
    }
  }

  def init(address: InetSocketAddress,
           handler: HttpHandler,
           sslContext: Option[SslContext],
           serverBootstrapChanges: S2S
         ): Instance = {
    new Instance(
      address = address,
      handler = handler,
      sslContext = sslContext,
      serverBootstrapChanges = serverBootstrapChanges
    )
  }

  def init(host: String, port: Int,
           handler: HttpHandler,
           sslContext: Option[SslContext],
           serverBootstrapChanges: S2S
         ): Instance = {
    init(
      address = new InetSocketAddress(host, port),
      handler = handler,
      sslContext = sslContext,
      serverBootstrapChanges = serverBootstrapChanges
    )
  }

  def init(port: Int,
          handler: HttpHandler,
          sslContext: Option[SslContext],
          serverBootstrapChanges: S2S = noServerBootstrapChanges
         ): Instance = {
    init(
      address = new InetSocketAddress("127.0.0.1", port),
      handler = handler,
      sslContext = sslContext,
      serverBootstrapChanges = serverBootstrapChanges
    )
  }

}
