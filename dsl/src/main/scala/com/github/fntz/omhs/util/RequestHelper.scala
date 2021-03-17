package com.github.fntz.omhs.util

import com.github.fntz.omhs._
import com.github.fntz.omhs.internal._
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType
import io.netty.handler.codec.http.multipart.{HttpPostRequestDecoder, MixedFileUpload}
import io.netty.handler.codec.http.{FullHttpRequest, QueryStringDecoder}
import io.netty.util.CharsetUtil

import scala.language.existentials

private[omhs] object RequestHelper {

  import CollectionsConverters._
  import SetupImplicits._

  type E[R] = Either[UnhandledReason, List[R]]

  def fetchAdditionalDefs(request: FullHttpRequest, rule: Rule, setup: Setup): E[ParamDef[_]] = {
    val filesDef = fetchFileDef(request, rule)

    val bodyDefE = fetchBodyDef(request, rule)

    val headersDefE = fetchHeadersDef(request, rule)

    val cookieDefE = fetchCookies(request, rule, setup)

    val queryDefE = fetchQuery(request, rule)

    val current = List(bodyDefE, headersDefE, filesDef, cookieDefE, queryDefE)

    current.collectFirst {
      case Left(e) => Left(e)
    }.getOrElse {
      Right(current.collect { case Right(v) => v }.reduce(_ ++ _))
    }
  }

  private def fetchQuery(request: FullHttpRequest, rule: Rule): E[QueryDef[_]] = {
    if (rule.isNeedToDecodeQuery) {
      val decoder = new QueryStringDecoder(request.uri)
      val queryParams = decoder.parameters()
          .toScala
          .map(x => x._1 -> x._2.toScala)
      rule.currentQueryReader.read(queryParams).map { result =>
        Right(List(QueryDef(result)))
      }.getOrElse(Left(QueryIsUnparsable(queryParams)))
    } else {
      Right(Nil)
    }
  }

  private def fetchFileDef(request: FullHttpRequest, rule: Rule): E[FileDef] = {
    if (rule.isNeedToFetchFiles) {
      val decoder = new HttpPostRequestDecoder(request)
      try {
        val files = decoder.getBodyHttpDatas.toScala.collect {
          case data: MixedFileUpload if data.getHttpDataType == HttpDataType.FileUpload =>
            data.copy()
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

  private def fetchBodyDef(request: FullHttpRequest, rule: Rule): E[BodyDef[_]] = {
    if (rule.isNeedToParseBody) {
      if (request.decoderResult().isSuccess) {
        try {
          val strBody = request.content.toString(CharsetUtil.UTF_8)
          Right(List(BodyDef(rule.currentBodyReader.read(strBody))))
        } catch {
          case ex: Throwable =>
            Left(BodyIsUnparsable(ex))
        }

      } else {
        Left(BodyIsUnparsable(request.decoderResult().cause()))
      }
    } else {
      Right(Nil)
    }
  }

  private def fetchCookies(request: FullHttpRequest,
                           rule: Rule,
                           setup: Setup
                          ): E[CookieDef] = {
    if (rule.currentCookies.nonEmpty) {
      val cookies = setup.decode(request)
      val result = rule.currentCookies.map { need =>
        need.cookieName -> cookies.find(need.cookieName == _.name())
      }
      val nullCookie = result.find(_._2.isEmpty)
      nullCookie match {
        case Some(v) => Left(CookieIsMissing(v._1))
        case _ =>
          val tmp = result.flatMap(_._2).map(x => CookieDef(x)).toList
          Right(tmp)
      }
    } else {
      Right(Nil)
    }
  }

  private def fetchHeadersDef(request: FullHttpRequest, rule: Rule): E[HeaderDef] = {
    val fetchedHeaders = if (rule.currentHeaders.nonEmpty) {
      rule.currentHeaders.map { need =>
        (request.headers.get(need.headerName), need)
      }.toList
    } else {
      Nil
    }
    val nullHeader = fetchedHeaders.find(_._1 == null)
    if (nullHeader.isEmpty) {
      val headerDefs = fetchedHeaders.map(x => HeaderDef(x._1))
      Right(headerDefs)
    } else {
      Left(HeaderIsMissing(nullHeader.get._2.headerName))
    }
  }

}
