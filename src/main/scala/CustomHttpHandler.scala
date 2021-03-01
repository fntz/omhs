import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.HttpResponseStatus.{BAD_REQUEST, OK}
import io.netty.handler.codec.http._
import io.netty.util.CharsetUtil

class CustomHttpHandler extends SimpleChannelInboundHandler[Object] {

  private var request: HttpRequest = null
  private val responseData: StringBuilder = new StringBuilder()

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
    ctx.flush()
  }

  private def writeResponse(ctx: ChannelHandlerContext): Unit = {
    val response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
      HttpResponseStatus.CONTINUE, Unpooled.EMPTY_BUFFER)
    ctx.write(response)
  }

  override def channelRead0(ctx: ChannelHandlerContext, msg: Object): Unit = {
    msg match {
      case req: HttpRequest =>
        request = req
        if (HttpUtil.is100ContinueExpected(request)) {
          writeResponse(ctx)
        }

        responseData.setLength(0)
        responseData.append(RequestUtils.formatParams(request))

      case cont: HttpContent =>
        responseData.append(RequestUtils.formatBody(cont))
        responseData.append(RequestUtils.evaluateDecoderResult(request))

        msg match {
          case l: LastHttpContent =>
            responseData.append(RequestUtils.prepareLastResponse(request, l))
            writeResponse(ctx, l, responseData)

          case _ =>
        }

      case _ =>
    }

  }

  private def writeResponse(ctx: ChannelHandlerContext, trailer: LastHttpContent, responseData: StringBuilder): Unit = {
    val keepAlive = HttpUtil.isKeepAlive(request)
    val status = if (trailer.asInstanceOf[HttpObject].decoderResult().isSuccess) {
      OK
    } else {
      BAD_REQUEST
    }
    val httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
      Unpooled.copiedBuffer(responseData.toString, CharsetUtil.UTF_8))

    httpResponse.headers.set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")

    if (keepAlive) {
      httpResponse.headers.setInt(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content.readableBytes)
      httpResponse.headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
    }
    ctx.write(httpResponse)
    if (!keepAlive) ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause.printStackTrace()
    ctx.close()
  }
}
