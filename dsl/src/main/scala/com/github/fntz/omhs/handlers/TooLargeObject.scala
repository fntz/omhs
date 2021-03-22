package com.github.fntz.omhs.handlers

import io.netty.handler.codec.http2.Http2FrameStream

case class TooLargeObject(stream: Http2FrameStream)
