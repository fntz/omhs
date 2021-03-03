import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}
import io.netty.handler.codec.http._

class CustomHttpHandler extends ChannelInboundHandlerAdapter {

  override def channelRead(ctx: ChannelHandlerContext, msg: Object): Unit = {
    msg match {
      case request: FullHttpRequest =>
        val message = "test"

        val dec = new QueryStringDecoder(request.uri)

        val response = new DefaultFullHttpResponse(
          HttpVersion.HTTP_1_1,
          HttpResponseStatus.OK,
          Unpooled.copiedBuffer(message.getBytes)
        )

        if (HttpUtil.isKeepAlive(request))
        {
          response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content.readableBytes)
          response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
        }

        response.headers.set(HttpHeaderNames.CONTENT_TYPE, "text/plain")
        response.headers.set(HttpHeaderNames.CONTENT_LENGTH, message.length)

        ctx.writeAndFlush(response)

      case _ =>
        super.channelRead(ctx, msg)
    }
  }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
    ctx.flush()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    ctx.writeAndFlush(new DefaultFullHttpResponse(
      HttpVersion.HTTP_1_1,
      HttpResponseStatus.INTERNAL_SERVER_ERROR,
      Unpooled.copiedBuffer(cause.getMessage.getBytes())
    ))
  }
}
