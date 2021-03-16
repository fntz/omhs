package com.github.fntz.omhs.handlers.http2

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import io.netty.handler.codec.http2.{DefaultHttp2DataFrame, DefaultHttp2HeadersFrame, Http2Frame}

import java.util

/**
 * save header/data frames from Http2 and pass into pipeline
 */
class Http2MessageDecoder extends MessageToMessageDecoder[Http2Frame] {

  private val current = new MutableHttp2Message()

  override def decode(ctx: ChannelHandlerContext, msg: Http2Frame, out: util.List[AnyRef]): Unit = {
    msg match {
      case x: DefaultHttp2HeadersFrame =>
        current.pushHeader(x)
        if (x.isEndStream) {
          out.add(current.toAggregated(x.stream()))
        }

      case x: DefaultHttp2DataFrame =>
        current.pushData(x)
        if (x.isEndStream) {
          out.add(current.toAggregated(x.stream()))
        }

      case _ =>
        out.add(msg)
    }
  }
}
