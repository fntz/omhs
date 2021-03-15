import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelDuplexHandler, ChannelHandlerContext}
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http2.{DefaultHttp2DataFrame, DefaultHttp2Headers, DefaultHttp2HeadersFrame, Http2HeadersFrame}
import io.netty.util.CharsetUtil

class Http2ServerHandler extends ChannelDuplexHandler {
  val empty = Unpooled
    .unreleasableBuffer(Unpooled.copiedBuffer("Hello World", CharsetUtil.UTF_8))

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
    msg match {
      case frame: Http2HeadersFrame =>
        val content = ctx.alloc().buffer()
        content.writeBytes(empty.duplicate())

        val headers = new DefaultHttp2Headers().status(HttpResponseStatus.OK.codeAsText)
        ctx.write(new DefaultHttp2HeadersFrame(headers).stream(frame.stream))
        ctx.write(new DefaultHttp2DataFrame(content, true).stream(frame.stream))

      case _ => super.channelRead(ctx, msg)
    }

  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    ctx.close() // todo
  }

}
