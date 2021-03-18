package com.github.fntz.omhs.handlers.http2

import com.github.fntz.omhs.handlers.TooLargeObject
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import io.netty.handler.codec.http2.{DefaultHttp2DataFrame, DefaultHttp2HeadersFrame, Http2Frame}

import java.util

/**
 * save header/data frames from Http2 and pass into pipeline
 */
class Http2MessageDecoder(maxContentLength: Int) extends MessageToMessageDecoder[Http2Frame] {

  private val current = new MutableHttp2Message()

  override def decode(ctx: ChannelHandlerContext, msg: Http2Frame, out: util.List[AnyRef]): Unit = {
    msg match {
      case x: DefaultHttp2HeadersFrame =>
        // content-length header is optional in HTTP/2
        Option(x.headers().get("content-length")).foreach { chars =>
          val tmp = chars.toString
          if (tmp.forall(Character.isDigit)) {
            if (tmp.toInt > maxContentLength) {
              out.add(TooLargeObject(x.stream()))
            }
          }
        }
        current.set(x)
        if (x.isEndStream) {
          out.add(current.toAggregated(x.stream()))
        }

      case x: DefaultHttp2DataFrame =>
        if (x.content().maxCapacity() > maxContentLength) {
          out.add(TooLargeObject(x.stream()))
        } else {
          current.set(x)
          if (x.isEndStream) {
            out.add(current.toAggregated(x.stream()))
          }
        }

      case _ =>
        out.add(msg)
    }
  }
}
