import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelDuplexHandler, ChannelHandlerContext, ChannelInboundHandlerAdapter}
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http2.{DefaultHttp2DataFrame, DefaultHttp2Headers, DefaultHttp2HeadersFrame, Http2DataFrame, Http2HeadersFrame}
import io.netty.util.{CharsetUtil, ReferenceCountUtil}

class Http2ServerHandler extends ChannelInboundHandlerAdapter {
  val empty = Unpooled
    .unreleasableBuffer(Unpooled.copiedBuffer("Hello World", CharsetUtil.UTF_8))

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
    msg match {
      case agg: AggregatedHttp2Message =>
        val content = ctx.alloc().buffer()
        content.writeBytes(empty.duplicate())
        val headers = new DefaultHttp2Headers().status(HttpResponseStatus.OK.codeAsText)
        ctx.write(new DefaultHttp2HeadersFrame(headers).stream(agg.stream))
        ctx.write(new DefaultHttp2DataFrame(content, true).stream(agg.stream))

      case _ =>
        // last step, I do not handle
        ReferenceCountUtil.release(msg)
    }
  }



  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    ctx.close() // todo
  }

}
