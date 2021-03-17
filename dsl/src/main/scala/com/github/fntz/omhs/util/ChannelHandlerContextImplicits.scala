package com.github.fntz.omhs.util

import com.github.fntz.omhs.{Address, ForwardProxies, RemoteAddress, Unknown}
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpHeaders
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame

import java.net.InetSocketAddress

object ChannelHandlerContextImplicits {

  import AdditionalHeadersConstants._

  implicit class ChannelHandlerContextExt(val ctx: ChannelHandlerContext) extends AnyVal {
    def remoteAddress(headers: HttpHeaders): RemoteAddress = {
      fetch(Option(headers.get(XForwardFor)))
    }

    def remoteAddress(frame: DefaultHttp2HeadersFrame): RemoteAddress = {
      fetch(Option(frame.headers().get(XForwardFor)).map(_.toString))
    }

    private def fetch(forward: Option[String]) = {
      forward match {
        case Some(value) if value.trim.nonEmpty =>
          ForwardProxies(value.trim.split(",").reverse.toList)

        case _ =>
          ctx.channel().remoteAddress() match {
            case i: InetSocketAddress => Address(i.getAddress.getHostAddress)
            case _ => Unknown
          }
      }
    }
  }
}
