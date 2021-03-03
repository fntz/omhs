package com.github.fntz.omhs

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._
import io.netty.util.{CharsetUtil, Version}
import scala.collection.JavaConverters._
import scala.language.existentials
import org.slf4j.LoggerFactory

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
    //
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
        println("~"*100)
        val cr = CurrentHttpRequest(request.uri())
        logger.debug(s"${request.method()} -> ${request.uri()}")
        val decoder = new QueryStringDecoder(request.uri)
        val target = decoder.rawPath()
        val headers = request.headers()

        // todo from map method -> params
        val result = rules
          .map(x => (x, Param.parse(target, x.rule.params)))
          .find(_._2.isSuccess)

        val matchResult = result match {
          case Some((r, ParseResult(_, defs))) =>
            val real = defs.filterNot(_.skip).toList
            try {
              if (request.decoderResult().isSuccess) {
                val bodyDef = if (r.rule.isParseBody) {
                  val strBody = request.content.toString(CharsetUtil.UTF_8)
                  List(BodyDef(r.rule.currentReader.read(strBody)))
                } else {
                  Nil
                }
                val tmpHeaderDefs = if (r.rule.currentHeaders.nonEmpty) {
                  r.rule.currentHeaders.map { need =>
                    (headers.get(need), need)
                  }.toList
                } else {
                  Nil
                }
                val nullHeader = tmpHeaderDefs.find(_._1 == null)
                if (nullHeader.isEmpty) { // is it correct ?
                  val headerDefs = tmpHeaderDefs.map(x => HeaderDef(x._1))
                  val full = real ++ bodyDef ++ headerDefs
                  r.run(full)
                } else {
                  logger.warn("Header is missing")
                  unhanded.apply(HeaderIsMissing(nullHeader.get._2))
                }
              } else {
                logger.warn(s"Can not parse body", request.decoderResult().cause())
                unhanded.apply(BodyIsUnparsable)
              }
            } catch {
              case t: Throwable =>
                logger.warn("Failed to call function", t)
                unhanded.apply(UnhandledException(t))
            }

          case _ =>
            logger.debug(s"No matched route for ${request.uri()}")
            unhanded.apply(PathNotFound(request.uri()))
        }

        val response = empty.replace(Unpooled.copiedBuffer(matchResult.content.getBytes))

        if (HttpUtil.isKeepAlive(request)) {
          empty.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content.readableBytes)
          empty.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
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
  // params:
  // - do not pass X-Server-Version
  // - ignore-case
  // - netty ???
  implicit class RouteExt(val r: Route) extends AnyVal {
    def toHandler: DefaultHttpHandler = new DefaultHttpHandler(r)
  }
}
