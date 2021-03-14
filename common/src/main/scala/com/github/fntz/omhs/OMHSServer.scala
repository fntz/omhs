package com.github.fntz.omhs

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{ChannelFuture, ChannelInitializer, ChannelPipeline}
import io.netty.handler.codec.http.{HttpContentCompressor, HttpObjectAggregator, HttpServerCodec}
import io.netty.handler.stream.ChunkedWriteHandler

import java.net.InetSocketAddress

object OMHSServer {

  type C2C = ChannelPipeline => ChannelPipeline
  type S2S = ServerBootstrap => ServerBootstrap

  def noServerBootstrapChanges: S2S = (s: ServerBootstrap) => s
  def noPipelineChanges: C2C = (c: ChannelPipeline) => c

  def run(address: InetSocketAddress,
          handler: OMHSHttpHandler,
          pipeLineChanges: C2C,
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
            val p = ch.pipeline()
            p.addLast("codec", new HttpServerCodec())
            p.addLast("aggregator", new HttpObjectAggregator(setup.maxContentLength))

            pipeLineChanges(p)

            if (setup.enableCompression) {
              p.addLast("compressor", new HttpContentCompressor())
            }
            p.addLast("omhs", handler)
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
          handler: OMHSHttpHandler,
          pipeLineChanges: C2C,
          serverBootstrapChanges: S2S
         ): ChannelFuture = {
    run(
      address = new InetSocketAddress(host, port),
      handler = handler,
      pipeLineChanges = pipeLineChanges,
      serverBootstrapChanges = serverBootstrapChanges
    )
  }

  def run(port: Int,
          handler: OMHSHttpHandler,
          pipeLineChanges: C2C = noPipelineChanges,
          serverBootstrapChanges: S2S = noServerBootstrapChanges
         ): ChannelFuture = {
    run(
      address = new InetSocketAddress("127.0.0.1", port),
      handler = handler,
      pipeLineChanges = pipeLineChanges,
      serverBootstrapChanges = serverBootstrapChanges
    )
  }

}
