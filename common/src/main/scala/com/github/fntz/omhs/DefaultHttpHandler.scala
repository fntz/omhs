package com.github.fntz.omhs

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}
import io.netty.handler.codec.http._
import io.netty.util.Version
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.language.existentials

@Sharable
class DefaultHttpHandler(final val route: Route) extends ChannelInboundHandlerAdapter {

  // todo rename route
  private val logger = LoggerFactory.getLogger(getClass)

  private val rules = route.current
  private val unhanded = route.currentUnhandled

  private val currentProject = "omhs"
  private val nettyVersion = s"$currentProject on " + Version.identify().asScala.values.headOption
    .map { v => s"netty-${v.artifactVersion()}"}
    .getOrElse("unknown")
  private val serverHeader = "X-Server-Version"
  private val continue = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE)

  override def channelRead(ctx: ChannelHandlerContext, msg: Object): Unit = {
    import AsyncResult._
    val empty = new DefaultFullHttpResponse(
      HttpVersion.HTTP_1_1,
      HttpResponseStatus.OK,
      Unpooled.EMPTY_BUFFER
    )
    // todo from setup
    empty.headers().set(serverHeader, nettyVersion)

    msg match {
      case request: FullHttpRequest if HttpUtil.is100ContinueExpected(request)  =>
        ctx.writeAndFlush(continue)

      case request: FullHttpRequest =>
        logger.debug(s"${request.method()} -> ${request.uri()}")
        val decoder = new QueryStringDecoder(request.uri)
        val target = decoder.rawPath()

        // todo from map method -> params
        val result = rules
          .map(x => (x, Param.parse(target, x.rule.params)))
          .find(_._2.isSuccess)

        val matchResult = result match {
          case Some((r, ParseResult(_, defs))) =>
            val real = defs.filterNot(_.skip)
            try {
              RequestHelper.materialize(request, r.rule) match {
                case Right(defs) =>
                  r.run(real ++ defs)
                case Left(reason) =>
                  AsyncResult.complete(unhanded.apply(reason))
              }
            } catch {
              case t: Throwable =>
                logger.warn("Failed to call function", t)
                AsyncResult.complete(unhanded.apply(UnhandledException(t)))
            }

          case _ =>
            logger.debug(s"No matched route for ${request.uri()}")
            AsyncResult.complete(unhanded.apply(PathNotFound(request.uri())))
        }

        matchResult.onComplete { v: CommonResponse =>
          done(
            ctx = ctx,
            request = request,
            response = empty.replace(
              Unpooled.copiedBuffer(v.content)),
            userResponse = v
          )
        }

      case _ =>
        super.channelRead(ctx, msg)
    }
  }

  protected def done(ctx: ChannelHandlerContext,
                     request: FullHttpRequest,
                     response: FullHttpResponse,
                     userResponse: CommonResponse) = {
    if (HttpUtil.isKeepAlive(request)) {
      response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content.readableBytes)
      response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
    }

    response.setStatus(userResponse.status)
    response.headers.set(HttpHeaderNames.CONTENT_TYPE, userResponse.contentType)
    response.headers.set(HttpHeaderNames.CONTENT_LENGTH, userResponse.content.length)

    ctx.writeAndFlush(response)
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
  // params:
  // - do not pass X-Server-Version
  // - ignore-case
  // - netty ???
  implicit class RouteExt(val r: Route) extends AnyVal {
    def toHandler: DefaultHttpHandler = new DefaultHttpHandler(r)
  }
}
