import io.netty.channel.{ChannelDuplexHandler, ChannelHandlerContext}
import io.netty.handler.codec.http2.Http2HeadersFrame
import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http2.DefaultHttp2DataFrame
import io.netty.handler.codec.http2.DefaultHttp2Headers
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame
import io.netty.handler.codec.http2.Http2Headers
import io.netty.handler.ssl.SslHandshakeCompletionEvent
import io.netty.util.CharsetUtil


class Http2HandlerX extends ChannelDuplexHandler {

  val empty = Unpooled
    .unreleasableBuffer(Unpooled.copiedBuffer("Hello World", CharsetUtil.UTF_8))

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
    println("~"*100)
    msg match {
      case frame: Http2HeadersFrame =>
        if (frame.isEndStream) {
          val content = ctx.alloc().buffer()
          content.writeBytes(empty.duplicate())

          val headers = new DefaultHttp2Headers().status(HttpResponseStatus.OK.codeAsText)
          ctx.write(new DefaultHttp2HeadersFrame(headers).stream(frame.stream))
          ctx.write(new DefaultHttp2DataFrame(content, true).stream(frame.stream))
        }

      case x =>
        println(s"====>? ${x.getClass}")
        super.channelRead(ctx, msg)
    }
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    println("@"*100)
    super.exceptionCaught(ctx, cause)
    cause.printStackTrace
    ctx.close()
  }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
    ctx.flush()
  }

}
