import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpServerCodec}
import io.netty.handler.codec.http2.Http2SecurityUtil
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.handler.ssl.ApplicationProtocolConfig.{SelectedListenerFailureBehavior, SelectorFailureBehavior}
import io.netty.handler.ssl.util.SelfSignedCertificate
import io.netty.handler.ssl._

object HttpServer {

  def run(port: Int): Unit = {
    val ssl = Option(System.getProperty("ssl")).map { prop =>
      val provider =  SslProvider.JDK //SslProvider.isAlpnSupported(SslProvider.JDK) // ssl.jdk todo
      val ssc = new SelfSignedCertificate()
      val sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
        .sslProvider(provider)
        .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
        .applicationProtocolConfig(new ApplicationProtocolConfig(
          ApplicationProtocolConfig.Protocol.ALPN,
          SelectorFailureBehavior.NO_ADVERTISE,
          SelectedListenerFailureBehavior.ACCEPT,
          ApplicationProtocolNames.HTTP_2,
          ApplicationProtocolNames.HTTP_1_1
        )).build()
      sslCtx
    }

    val group = new NioEventLoopGroup()
    val b = new ServerBootstrap()
    b.group(group)
      .channel(classOf[NioServerSocketChannel])
      .handler(new LoggingHandler(LogLevel.INFO))
      .childHandler(new ChannelInitializer[SocketChannel] {
        override def initChannel(ch: SocketChannel): Unit = {
          ssl match {
            case Some(x) =>
              ch.pipeline()
                .addLast(x.newHandler(ch.alloc()), new Http2OrHttpHandler)
            case None =>
              ch.pipeline()
              .addLast(
                new HttpServerCodec(),
                new HttpObjectAggregator(512*1024),
                new CustomHttpHandler
              )
          }

        }
      })

    val f = b.bind(port).sync()
    f.channel().closeFuture().sync()
  }

}
