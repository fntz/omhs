package com.github.fntz.omhs

import com.github.fntz.omhs.util.SetupImplicits
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.codec.http._
import io.netty.util.CharsetUtil

/**
 * Describes incoming http request
 * @param uri - full request path
 * @param path - only path part
 * @param query - query part
 * @param method - current http method
 * @param headers - request headers
 * @param rawBody - body as a string
 * @param isHttp2 - protocol http2
 * @param remoteAddress - remote address
 * @param cookies - decoded cookies
 */
case class CurrentHttpRequest(
                             uri: String,
                             path: String,
                             query: String,
                             method: HttpMethod,
                             headers: HttpHeaders,
                             rawBody: String,
                             isHttp2: Boolean,
                             remoteAddress: RemoteAddress,
                             cookies: Iterable[Cookie]
                      ) {
  import CurrentHttpRequest._

  def isAccept(maybe: String): Boolean = {
    accept.forall(_.toLowerCase == maybe.toLowerCase)
  }

  def accept: Option[String] = {
    Option(headers.get(HttpHeaderNames.ACCEPT))
  }
  // scheme
  // port

  def isForwarded: Boolean = {
    remoteAddress match {
      case _: ForwardProxies => true
      case _ => false
    }
  }

  def isXHR: Boolean = {
    Option(headers.get(HttpHeaderNames.X_REQUESTED_WITH))
      .forall(_.toLowerCase == AjaxHeaderValue)
  }

  def userAgent: Option[String] = {
    Option(headers.get(HttpHeaderNames.USER_AGENT))
  }
}

object CurrentHttpRequest {

  import SetupImplicits._

  private val AjaxHeaderValue = "xmlhttprequest"

  def apply(request: FullHttpRequest,
            remoteAddress: RemoteAddress,
            setup: Setup,
            isHttp2: Boolean
           ): CurrentHttpRequest = {
    val decoder = new QueryStringDecoder(request.uri)
    val cookies = setup.decode(request)
    new CurrentHttpRequest(
      uri = request.uri(),
      path = decoder.rawPath(),
      query = decoder.rawQuery(),
      method = request.method(),
      headers = request.headers(),
      rawBody = request.content.toString(CharsetUtil.UTF_8),
      isHttp2 = isHttp2,
      remoteAddress = remoteAddress,
      cookies = cookies
    )
  }
}
