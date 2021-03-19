package com.github.fntz.omhs.handlers

import com.github.fntz.omhs._
import com.github.fntz.omhs.handlers.http2.AggregatedHttp2Message
import com.github.fntz.omhs.handlers.writers.{Http2ResponseWriter, HttpResponseWriter, ServerVersion}
import com.github.fntz.omhs.internal._
import com.github.fntz.omhs.streams.ChunkedOutputStream
import com.github.fntz.omhs.util._
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelFuture, ChannelFutureListener, ChannelHandlerContext, ChannelInboundHandlerAdapter}
import io.netty.handler.codec.http._
import io.netty.handler.codec.http2._
import io.netty.util.Version
import io.netty.util.concurrent.{Future, GenericFutureListener}
import org.slf4j.LoggerFactory

import java.time.ZonedDateTime
import scala.language.existentials

@Sharable
case class HttpHandler(route: Route, setup: Setup) extends ChannelInboundHandlerAdapter {

  import ChannelHandlerContextImplicits._
  import Http2HeadersImplicits._
  import HttpHandler._
  import ResponseImplicits._
  import UtilImplicits._
  import ServerVersion._

  private val logger = LoggerFactory.getLogger(getClass)
  private val byMethod = route.current.groupBy(_.rule.currentMethod)
  private val unhanded = route.currentUnhandled

  override def channelRead(ctx: ChannelHandlerContext, msg: Object): Unit = {
    msg match {
      case request: FullHttpRequest if HttpUtil.is100ContinueExpected(request) =>
        ctx.writeAndFlush(route.rewrite(continue))

      case request: FullHttpRequest =>
        val result = process(ctx, request, None)
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
          agg = agg
        ).write(process(ctx, request, Some(agg.stream)))

      case TooLargeObject(stream) =>
        write413(ctx, stream)

      case _ =>
        super.channelRead(ctx, msg)
    }
  }

  private def process(ctx: ChannelHandlerContext,
                      request: FullHttpRequest,
                      http2Stream: Option[Http2FrameStream]
                     ): ResourceResultContainer = {
    val remoteAddress = ctx.remoteAddress(request.headers())
    logger.debug(s"${request.method()} -> ${request.uri()} from $remoteAddress")

    findRule(request) match {
      case Some((r, ParseResult(_, defs))) =>
        val materialized = r.rule.materialize(ctx, request, remoteAddress, setup, http2Stream)
        val files = fetchFilesToRelease(materialized)
        try {
          val asyncResult = materialized.map(defs.filterNot(_.skip) ++ _)
            .map { params =>
              beforeStreaming(
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

  private def fileCleaner(files: List[FileDef]): ChannelFutureListener = {
    (_: ChannelFuture) => {
      files.flatMap(_.value).filter(_.refCnt() != 0).map(_.release())
    }
  }

  private def fetchFilesToRelease(defs: Either[UnhandledReason, List[ParamDef[_]]]): List[FileDef] = {
    defs.getOrElse(Nil).collect {
      case f: FileDef => f
    }
  }

  private def findRule(request: FullHttpRequest): Option[(ExecutableRule, ParseResult)] = {
    val decoder = new QueryStringDecoder(request.uri)
    val target = decoder.rawPath()
    byMethod
      .getOrElse(request.method(), Vector.empty)
      .map { x => (x, ParamsParser.parse(target, x.rule.currentParams)) }
      .find(_._2.isSuccess)
  }

  private def fail(reason: UnhandledReason): AsyncResult = {
    AsyncResult.completed(unhanded.apply(reason))
  }

  private def beforeStreaming(
                               ctx: ChannelHandlerContext,
                               request: FullHttpRequest,
                               http2Stream: Option[Http2FrameStream],
                               materialized: Either[UnhandledReason, List[ParamDef[_]]]
                             ): Unit = {
    // if user wants to stream data, I should send empty response first
    val hasStream = materialized.getOrElse(Nil).collectFirst {
      case _: StreamDef => true
    }.getOrElse(false)
    if (hasStream) {
      logger.debug("Streaming detected, write empty response")
      http2Stream match {
        case Some(stream) =>
          writeEmptyOnStream2(ctx, stream)
        case _ =>
          writeEmptyOnStream(ctx, request)
      }
    }
  }

  private def write413(ctx: ChannelHandlerContext, stream: Http2FrameStream) = {
    val headers = new DefaultHttp2Headers()
      .status(HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE.codeAsText())
      .withDate(ZonedDateTime.now().format(setup.timeFormatter))
      .withServer(ServerVersion, setup.sendServerHeader)

    ctx.write(route.rewrite(new DefaultHttp2HeadersFrame(headers, true).stream(stream)))
  }

  private def writeEmptyOnStream2(ctx: ChannelHandlerContext,
                                  http2Stream: Http2FrameStream) = {
    val headers = new DefaultHttp2Headers().status(HttpResponseStatus.OK.codeAsText())
      .withContentType(HttpHeaderValues.APPLICATION_OCTET_STREAM.toString)
      .withDate(ZonedDateTime.now().format(setup.timeFormatter))
      .withServer(ServerVersion, setup.sendServerHeader)

    ctx.write(route.rewrite(new DefaultHttp2HeadersFrame(headers, false)
      .stream(http2Stream)))
  }

  private def writeEmptyOnStream(ctx: ChannelHandlerContext,
                                 request: FullHttpRequest
                                ) = {
    val response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK)

    response
      .withContentType(HttpHeaderValues.APPLICATION_OCTET_STREAM.toString)
      .chunked
      .withDate(ZonedDateTime.now().format(setup.timeFormatter))
      .processKeepAlive(HttpUtil.isKeepAlive(request), request)
      .withServer(ServerVersion, setup.sendServerHeader)

    ctx.write(route.rewrite(response)) // will be flushed with first chunk
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

  import CollectionsConverters._

  private val continue = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE)

}

private case class ResourceResultContainer(
                                            files: List[FileDef],
                                            asyncResult: AsyncResult
                                     )