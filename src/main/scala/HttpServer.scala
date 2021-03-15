import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
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
      .childHandler(new Http2Initializer(ssl))
//      .childHandler(new ChannelInitializer[SocketChannel] {
//        override def initChannel(ch: SocketChannel): Unit = {
//          val p = ch.pipeline()
//          p.addLast("http2-initializer", new Http2Initializer(ssl))
////          ch.pipeline().addLast("codec", new HttpServerCodec())
////          ch.pipeline().addLast("aggregator",
////            new HttpObjectAggregator(512*1024))
////          p.addLast(new CustomHttpHandler)
//        }
//      })

    val f = b.bind(port).sync()
    f.channel().closeFuture().sync()
  }

}
