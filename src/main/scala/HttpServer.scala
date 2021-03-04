import com.github.fntz.omhs.DefaultHttpHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.{HttpContentCompressor, HttpObjectAggregator, HttpRequestDecoder, HttpResponseEncoder, HttpServerCodec}
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.handler.stream.ChunkedWriteHandler

object DefaultServer {
  def run(port: Int, handler: DefaultHttpHandler): Unit = {
    val group = new NioEventLoopGroup()
    val b = new ServerBootstrap()
    b.group(group)
      .channel(classOf[NioServerSocketChannel])
      .handler(new LoggingHandler(LogLevel.INFO))
      .childHandler(new ChannelInitializer[SocketChannel] {
        override def initChannel(ch: SocketChannel): Unit = {
          val p = ch.pipeline()
          ch.pipeline().addLast("codec", new HttpServerCodec())
          ch.pipeline().addLast("aggregator",
            new HttpObjectAggregator(512*1024))
//          ch.pipeline().addLast("compressor",    new HttpContentCompressor())
          ch.pipeline().addLast(new ChunkedWriteHandler)
          p.addLast(handler)
        }
      })

    val f = b.bind(port).sync()
    f.channel().closeFuture().sync()
  }
}

class HttpServer {

  def run(port: Int): Unit = {
    val group = new NioEventLoopGroup()
    val b = new ServerBootstrap()
    b.group(group)
      .channel(classOf[NioServerSocketChannel])
      .handler(new LoggingHandler(LogLevel.INFO))
      .childHandler(new ChannelInitializer[SocketChannel] {
        override def initChannel(ch: SocketChannel): Unit = {
          val p = ch.pipeline()
          ch.pipeline().addLast("codec", new HttpServerCodec())
          ch.pipeline().addLast("aggregator",
            new HttpObjectAggregator(512*1024))
          p.addLast(new CustomHttpHandler)
        }
      })

    val f = b.bind(port).sync()
    f.channel().closeFuture().sync()
  }

}
