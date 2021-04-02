package com.github.fntz.omhs.handlers

import com.github.fntz.omhs._
import com.github.fntz.omhs.handlers.http2.AggregatedHttp2Message
import com.github.fntz.omhs.handlers.writers.{BeforeStreamingWriter, ErrorWriters, Http2ResponseWriter, HttpResponseWriter}
import com.github.fntz.omhs.internal._
import com.github.fntz.omhs.util._
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}
import io.netty.handler.codec.http._
import io.netty.handler.codec.http2._
import io.netty.handler.ssl.SslHandler
import io.netty.util.ReferenceCountUtil
import org.slf4j.LoggerFactory

import scala.language.existentials

@Sharable
case class HttpHandler(route: Route, setup: Setup) extends ChannelInboundHandlerAdapter {

  import ChannelHandlerContextImplicits._
  import FullHttpRequestImplicits._
  import HttpHandler._
  import UtilImplicits._

  private val logger = LoggerFactory.getLogger(getClass)
  private val byMethod = route.current.groupBy(_.rule.currentMethod)
  private val unhanded = route.currentUnhandled
  private val beforeStreamingWriter = BeforeStreamingWriter(
    route = route,
    setup = setup
  )

  override def channelRead(ctx: ChannelHandlerContext, msg: Object): Unit = {
    msg match {
      case request: FullHttpRequest if HttpUtil.is100ContinueExpected(request) =>
        ctx.writeAndFlush(route.rewrite(continue))

      case request: FullHttpRequest =>
        HttpResponseWriter(
          route = route,
          setup = setup,
          ctx = ctx,
          request = request
        ).write(process(ctx, request, None))

      case agg: AggregatedHttp2Message =>
        val request = HttpConversionUtil.toFullHttpRequest(
          agg.stream.id(),
          agg.headers.headers(),
          agg.data.content(),
          true
        )
        Http2ResponseWriter(
          setup = setup,
          route = route,
          ctx = ctx,
          request = request,
          agg = agg
        ).write(process(ctx, request, Some(agg.stream)))

      case TooLargeObject(stream) =>
        ErrorWriters(route, setup).write413(ctx, stream)

      case _ =>
    }
    ReferenceCountUtil.release(msg)
  }

  private def process(ctx: ChannelHandlerContext,
                      request: FullHttpRequest,
                      http2Stream: Option[Http2FrameStream]
                     ): ResourceResultContainer = {
    val isSsl = Option(ctx.channel().pipeline().get(classOf[SslHandler])).isDefined
    val remoteAddress = ctx.remoteAddress(request.headers())
    logger.debug(s"${request.method()} -> ${request.uri()} from $remoteAddress")

    request.findRule(byMethod) match {
      case Some((r, ParseResult(_, defs))) =>
        val materialized = r.rule.materialize(ctx,
          request, remoteAddress, setup, http2Stream, isSsl)
        val files = materialized.fetchFilesToRelease
        try {
          val asyncResult = materialized.map(defs.filterNot(_.skip) ++ _)
            .map { params =>
              beforeStreamingWriter.write(
                ctx = ctx,
                request = request,
                http2Stream = http2Stream,
                materialized = materialized
              )
              r.run(params)
            }
            .fold(fail, identity)
          ResourceResultContainer(files, asyncResult)
        } catch {
          case t: Throwable =>
            logger.warn("Failed to call function", t)
            handlers.ResourceResultContainer(files, fail(UnhandledException(t)))
        }

      case _ =>
        logger.warn(s"No matched route for ${request.uri()}")
        handlers.ResourceResultContainer(Nil, fail(PathNotFound(request.uri())))
    }
  }

  private def fail(reason: UnhandledReason): AsyncResult = {
    AsyncResult.completed(unhanded.apply(reason))
  }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
    ctx.flush()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    logger.warn(cause.getMessage)
    ctx.close()
  }
}

object HttpHandler {

  private val continue = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE)

}

private case class ResourceResultContainer(
                                            files: List[FileDef],
                                            asyncResult: AsyncResult
                                     )