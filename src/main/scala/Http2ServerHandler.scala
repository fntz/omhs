import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelDuplexHandler, ChannelHandlerContext, ChannelInboundHandlerAdapter}
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType
import io.netty.handler.codec.http.multipart.{HttpPostRequestDecoder, MixedFileUpload}
import io.netty.handler.codec.http2.{DefaultHttp2DataFrame, DefaultHttp2Headers, DefaultHttp2HeadersFrame, Http2DataFrame, Http2HeadersFrame, HttpConversionUtil, InboundHttp2ToHttpAdapter}
import io.netty.util.{CharsetUtil, ReferenceCountUtil}

class Http2ServerHandler extends ChannelInboundHandlerAdapter {
  val empty = Unpooled
    .unreleasableBuffer(Unpooled.copiedBuffer("Hello World", CharsetUtil.UTF_8))

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
    msg match {
      case agg: AggregatedHttp2Message =>
        val request = HttpConversionUtil.toFullHttpRequest(
          agg.streamId,
          agg.headers.headers(),
          agg.data.content(),
          true
        )
        import scala.collection.JavaConverters._
        val decoder = new HttpPostRequestDecoder(request)
        val files = decoder.getBodyHttpDatas.asScala.collect {
          case data: MixedFileUpload if data.getHttpDataType == HttpDataType.FileUpload =>
            data.copy()
        }.toList
        println("@"*100)
        println(files.map(_.getName))


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
