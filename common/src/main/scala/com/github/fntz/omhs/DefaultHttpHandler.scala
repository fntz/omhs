package com.github.fntz.omhs

import com.github.fntz.omhs.util.UtilImplicits
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelFuture, ChannelHandlerContext, ChannelInboundHandlerAdapter}
import io.netty.handler.codec.http._
import io.netty.util.Version
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.language.existentials

@Sharable
class DefaultHttpHandler(final val route: Route) extends ChannelInboundHandlerAdapter {

  import DefaultHttpHandler._
  import UtilImplicits._

  private val logger = LoggerFactory.getLogger(getClass)
  private val byMethod = route.current.groupBy(_.rule.method)
  private val unhanded = route.currentUnhandled

  override def channelRead(ctx: ChannelHandlerContext, msg: Object): Unit = {
    // todo from setup
    empty.headers().set(serverHeader, nettyVersion)

    msg match {
      case request: FullHttpRequest if HttpUtil.is100ContinueExpected(request)  =>
        ctx.writeAndFlush(continue)

      case request: FullHttpRequest =>
        logger.debug(s"${request.method()} -> ${request.uri()}")
        val result = findRule(request)

        val matchResult = result match {
          case Some((r, ParseResult(_, defs))) =>
            val real = defs.filterNot(_.skip)
            try {
              r.rule.materialize(request)
                .map(real ++ _)
                .map(r.run)
                .fold(fail, identity)
            } catch {
              case t: Throwable =>
                logger.warn("Failed to call function", t)
                fail(UnhandledException(t))
            }

          case _ =>
            logger.warn(s"No matched route for ${request.uri()}")
            fail(PathNotFound(request.uri()))
        }

        matchResult.onComplete { v: CommonResponse =>
          write(
            ctx = ctx,
            isKeepAlive = HttpUtil.isKeepAlive(request),
            userResponse = v
          )
        }

      case _ =>
        super.channelRead(ctx, msg)
    }
  }

  private def findRule(request: FullHttpRequest): Option[(RuleAndF, ParseResult)] = {
    val decoder = new QueryStringDecoder(request.uri)
    val target = decoder.rawPath()
    byMethod
      .getOrElse(request.method(), Vector.empty)
      .map { x => (x, Param.parse(target, x.rule.params)) }
      .find(_._2.isSuccess)
  }

  private def fail(reason: UnhandledReason): AsyncResult = {
    AsyncResult.completed(unhanded.apply(reason))
  }

  private def write(ctx: ChannelHandlerContext,
                     isKeepAlive: Boolean,
                     userResponse: CommonResponse): ChannelFuture = {

    val response = empty.replace(Unpooled.copiedBuffer(userResponse.content))

    if (isKeepAlive) {
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

  private val currentProject = "omhs"
  private val nettyVersion = s"$currentProject on " + Version.identify().asScala.values.headOption
    .map { v => s"netty-${v.artifactVersion()}"}
    .getOrElse("unknown")
  private val serverHeader = "X-Server-Version"
  private val continue = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE)
  private val empty = new DefaultFullHttpResponse(
    HttpVersion.HTTP_1_1,
    HttpResponseStatus.OK,
    Unpooled.EMPTY_BUFFER
  )

  // params:
  // - do not pass X-Server-Version
  // - ignore-case
  // - netty ???
  implicit class RouteExt(val r: Route) extends AnyVal {
    def toHandler: DefaultHttpHandler = new DefaultHttpHandler(r)
  }
}
