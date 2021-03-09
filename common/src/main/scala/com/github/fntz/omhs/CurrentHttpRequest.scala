package com.github.fntz.omhs

import io.netty.handler.codec.http.{FullHttpRequest, HttpHeaders, HttpMethod, HttpVersion, QueryStringDecoder}
import io.netty.util.CharsetUtil

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
