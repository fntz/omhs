package com.github.fntz.omhs.handlers.http2

import io.netty.buffer.Unpooled
import io.netty.handler.codec.http2.{DefaultHttp2DataFrame, DefaultHttp2HeadersFrame, EmptyHttp2Headers, Http2Frame, Http2FrameStream, Http2Headers}

class MutableHttp2Message {
  private var header: DefaultHttp2HeadersFrame =
    new DefaultHttp2HeadersFrame(EmptyHttp2Headers.INSTANCE)
  private var data: DefaultHttp2DataFrame =
    new DefaultHttp2DataFrame(Unpooled.EMPTY_BUFFER)

  def set(x: DefaultHttp2DataFrame): Unit = {
    data = x
  }

  def set(x: DefaultHttp2HeadersFrame): Unit = {
    header = x
  }

  def toAggregated(stream: Http2FrameStream): AggregatedHttp2Message = {
    AggregatedHttp2Message(
      stream = stream,
      data = data,
      headers = header
    )
  }
}
