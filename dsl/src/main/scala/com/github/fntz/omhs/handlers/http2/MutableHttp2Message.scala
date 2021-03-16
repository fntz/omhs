package com.github.fntz.omhs.handlers.http2

import io.netty.handler.codec.http2.{DefaultHttp2DataFrame, DefaultHttp2HeadersFrame, Http2Frame, Http2FrameStream}

import scala.collection.mutable.{ArrayBuffer => AB}

class MutableHttp2Message {
  private var headers = AB[DefaultHttp2HeadersFrame]()
  private var datas = AB[DefaultHttp2DataFrame]()

  def isEmpty = headers.isEmpty && datas.isEmpty

  def pushData(x: DefaultHttp2DataFrame) = {
    datas += x
  }

  def pushHeader(x: DefaultHttp2HeadersFrame) = {
    headers += x
  }

  def toAggregated(stream: Http2FrameStream): AggregatedHttp2Message = {
    AggregatedHttp2Message(
      stream = stream,
      data = datas.toVector,
      headers = headers.toVector
    )
  }
}
