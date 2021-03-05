package com.github.fntz.omhs

import com.github.fntz.omhs.util.UtilImplicits
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, ChannelInboundHandlerAdapter}
import io.netty.handler.codec.http._
import io.netty.util.Version
import org.slf4j.LoggerFactory

import java.net.InetSocketAddress
import java.time.format.DateTimeFormatter
import java.time.{ZoneOffset, ZonedDateTime}
import java.util.Locale
import scala.collection.JavaConverters._
import scala.language.existentials

@Sharable
class DefaultHttpHandler(final val route: Route) extends ChannelInboundHandlerAdapter {

  import DefaultHttpHandler._
  import UtilImplicits._

  private val logger = LoggerFactory.getLogger(getClass)
  private val byMethod = route.current.groupBy(_.rule.method)
  private val unhanded = route.currentUnhandled

  // todo pass with params probably
  private val formatter = DateTimeFormatter.RFC_1123_DATE_TIME
    .withZone(ZoneOffset.UTC).withLocale(Locale.US)

  override def channelRead(ctx: ChannelHandlerContext, msg: Object): Unit = {
    // todo from setup
    empty.headers().set(serverHeader, nettyVersion)

    msg match {
      case request: FullHttpRequest if HttpUtil.is100ContinueExpected(request)  =>
        ctx.writeAndFlush(continue)

      case request: FullHttpRequest =>
        val remoteAddress = ctx.remoteAddress
        logger.debug(s"${request.method()} -> ${request.uri()} from $remoteAddress")
        val result = findRule(request)

        val matchResult = result match {
          case Some((r, ParseResult(_, defs))) =>
            try {
              // todo mixed files should be released !!!
              r.rule.materialize(request, remoteAddress)
                .map(defs.filterNot(_.skip) ++ _)
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

        matchResult.onComplete {
          case outResponse: CommonResponse =>
            write(
              ctx = ctx,
              request = request,
              isKeepAlive = HttpUtil.isKeepAlive(request),
              userResponse = outResponse
            )

          case streamResponse: StreamResponse =>
            write(
              ctx = ctx,
              request = request,
              isKeepAlive = HttpUtil.isKeepAlive(request),
              userResponse = streamResponse
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

  private def write(
                     ctx: ChannelHandlerContext,
                     request: FullHttpRequest,
                     isKeepAlive: Boolean,
                     userResponse: StreamResponse
                   ): Unit = {

    val response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK)
    response.headers()
      .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
      .set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED)

    userResponse.headers.foreach { case (h, v) =>
      response.headers().set(h, v)
    }

    processKeepAlive(isKeepAlive, request, response)

    ctx.write(response) // empty first

    userResponse.it.zipWithIndex.foreach { case (chunk, index) =>
      ctx.write(new DefaultHttpContent(
        Unpooled.copiedBuffer(chunk)
      ))
      if (index % 3 == 0) {
        ctx.flush()
      }
    }

    val f = ctx.write(LastHttpContent.EMPTY_LAST_CONTENT)
    if (!isKeepAlive) {
      f.addListener(ChannelFutureListener.CLOSE)
    }
  }

  private def write(ctx: ChannelHandlerContext,
                    request: FullHttpRequest,
                     isKeepAlive: Boolean,
                     userResponse: CommonResponse): Unit = {

    val response = empty.replace(Unpooled.copiedBuffer(userResponse.content))

    processKeepAlive(isKeepAlive, request, response)

    userResponse.headers.foreach { case (h, v) =>
      response.headers().set(h, v)
    }

    response.setStatus(userResponse.status)
    response.headers.set(HttpHeaderNames.CONTENT_TYPE, userResponse.contentType)
    response.headers.set(HttpHeaderNames.CONTENT_LENGTH, userResponse.content.length)
    response.headers.set(HttpHeaderNames.DATE, ZonedDateTime.now().format(formatter))

    val f = ctx.writeAndFlush(response)

    if (!isKeepAlive) {
      f.addListener(ChannelFutureListener.CLOSE)
    }
  }

  private def processKeepAlive(isKeepAlive: Boolean, request: FullHttpRequest, response: HttpResponse): Unit = {
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
