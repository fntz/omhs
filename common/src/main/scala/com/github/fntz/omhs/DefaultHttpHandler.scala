package com.github.fntz.omhs

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.{HttpMethod => HM}
import io.netty.util.{CharsetUtil, Version}
import scala.collection.JavaConverters._
import org.slf4j.LoggerFactory

@Sharable
class DefaultHttpHandler(final val route: Route) extends ChannelInboundHandlerAdapter {

  // todo rename route
  private val logger = LoggerFactory.getLogger(getClass)

  private val rules = route.current

  private val currentProject = "omhs"
  private val nettyVersion = s"$currentProject on " + Version.identify().asScala.values.headOption
    .map { v => s"netty-${v.artifactVersion()}"}
    .getOrElse("unknown")
  private val serverHeader = "X-Server-Version"

  override def channelRead(ctx: ChannelHandlerContext, msg: Object): Unit = {
    //
    val empty = new DefaultFullHttpResponse(
      HttpVersion.HTTP_1_1,
      HttpResponseStatus.OK,
      Unpooled.EMPTY_BUFFER
    )

    msg match {
      case request: FullHttpRequest =>
        val cr = CurrentHttpRequest(request.uri())
        logger.debug(s"${request.method()} -> ${request.uri()}")
        val decoder = new QueryStringDecoder(request.uri)
        val target = decoder.rawPath()
        val headers = request.headers()

        val result = rules.map(x => (x, Param.parse(target, x.rule.params)))

        val first = result.find(_._2.isSuccess)

        // todo on big screen :)
        var strBody = ""

        if (request.method() == HM.POST) {
          val tmp = request.decoderResult()
          // todo stop processing here and push unparsed
          if (tmp.isSuccess) {
            strBody = request.content.toString(CharsetUtil.UTF_8)
          }
        }

        val matchResult = first match {
          case Some((r, ParseResult(_, defs))) =>
            val real = defs.filterNot(_.skip)
            try {
              if (strBody.nonEmpty) {
                val parsed = r.rule.currentReader.read(strBody)
                r.run(real ++ Vector(BodyDef(parsed)))
              } else {
                r.run(real)
              }

            } catch {
              case t: Throwable =>
                logger.warn("Failed to call function", t)
                route.currentUnhandled.apply(UnhandledException(t))
            }

          case _ =>
            logger.debug(s"No matched route for ${request.uri()}")
            route.currentUnhandled.apply(PathNotFound)
        }

        val response = empty.replace(Unpooled.copiedBuffer(matchResult.content.getBytes))

        // todo from setup
        response.headers().set(serverHeader, nettyVersion)

        if (HttpUtil.isKeepAlive(request))
        {
          response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content.readableBytes)
          response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
        }

        response.setStatus(HttpResponseStatus.valueOf(matchResult.status))
        response.headers.set(HttpHeaderNames.CONTENT_TYPE, matchResult.contentType)
        response.headers.set(HttpHeaderNames.CONTENT_LENGTH, matchResult.content.length)

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
