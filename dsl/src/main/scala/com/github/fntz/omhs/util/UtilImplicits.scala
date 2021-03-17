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
  import CollectionsConverters._

  private val XForwardFor = "X-Forwarded-For"

  implicit class SetupImplicits(val setup: Setup) extends AnyVal {
    def decode(request: FullHttpRequest): Iterable[Cookie] = {
      Option(request.headers.get(HttpHeaderNames.COOKIE)).map { x =>
        val decoder = setup.cookieDecoderStrategy match {
          case CookieDecoderStrategies.Strict =>
            ServerCookieDecoder.STRICT
          case CookieDecoderStrategies.Lax =>
            ServerCookieDecoder.LAX
        }
        decoder.decode(x).toScala
      }.getOrElse(Set.empty)
    }
  }

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
