package com.github.fntz.omhs.handlers

import com.github.fntz.omhs.internal._
import com.github.fntz.omhs.streams.ChunkedOutputStream
import com.github.fntz.omhs.util._
import com.github.fntz.omhs._
import com.github.fntz.omhs.handlers.http2.AggregatedHttp2Message
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelFuture, ChannelFutureListener, ChannelHandlerContext, ChannelInboundHandlerAdapter}
import io.netty.handler.codec.http._
import io.netty.handler.codec.http2.{DefaultHttp2DataFrame, DefaultHttp2Headers, DefaultHttp2HeadersFrame, Http2FrameStream, HttpConversionUtil}
import io.netty.util.{CharsetUtil, Version}
import io.netty.util.concurrent.{Future, GenericFutureListener}
import org.slf4j.LoggerFactory

import java.time.ZonedDateTime
import scala.language.existentials

@Sharable
case class HttpHandler(route: Route, setup: Setup) extends ChannelInboundHandlerAdapter {

  import HttpHandler._
  import ResponseImplicits._
  import UtilImplicits._
  import ChannelHandlerContextImplicits._
  import Http2HeadersImplicits._

  private val logger = LoggerFactory.getLogger(getClass)
  private val byMethod = route.current.groupBy(_.rule.currentMethod)
  private val unhanded = route.currentUnhandled

  override def channelRead(ctx: ChannelHandlerContext, msg: Object): Unit = {
    msg match {
      case request: FullHttpRequest if HttpUtil.is100ContinueExpected(request) =>
        ctx.writeAndFlush(route.rewrite(continue))

      case request: FullHttpRequest =>
        val result = process(ctx, request, None)

        result.asyncResult.onComplete {
          case outResponse: CommonResponse =>
            write(
              ctx = ctx,
              request = request,
              isKeepAlive = HttpUtil.isKeepAlive(request),
              userResponse = outResponse
            ).addListener(fileCleaner(result.files))

          case streamResponse: StreamResponse =>
            write(
              ctx = ctx,
              request = request,
              isKeepAlive = HttpUtil.isKeepAlive(request),
              stream = streamResponse.stream
            ).addListener(fileCleaner(result.files))
        }

      case agg: AggregatedHttp2Message =>
        val request = HttpConversionUtil.toFullHttpRequest(
          agg.stream.id(),
          agg.headers.headers(),
          agg.data.content(),
          true
        )
        val result = process(ctx, request, Some(agg.stream))
        result.asyncResult.onComplete {
          case outResponse: CommonResponse =>
            write2(
              ctx = ctx,
              agg = agg,
              userResponse = outResponse
            ).addListener(fileCleaner(result.files))

          case streamResponse: StreamResponse =>
            write2(
              ctx = ctx,
              agg = agg,
              stream = streamResponse.stream
            )
        }

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

  private def write2(
                      ctx: ChannelHandlerContext,
                      agg: AggregatedHttp2Message,
                      userResponse: CommonResponse
                    ): ChannelFuture = {
    val content = Unpooled.copiedBuffer(userResponse.content)
    content.writeBytes(Unpooled.EMPTY_BUFFER.duplicate())

    val headers = new DefaultHttp2Headers().status(userResponse.status.codeAsText())
      .withContentType(userResponse.contentType)
      .withUserHeaders(userResponse.headers)
      .withDate(ZonedDateTime.now().format(setup.timeFormatter))
      .withLength(userResponse.content.length)
      .withServer(ServerVersion, setup.sendServerHeader)

    ctx.write(new DefaultHttp2HeadersFrame(headers).stream(agg.stream))
    ctx.write(new DefaultHttp2DataFrame(content, true).stream(agg.stream))
  }

  private def writeEmptyOnStream2(ctx: ChannelHandlerContext,
                                  http2Stream: Http2FrameStream) = {
    val headers = new DefaultHttp2Headers().status(HttpResponseStatus.OK.codeAsText())
      .withContentType(HttpHeaderValues.APPLICATION_OCTET_STREAM.toString)
      .withDate(ZonedDateTime.now().format(setup.timeFormatter))
      .withServer(ServerVersion, setup.sendServerHeader)

    ctx.write(new DefaultHttp2HeadersFrame(headers, false).stream(http2Stream))
  }

  private def write2(
                      ctx: ChannelHandlerContext,
                      agg: AggregatedHttp2Message,
                      stream: ChunkedOutputStream
                    ): ChannelFuture = {
    stream.flush()

    ctx.write(new DefaultHttp2DataFrame(Unpooled.EMPTY_BUFFER, true)
      .stream(agg.stream)).addListener(new GenericFutureListener[Future[_ >: Void]] {
      override def operationComplete(future: Future[_ >: Void]): Unit = {
        stream.close()
      }
    })
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
      .withXSSProtection(setup.sendXSSProtection)

    ctx.write(response) // will be flushed with first chunk
  }

  private def write(
                     ctx: ChannelHandlerContext,
                     request: FullHttpRequest,
                     isKeepAlive: Boolean,
                     stream: ChunkedOutputStream
                   ): ChannelFuture = {

    stream.flush()

    val f = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
      .addListener(new GenericFutureListener[Future[_ >: Void]] {
        override def operationComplete(future: Future[_ >: Void]): Unit = {
          stream.close()
        }
      })
    if (!isKeepAlive) {
      f.addListener(ChannelFutureListener.CLOSE)
    }
    f
  }

  private def write(ctx: ChannelHandlerContext,
                    request: FullHttpRequest,
                    isKeepAlive: Boolean,
                    userResponse: CommonResponse): ChannelFuture = {

    val response = empty.replace(Unpooled.copiedBuffer(userResponse.content))
      .processKeepAlive(isKeepAlive, request)
      .withUserHeaders(userResponse.headers)
      .setStatus(userResponse.status)
      .withContentType(userResponse.contentType)
      .withDate(ZonedDateTime.now().format(setup.timeFormatter))
      .withLength(userResponse.content.length)
      .withServer(ServerVersion, setup.sendServerHeader)
      .withXSSProtection(setup.sendXSSProtection)

    val f = ctx.writeAndFlush(route.rewrite(response))

    if (!isKeepAlive) {
      f.addListener(ChannelFutureListener.CLOSE)
    }
    f
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

object HttpHandler {

  import CollectionsConverters._

  private val currentProject = "omhs"
  private val ServerVersion = s"$currentProject on " + Version.identify().values.toScala.headOption
    .map { v => s"netty-${v.artifactVersion()}"}
    .getOrElse("unknown")
  private val continue = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE)

  private def empty = new DefaultFullHttpResponse(
    HttpVersion.HTTP_1_1,
    HttpResponseStatus.OK,
    Unpooled.EMPTY_BUFFER
  )
}

private case class ResourceResultContainer(
                                            files: List[FileDef],
                                            asyncResult: AsyncResult
                                     )