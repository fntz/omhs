package com.github.fntz.omhs.util

import com.github.fntz.omhs.internal.{ExecutableRule, ParamsParser, ParseResult}
import io.netty.handler.codec.http.{FullHttpRequest, HttpMethod, QueryStringDecoder}

object FullHttpRequestImplicits {

  private type R = Option[(ExecutableRule, ParseResult)]

  implicit class FullHttpRequestExt(val request: FullHttpRequest) extends AnyVal {
    def findRule(byMethod: Map[HttpMethod, Vector[ExecutableRule]]): R = {
      val decoder = new QueryStringDecoder(request.uri)
      val target = decoder.rawPath()
      byMethod
        .getOrElse(request.method(), Vector.empty)
        .map { x => (x, ParamsParser.parse(target, x.rule.currentParams)) }
        .find(_._2.isSuccess)
    }
  }

}
