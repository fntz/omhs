import io.netty.buffer.Unpooled.{copiedBuffer, unreleasableBuffer}
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http._
import io.netty.handler.codec.http2._
import io.netty.buffer.{ByteBuf, ByteBufUtil}
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http2.{DefaultHttp2Headers, Http2Headers}

class Http2Handler(
                  decoder: Http2ConnectionDecoder,
                  encoder: Http2ConnectionEncoder,
                  settings: Http2Settings
                  ) extends Http2ConnectionHandler(decoder, encoder, settings)
    with Http2FrameListener {

  import Http2Handler._

  override def userEventTriggered(ctx: ChannelHandlerContext, evt: Any): Unit = {
    println("$"*100)
    evt match {
      case upgrade: HttpServerUpgradeHandler.UpgradeEvent =>
        onHeadersRead(ctx, 1, http1Headers2Http2Headers(upgrade.upgradeRequest()), 0, true)
      case _ =>
    }
    super.userEventTriggered(ctx, evt)
  }

  private def sendResponse(ctx: ChannelHandlerContext, streamId: Int, payload: ByteBuf): Unit = { // Send a frame for the response status
    println("^"*100)
    val headers = new DefaultHttp2Headers().status(HttpResponseStatus.OK.codeAsText)
    encoder.writeHeaders(ctx, streamId, headers, 0, false, ctx.newPromise)
    encoder.writeData(ctx, streamId, payload, 0, true, ctx.newPromise)
  }

  override def onDataRead(ctx: ChannelHandlerContext, streamId: Int, data: ByteBuf, padding: Int, endOfStream: Boolean): Int = {
    val processed = data.readableBytes + padding
    if (endOfStream) {
      sendResponse(ctx, streamId, data.retain)
    }
    processed
  }

  override def onHeadersRead(ctx: ChannelHandlerContext, streamId: Int, headers: Http2Headers, padding: Int, endOfStream: Boolean): Unit = {
    if (endOfStream) {
      val content = ctx.alloc.buffer
      content.writeBytes(RESPONSE_BYTES.duplicate)
      ByteBufUtil.writeAscii(content, " - via HTTP/2")
      sendResponse(ctx, streamId, content)
    }
  }

  override def onHeadersRead(ctx: ChannelHandlerContext, streamId: Int,
                    headers: Http2Headers, streamDependency: Int, weight: Short, exclusive: Boolean, padding: Int, endOfStream: Boolean): Unit = {
    onHeadersRead(ctx, streamId, headers, padding, endOfStream)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) = {
    println("~"*100)
    super.exceptionCaught(ctx, cause)
    cause.printStackTrace()
    ctx.close()
  }

  override def onPriorityRead(ctx: ChannelHandlerContext, streamId: Int, streamDependency: Int, weight: Short, exclusive: Boolean): Unit = {}

  override def onRstStreamRead(ctx: ChannelHandlerContext, streamId: Int, errorCode: Long): Unit = {}

  override def onSettingsAckRead(ctx: ChannelHandlerContext): Unit = {}

  override def onSettingsRead(ctx: ChannelHandlerContext, settings: Http2Settings): Unit = {}

  override def onPingRead(ctx: ChannelHandlerContext, data: Long): Unit = {}

  override def onPingAckRead(ctx: ChannelHandlerContext, data: Long): Unit = {}

  override def onPushPromiseRead(ctx: ChannelHandlerContext, streamId: Int, promisedStreamId: Int, headers: Http2Headers, padding: Int): Unit = {}

  override def onGoAwayRead(ctx: ChannelHandlerContext, lastStreamId: Int, errorCode: Long, debugData: ByteBuf): Unit = {}

  override def onWindowUpdateRead(ctx: ChannelHandlerContext, streamId: Int, windowSizeIncrement: Int): Unit = {}

  override def onUnknownFrame(ctx: ChannelHandlerContext, frameType: Byte, streamId: Int, flags: Http2Flags, payload: ByteBuf): Unit = ???
}

object Http2Handler {

  import io.netty.buffer.ByteBuf
  import io.netty.util.CharsetUtil

  val RESPONSE_BYTES: ByteBuf = unreleasableBuffer(copiedBuffer("", CharsetUtil.UTF_8))

  def http1Headers2Http2Headers(request: FullHttpRequest): Http2Headers = {
    val headers = new DefaultHttp2Headers()
      .method(HttpMethod.GET.asciiName())
      .path(request.uri())
      .scheme(HttpScheme.HTTP.name())
    Option(request.headers().get(HttpHeaderNames.HOST)).map { host =>
      headers.authority(host)
    }.getOrElse(headers)
  }
}
