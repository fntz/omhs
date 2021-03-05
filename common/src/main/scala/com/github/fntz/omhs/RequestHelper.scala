package com.github.fntz.omhs

import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType
import io.netty.handler.codec.http.multipart.{HttpPostRequestDecoder, MixedFileUpload}
import io.netty.util.CharsetUtil

import scala.collection.JavaConverters._
import scala.language.existentials

object RequestHelper {

  def fetchAdditionalDefs(request: FullHttpRequest, rule: Rule): Either[UnhandledReason, List[ParamDef[_]]] = {
    val filesDef = fetchFileDef(request, rule)

    val bodyDefE = fetchBodyDef(request, rule)

    val headersDefE = fetchHeadersDef(request, rule)

    // todo: ++
    (bodyDefE, headersDefE, filesDef) match {
      case (Right(b), Right(h), Right(f)) => Right(b ++ h ++ f)
      case (_, _, Left(e)) => Left(e)
      case (_, Left(e), _) => Left(e)
      case (Left(e), _, _) => Left(e)
    }
  }

  private def fetchFileDef(request: FullHttpRequest, rule: Rule): Either[UnhandledReason, List[FileDef]] = {
    if (rule.isFilePassed) {
      val decoder = new HttpPostRequestDecoder(request)
      try {
        val files = decoder.getBodyHttpDatas.asScala.collect {
          case data: MixedFileUpload if data.getHttpDataType == HttpDataType.FileUpload =>
            data
        }.toList
        Right(List(FileDef(files)))
      } catch {
        case ex: Throwable =>
          Left(FilesIsUnparsable(ex))
      } finally {
        decoder.destroy()
      }
    } else {
      Right(Nil)
    }
  }

  private def fetchBodyDef(request: FullHttpRequest, rule: Rule): Either[UnhandledReason, List[BodyDef[_]]] = {
    if (rule.isParseBody) {
      if (request.decoderResult().isSuccess) {
        val strBody = request.content.toString(CharsetUtil.UTF_8)
        Right(List(BodyDef(rule.currentReader.read(strBody))))
      } else {
        Left(BodyIsUnparsable)
      }
    } else {
      Right(Nil)
    }
  }

  private def fetchHeadersDef(request: FullHttpRequest, rule: Rule): Either[UnhandledReason, List[HeaderDef]] = {
    val fetchedHeaders = if (rule.currentHeaders.nonEmpty) {
      rule.currentHeaders.map { need =>
        (request.headers.get(need), need)
      }.toList
    } else {
      Nil
    }
    val nullHeader = fetchedHeaders.find(_._1 == null)
    if (nullHeader.isEmpty) {
      val headerDefs = fetchedHeaders.map(x => HeaderDef(x._1))
      Right(headerDefs)
    } else {
      Left(HeaderIsMissing(nullHeader.get._2))
    }
  }

}
