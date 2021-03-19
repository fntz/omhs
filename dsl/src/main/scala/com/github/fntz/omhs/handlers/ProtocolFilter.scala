package com.github.fntz.omhs.handlers

import com.github.fntz.omhs.Setup
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}
import io.netty.handler.codec.http.{FullHttpRequest, HttpMessage}
import io.netty.handler.codec.http2.Http2Frame

class ProtocolFilter(setup: Setup) extends ChannelInboundHandlerAdapter {

  override def channelRead(ctx: ChannelHandlerContext, msg: Object): Unit = {
    msg match {
      case request: HttpMessage if setup.isH2 =>
        println("======================")
        ctx.fireChannelRead(msg)
      case frame: Http2Frame if setup.isH1 =>
        println("~"*100)
        ctx.fireChannelRead(msg)

      case x =>
        println("@"*100 + s"${x.getClass}")
        ctx.fireChannelRead(msg)
    }
  }

}
