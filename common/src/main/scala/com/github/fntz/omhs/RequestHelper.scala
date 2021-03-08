package com.github.fntz.omhs

import io.netty.handler.codec.http.cookie.ServerCookieDecoder
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType
import io.netty.handler.codec.http.multipart.{HttpPostRequestDecoder, MixedFileUpload}
import io.netty.handler.codec.http.{FullHttpRequest, HttpHeaderNames}
import io.netty.util.CharsetUtil

import scala.collection.JavaConverters._
import scala.language.existentials

object RequestHelper {

  type E[R] = Either[UnhandledReason, List[R]]

  def fetchAdditionalDefs(request: FullHttpRequest, rule: Rule): E[ParamDef[_]] = {
    val filesDef = fetchFileDef(request, rule)

    val bodyDefE = fetchBodyDef(request, rule)

    val headersDefE = fetchHeadersDef(request, rule)

    val cookieDefE = fetchCookies(request, rule)

    // todo: ++
    (bodyDefE, headersDefE, filesDef, cookieDefE) match {
      case (Right(b), Right(h), Right(f), Right(c)) => Right(b ++ h ++ f ++ c)
      case (_, _, _, Left(e)) => Left(e)
      case (_, _, Left(e), _) => Left(e)
      case (_, Left(e), _, _) => Left(e)
      case (Left(e), _, _, _) => Left(e)
    }
  }

  private def fetchFileDef(request: FullHttpRequest, rule: Rule): E[FileDef] = {
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

  private def fetchBodyDef(request: FullHttpRequest, rule: Rule): E[BodyDef[_]] = {
    if (rule.isParseBody) {
      if (request.decoderResult().isSuccess) {
        try {
          val strBody = request.content.toString(CharsetUtil.UTF_8)
          Right(List(BodyDef(rule.currentReader.read(strBody))))
        } catch {
          case _: Throwable =>
            // todo push into exception
            Left(BodyIsUnparsable)
        }

      } else {
        Left(BodyIsUnparsable)
      }
    } else {
      Right(Nil)
    }
  }

  private def fetchCookies(request: FullHttpRequest, rule: Rule): E[CookieDef] = {
    if (rule.currentCookies.nonEmpty) {
      val cookies = Option(request.headers.get(HttpHeaderNames.COOKIE)).map { x =>
        // todo strategy
        ServerCookieDecoder.STRICT.decode(x).asScala
      }.getOrElse(Set.empty)
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
