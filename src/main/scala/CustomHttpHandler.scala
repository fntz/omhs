import io.netty.buffer.{ByteBuf, ByteBufAllocator, Unpooled}
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, ChannelInboundHandlerAdapter, ChannelProgressiveFuture, ChannelProgressiveFutureListener, DefaultFileRegion}
import io.netty.handler.codec.http._
import io.netty.handler.stream.{ChunkedFile, ChunkedInput}
import io.netty.util.CharsetUtil
import io.netty.util.concurrent.{Future, GenericFutureListener}

import java.io.{File, RandomAccessFile}

class CustomHttpHandler extends ChannelInboundHandlerAdapter {

  override def channelRead(ctx: ChannelHandlerContext, msg: Object): Unit = {
    msg match {
      case request: FullHttpRequest =>
        val message = "test"

        val isKeepAlive = HttpUtil.isKeepAlive(request)
        val response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK)
        response.headers()
          .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
          .set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED)

        processKeepAlive(isKeepAlive, request, response)

        ctx.write(response) // empty

        (0 to 10).foreach { x =>
          ctx.write(new DefaultHttpContent(Unpooled.copiedBuffer(s"$x hello there", CharsetUtil.UTF_8)))
          if (x % 3 == 0) {
            ctx.flush()
          }
        }

        val f = ctx.write(LastHttpContent.EMPTY_LAST_CONTENT)
        if (!isKeepAlive) {
          f.addListener(ChannelFutureListener.CLOSE)
        }

      case _ =>
        super.channelRead(ctx, msg)
    }
  }

  def processKeepAlive(isKeepAlive: Boolean, request: FullHttpRequest, response: HttpResponse): Unit = {
    if (isKeepAlive) {
      if (!request.protocolVersion.isKeepAliveDefault) {
        response.headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
      }
    } else {
      response.headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
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
