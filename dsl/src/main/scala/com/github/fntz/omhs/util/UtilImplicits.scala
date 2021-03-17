package com.github.fntz.omhs.util

import com.github.fntz.omhs._
import com.github.fntz.omhs.internal.{CurrentHttpRequestDef, ParamDef, StreamDef}
import com.github.fntz.omhs.streams.ChunkedOutputStream
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.cookie.{Cookie, ServerCookieDecoder}
import io.netty.handler.codec.http.{FullHttpRequest, HttpHeaderNames, HttpHeaders}
import io.netty.handler.codec.http2.{DefaultHttp2Headers, DefaultHttp2HeadersFrame}

import java.net.InetSocketAddress

private[omhs] object UtilImplicits {

  implicit class RuleImplicits(val rule: Rule) extends AnyVal {
    def toDefs(request: FullHttpRequest, remoteAddress: RemoteAddress, setup: Setup): List[CurrentHttpRequestDef] = {
      if (rule.isNeedToPassCurrentRequest) {
        val currentRequest = CurrentHttpRequest(request, remoteAddress, setup)
        List(CurrentHttpRequestDef(currentRequest))
      } else {
        Nil
      }
    }

    def materialize(ctx: ChannelHandlerContext,
                    request: FullHttpRequest,
                    remoteAddress: RemoteAddress,
                    setup: Setup
                   ): Either[UnhandledReason, List[ParamDef[_]]] = {
      RequestHelper.fetchAdditionalDefs(request, rule, setup).map { additionalDefs =>
        val streamDef = if (rule.isNeedToStream) {
          List(StreamDef(ChunkedOutputStream(ctx, setup.chunkSize)))
        } else {
          Nil
        }
        additionalDefs ++ rule.toDefs(request, remoteAddress, setup) ++ streamDef
      }
    }
  }

}
