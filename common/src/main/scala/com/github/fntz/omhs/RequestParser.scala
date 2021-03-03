package com.github.fntz.omhs

import io.netty.handler.codec.http.FullHttpRequest
import io.netty.util.CharsetUtil
import scala.language.existentials

object RequestParser {
  def run(request: FullHttpRequest, rule: Rule): Either[UnhandledReason, List[ParamDef[_]]] = {
    val bodyDefE = if (rule.isParseBody) {
      if (request.decoderResult().isSuccess) {
        val strBody = request.content.toString(CharsetUtil.UTF_8)
        Right(List(BodyDef(rule.currentReader.read(strBody))))
      } else {
        Left(BodyIsUnparsable)
      }
    } else {
      Right(Nil)
    }

    val fetchedHeaders = if (rule.currentHeaders.nonEmpty) {
      rule.currentHeaders.map { need =>
        (request.headers.get(need), need)
      }.toList
    } else {
      Nil
    }
    val nullHeader = fetchedHeaders.find(_._1 == null)
    val headersDefE = if (nullHeader.isEmpty) { // is it correct ?
      val headerDefs = fetchedHeaders.map(x => HeaderDef(x._1))
      Right(headerDefs)
    } else {
      Left(HeaderIsMissing(nullHeader.get._2))
    }

    (bodyDefE, headersDefE) match {
      case (Right(b), Right(h)) => Right(b ++ h)
      case (_, Left(e)) => Left(e)
      case (Left(e), _) => Left(e)
    }
  }
}
