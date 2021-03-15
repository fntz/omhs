package com.github.fntz.omhs.handlers

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http2.Http2FrameCodecBuilder
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler
import org.slf4j.LoggerFactory

object HttpMixedHandler {

  private val logger = LoggerFactory.getLogger(getClass)

  def build(modes: Vector[WorkMode]): ApplicationProtocolNegotiationHandler = {
    val protocols = modes.map(_.protocol).distinct
    logger.info(s"Configure protocols: ${protocols.mkString(", ")}")
    val fallback = modes.sortBy(_.weight).headOption.getOrElse(WorkModes.Http11)
      .protocol
    logger.info(s"Fallback protocol: $fallback")
    new ApplicationProtocolNegotiationHandler(fallback) {
      override def configurePipeline(ctx: ChannelHandlerContext, protocol: String): Unit = {
         modes.find(_.protocol == protocol) match {
           case Some(value) if value.isH2 =>
             ctx.pipeline().addLast(
               Http2FrameCodecBuilder.forServer().build()
               //new Http2HandlerX()
             )

           case Some(_) =>
             // http1
             // todo

           case None =>
             logger.warn(s"Unknown protocol: $protocol, available: ${protocols.mkString(", ")}")
             throw new IllegalStateException(s"Unknown protocol: $protocol")
         }
      }
    }
  }

}


