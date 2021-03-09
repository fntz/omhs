package com.github.fntz.omhs.util

import com.github.fntz.omhs._
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.{FullHttpRequest, HttpHeaders}

import java.net.InetSocketAddress

object UtilImplicits {

  implicit class RuleImplicits(val rule: Rule) extends AnyVal {
    def toDefs(request: FullHttpRequest, remoteAddress: RemoteAddress): List[CurrentHttpRequestDef] = {
      if (rule.isRunWithRequest) {
        val currentRequest = CurrentHttpRequest(request, remoteAddress)
        List(CurrentHttpRequestDef(currentRequest))
      } else {
        Nil
      }
    }

    def materialize(request: FullHttpRequest,
                    remoteAddress: RemoteAddress,
                    setup: Setup
                   ): Either[UnhandledReason, List[ParamDef[_]]] = {
      RequestHelper.fetchAdditionalDefs(request, rule, setup).map { additionalDefs =>
        additionalDefs ++ rule.toDefs(request, remoteAddress)
      }
    }
  }

  implicit class ChannelHandlerContextExt(val ctx: ChannelHandlerContext) extends AnyVal {
    def remoteAddress(headers: HttpHeaders): RemoteAddress = {
      Option(headers.get("X-Forwarded-For")) match {
        case Some(forward) if forward.trim.nonEmpty =>
          ForwardProxies(forward.trim.split(",").reverse.toList)

        case _ =>
          ctx.channel().remoteAddress() match {
            case i: InetSocketAddress => Address(i.getAddress.getHostAddress)
            case _ => Unknown
          }
      }
    }
  }


}
