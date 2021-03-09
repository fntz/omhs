package com.github.fntz.omhs

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{ChannelFuture, ChannelInitializer, ChannelPipeline}
import io.netty.handler.codec.http.{HttpContentCompressor, HttpObjectAggregator, HttpServerCodec}
import io.netty.handler.stream.ChunkedWriteHandler

import java.net.InetSocketAddress

object OHMSServer {

  type C2C = ChannelPipeline => ChannelPipeline
  type S2S = ServerBootstrap => ServerBootstrap

  def noSetup: S2S = (s: ServerBootstrap) => s
  def noPipelineChanges: C2C = (c: ChannelPipeline) => c


  def run(address: InetSocketAddress,
          handler: DefaultHttpHandler,
          beforeHandlers: C2C,
          modifier: S2S
         ): ChannelFuture = {
    val boss = new NioEventLoopGroup()
    val worker = new NioEventLoopGroup()
    val b = new ServerBootstrap()
    b.group(boss, worker)
      .channel(classOf[NioServerSocketChannel])
      .childHandler(new ChannelInitializer[SocketChannel] {
        override def initChannel(ch: SocketChannel): Unit = {
          val p = ch.pipeline()
          p.addLast("codec", new HttpServerCodec())
          p.addLast("aggregator",
            new HttpObjectAggregator(512*1024))

          beforeHandlers(p)

          p.addLast("compressor", new HttpContentCompressor())
          p.addLast("chunked", new ChunkedWriteHandler) // todo check chunks without this
          p.addLast("omhs", handler)
        }
    })


    val f = modifier(b).bind(address).sync()
    f.channel().closeFuture().sync()
  }

  def run(host: String, port: Int,
          handler: DefaultHttpHandler,
          beforeHandlers: C2C,
          modifier: S2S
         ): ChannelFuture = {
    run(
      address = new InetSocketAddress(host, port),
      handler = handler,
      beforeHandlers = beforeHandlers,
      modifier = modifier
    )
  }

  def run(port: Int,
          handler: DefaultHttpHandler,
          beforeHandlers: C2C = (c: ChannelPipeline) => c,
          modifier: S2S = (b: ServerBootstrap) => b
         ): ChannelFuture = {
    run(
      address = new InetSocketAddress("127.0.0.1", port),
      handler = handler,
      beforeHandlers = beforeHandlers,
      modifier = modifier
    )
  }

}