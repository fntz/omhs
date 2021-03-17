package com.github.fntz.omhs.handlers.http2

import io.netty.handler.codec.http2.{DefaultHttp2DataFrame, DefaultHttp2HeadersFrame, Http2FrameStream}

case class AggregatedHttp2Message(
                                 stream: Http2FrameStream,
                                 data: DefaultHttp2DataFrame,
                                 headers: DefaultHttp2HeadersFrame
                                 )
