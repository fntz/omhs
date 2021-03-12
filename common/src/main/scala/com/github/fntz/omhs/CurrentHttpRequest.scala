package com.github.fntz.omhs

import io.netty.handler.codec.http.{FullHttpRequest, HttpHeaders, HttpMethod, HttpVersion, QueryStringDecoder}
import io.netty.util.CharsetUtil

/**
 * Describes incoming http request
 * @param uri - full request path
 * @param path - only path part
 * @param query - query part
 * @param method - current http method
 * @param headers - request headers
 * @param rawBody - body as a string
 * @param version - http protocol version
 * @param remoteAddress - remote address
 */
case class CurrentHttpRequest(
                             uri: String,
                             path: String,
                             query: String,
                             method: HttpMethod,
                             headers: HttpHeaders,
                             rawBody: String,
                             version: HttpVersion,
                             remoteAddress: RemoteAddress
                      )

object CurrentHttpRequest {
  def apply(request: FullHttpRequest, remoteAddress: RemoteAddress): CurrentHttpRequest = {
    val decoder = new QueryStringDecoder(request.uri)
    new CurrentHttpRequest(
      uri = request.uri(),
      path = decoder.rawPath(),
      query = decoder.rawQuery(),
      method = request.method(),
      headers = request.headers(),
      rawBody = request.content.toString(CharsetUtil.UTF_8),
      version = request.protocolVersion(),
      remoteAddress = remoteAddress
    )
  }
}
