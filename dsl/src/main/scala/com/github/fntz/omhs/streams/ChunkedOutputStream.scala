package com.github.fntz.omhs.streams

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.{DefaultHttpContent, HttpResponseStatus}
import io.netty.handler.codec.http2.{DefaultHttp2DataFrame, DefaultHttp2Headers, DefaultHttp2HeadersFrame, Http2FrameStream}
import io.netty.util.CharsetUtil

import java.io.OutputStream

// based on: https://gist.github.com/codingtony/6564901
case class ChunkedOutputStream(private val context: ChannelHandlerContext,
                               private val chunkSize: Int,
                               private val http2FrameStream: Option[Http2FrameStream]
                              ) extends OutputStream {

  private val buffer = Unpooled.buffer(0, chunkSize)

  override def write(b: Int): Unit = {
    if (buffer.maxWritableBytes() < 1) {
      flush()
    }
    buffer.writeByte(b)
  }

  def <<(b: Array[Byte]): ChunkedOutputStream = {
    write(b)
    this
  }

  def <<(b: String): ChunkedOutputStream = {
    write(b.getBytes(CharsetUtil.UTF_8))
    this
  }

  override def write(b: Array[Byte], off: Int, len: Int): Unit = {
    var dataLengthLeftToWrite = len
    var dataToWriteOffset = off
    var spaceLeftInCurrentChunk = 0
    while ({
      spaceLeftInCurrentChunk = buffer.maxWritableBytes
      spaceLeftInCurrentChunk
    } < dataLengthLeftToWrite) {
      buffer.writeBytes(b, dataToWriteOffset, spaceLeftInCurrentChunk)
      dataToWriteOffset = dataToWriteOffset + spaceLeftInCurrentChunk
      dataLengthLeftToWrite = dataLengthLeftToWrite - spaceLeftInCurrentChunk
      flush()
    }

    if (dataLengthLeftToWrite > 0) {
      buffer.writeBytes(b, dataToWriteOffset, dataLengthLeftToWrite)
    }
  }

  override def close(): Unit = {
    flush()
    super.close()
  }

  override def flush(): Unit = {
    http2FrameStream match {
      case Some(h2Stream) =>
        context.writeAndFlush(new DefaultHttp2DataFrame(buffer.copy(), false).stream(h2Stream))
      case None =>
        context.writeAndFlush(new DefaultHttpContent(buffer.copy()))
    }
    buffer.clear()
    super.flush()
  }

}
