import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._
import io.netty.util.CharsetUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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

        Future.successful(1).map { _ =>
          ctx.writeAndFlush(response)
        }

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
