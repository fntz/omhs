package com.github.fntz.omhs

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._
import io.netty.util.CharsetUtil

@Sharable
class DefaultHttpHandler(final val route: Route) extends ChannelInboundHandlerAdapter {

  private val rules = route.currentF

  override def channelRead(ctx: ChannelHandlerContext, msg: Object): Unit = {
    msg match {
      case request: FullHttpRequest =>
        val message = "test"
        println(request.method())
        val decoder = new QueryStringDecoder(request.uri)
        val target = decoder.rawPath()
        val result = rules.map(x => (x, Param.parse(target, x.rule.params)))

        val first = result.find(_._2.isSuccess)

        first match {
          case Some((r, ParseResult(_, defs))) =>
            val real = defs.filterNot(_.skip)
            r match {
              case RuleAndF0(rule, func) =>
                println("0")
              case RuleAndF1(rule, func) =>
                println("1")
              case RuleAndF2(rule, func) =>
                println("2")
            }
            println(s"=======> won: ${r} with ${defs}")

          case _ =>
            println("call 404")
            // todo call 404 and stop processing
        }

        // if post
//        println("-"*100)
//        println(request.decoderResult())
//        println(request.content.toString(CharsetUtil.UTF_8))

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

object DefaultHttpHandler {
  implicit class RouteExt(val r: Route) extends AnyVal {
    def toHandler: DefaultHttpHandler = new DefaultHttpHandler(r)
  }
}
