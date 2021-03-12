import io.netty.buffer.{ByteBuf, ByteBufAllocator, Unpooled}
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, ChannelInboundHandlerAdapter, ChannelProgressiveFuture, ChannelProgressiveFutureListener, DefaultFileRegion}
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType
import io.netty.handler.codec.http.multipart.{DefaultHttpDataFactory, FileUpload, HttpPostRequestDecoder, MixedFileUpload}
import io.netty.handler.stream.{ChunkedFile, ChunkedInput}
import io.netty.util.CharsetUtil
import io.netty.util.concurrent.{Future, GenericFutureListener}

import java.io.{File, RandomAccessFile}

class CustomHttpHandler extends ChannelInboundHandlerAdapter {

  override def channelRead(ctx: ChannelHandlerContext, msg: Object): Unit = {
    msg match {
      case request: FullHttpRequest =>

        // if method is post
        val httpDecoder = new HttpPostRequestDecoder(request)

        try {
          import scala.collection.JavaConverters._
          println(s"===> ${request.decoderResult().isSuccess}")
          httpDecoder.getBodyHttpDatas.asScala.foreach { x =>
            if (x.getHttpDataType == HttpDataType.Attribute) {
              println(s"data: ${x.getName} -> ${x}")
            } else if (x.getHttpDataType == HttpDataType.FileUpload) {
              val m = x.asInstanceOf[MixedFileUpload]
              println(s"${m.getFilename} and ${m.getName}")
              println("---> ")
            }
          }

        } catch {
          case ex: Throwable =>
            println(s"--------> ${ex}")
        }

        println("$"*100)
        ctx.writeAndFlush(
          new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.OK,
            Unpooled.copiedBuffer("test".getBytes()))
        ).addListener(ChannelFutureListener.CLOSE)


      case _ =>
        super.channelRead(ctx, msg)
    }
  }

  private def uploading(ctx: ChannelHandlerContext, request: FullHttpRequest): Unit = {
    val httpDecoder = new HttpPostRequestDecoder(request)

    try {
        import scala.collection.JavaConverters._
        httpDecoder.getBodyHttpDatas.asScala.foreach { x =>
          if (x.getHttpDataType == HttpDataType.Attribute) {
            println(s"data: ${x.getName} -> ${x}")
          } else if (x.getHttpDataType == HttpDataType.FileUpload) {
            val m = x.asInstanceOf[MixedFileUpload]
            println(s"${m.getFilename} and ${m.getName}")
            println("---> ")
          }
        }
        println("~"*100)

    } catch {
      case ex: Throwable =>
        println(s"--------> ${ex}")
    }
  }

  // streaming
  def chunks(ctx: ChannelHandlerContext, request: FullHttpRequest) = {
    val message = "test"

    val isKeepAlive = HttpUtil.isKeepAlive(request)
    val response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK)
    response.headers()
      .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
      .set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED)

    processKeepAlive(isKeepAlive, request, response)

    ctx.write(response) // empty

    (0 to 10).foreach { x =>
      ctx.write(new DefaultHttpContent(
        Unpooled.copiedBuffer(s"$x hello there", CharsetUtil.UTF_8)))
      if (x % 3 == 0) {
        ctx.flush()
      }
    }

    val f = ctx.write(LastHttpContent.EMPTY_LAST_CONTENT)
    if (!isKeepAlive) {
      f.addListener(ChannelFutureListener.CLOSE)
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
