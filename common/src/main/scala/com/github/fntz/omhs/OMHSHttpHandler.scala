package com.github.fntz.omhs

import com.github.fntz.omhs.internal.{ExecutableRule, FileDef, ParamDef, ParamsParser, ParseResult}
import com.github.fntz.omhs.util.{CollectionsConverters, ResponseImplicits, UtilImplicits}
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelFuture, ChannelFutureListener, ChannelHandlerContext, ChannelInboundHandlerAdapter}
import io.netty.handler.codec.http._
import io.netty.util.{CharsetUtil, Version}
import org.slf4j.LoggerFactory

import java.net.InetSocketAddress
import java.time.ZonedDateTime
import scala.language.existentials

@Sharable
case class OMHSHttpHandler(route: Route, setup: Setup) extends ChannelInboundHandlerAdapter {

  import OMHSHttpHandler._
  import UtilImplicits._
  import ResponseImplicits._

  private val logger = LoggerFactory.getLogger(getClass)
  private val byMethod = route.current.groupBy(_.rule.currentMethod)
  private val unhanded = route.currentUnhandled

  override def channelRead(ctx: ChannelHandlerContext, msg: Object): Unit = {
    msg match {
      case request: FullHttpRequest if HttpUtil.is100ContinueExpected(request) =>
        ctx.writeAndFlush(route.rewrite(continue))

      case request: FullHttpRequest =>
        val remoteAddress = ctx.remoteAddress(request.headers())
        logger.debug(s"${request.method()} -> ${request.uri()} from $remoteAddress")

        val result = findRule(request) match {
          case Some((r, ParseResult(_, defs))) =>
            val materialized = r.rule.materialize(request, remoteAddress, setup)
            val files = fetchFilesToRelease(materialized)
            try {
              val asyncResult = materialized.map(defs.filterNot(_.skip) ++ _)
                .map(r.run)
                .fold(fail, identity)
              ResourceResultContainer(files, asyncResult)
            } catch {
              case t: Throwable =>
                logger.warn("Failed to call function", t)
                ResourceResultContainer(files, fail(UnhandledException(t)))
            }

          case _ =>
            logger.warn(s"No matched route for ${request.uri()}")
            ResourceResultContainer(Nil, fail(PathNotFound(request.uri())))
        }

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
              userResponse = streamResponse
            ).addListener(fileCleaner(result.files))
        }


      case _ =>
        super.channelRead(ctx, msg)
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

  private def write(
                     ctx: ChannelHandlerContext,
                     request: FullHttpRequest,
                     isKeepAlive: Boolean,
                     userResponse: StreamResponse
                   ): ChannelFuture = {

    val response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK)

    response
      .withContentType(HttpHeaderValues.TEXT_PLAIN.toString) // is it?
      .chunked
      .withDate(ZonedDateTime.now().format(setup.timeFormatter))
      .withUserHeaders(userResponse.headers)
      .processKeepAlive(isKeepAlive, request)
      .withServer(ServerVersion, setup.sendServerHeader)

    ctx.write(response) // empty first

    // somehow depends on sslContext check stackoverflow TODO
    userResponse.it.zipWithIndex.foreach { case (chunk, index) =>
      val tmp = new DefaultHttpContent(
        Unpooled.copiedBuffer(chunk)
      )
      ctx.write(tmp)
      if (index % 3 == 0) {
        ctx.flush()
      }
    }

    val f = ctx.write(LastHttpContent.EMPTY_LAST_CONTENT)
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

object OMHSHttpHandler {

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