package com.github.fntz.omhs.util

import com.github.fntz.omhs._
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest

import java.net.InetSocketAddress

object UtilImplicits {

  implicit class RuleImplicits(val rule: Rule) extends AnyVal {
    def toDefs(request: FullHttpRequest, remoteAddress: String): List[CurrentHttpRequestDef] = {
      if (rule.isRunWithRequest) {
        val currentRequest = CurrentHttpRequest(request, remoteAddress)
        List(CurrentHttpRequestDef(currentRequest))
      } else {
        Nil
      }
    }

    def materialize(request: FullHttpRequest, remoteAddress: String): Either[UnhandledReason, List[ParamDef[_]]] = {
      RequestHelper.fetchAdditionalDefs(request, rule).map { additionalDefs =>
        additionalDefs ++ rule.toDefs(request, remoteAddress)
      }
    }
  }

  implicit class ChannelHandlerContextExt(val ctx: ChannelHandlerContext) extends AnyVal {
    // todo x-forward-from for checking proxy see notes
    // enum:
    // hostName
    // forwardFrom
    // Unknown
    def remoteAddress: String = {
      ctx.channel().remoteAddress() match {
        case i: InetSocketAddress => i.getAddress.getHostAddress
        case _ => "unknown"
      }
    }
  }

}
