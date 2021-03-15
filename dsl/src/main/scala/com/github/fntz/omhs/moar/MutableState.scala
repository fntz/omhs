package com.github.fntz.omhs.moar

import com.github.fntz.omhs.{AsyncResult, CommonResponse}
import io.netty.handler.codec.http.cookie.{Cookie, DefaultCookie, ServerCookieEncoder}
import io.netty.handler.codec.http.{HttpHeaderNames, HttpResponseStatus}
import org.slf4j.LoggerFactory

import scala.collection.mutable.{ArrayBuffer => AB}

class MutableState(var status: HttpResponseStatus,
                   var contentType: String,
                   var cookies: AB[String],
                   var headers: AB[(String, String)],
                   private var clazz: Class[_]
                  ) {
  import MutableState._

  private val logger = LoggerFactory.getLogger(clazz)
  private val filledStates = new AB[FilledStates.Value](FilledStates.values.size)

  def setStatus(responseStatus: HttpResponseStatus): Unit = {
    check(FilledStates.status, s"${this.status}", s"$responseStatus")
    this.status = responseStatus
  }

  def setStatus(intStatus: Int): Unit = {
    setStatus(HttpResponseStatus.valueOf(intStatus))
  }

  def setContentType(contentType: String): Unit = {
    check(FilledStates.contentType, this.contentType, contentType)
    this.contentType = contentType
  }

  def setCookie(name: String, value: String)(implicit enc: ServerCookieEncoder): Unit = {
    val cookie = new DefaultCookie(name, value)
    setCookie(cookie)(enc)
  }

  def setCookie(cookie: Cookie)(implicit enc: ServerCookieEncoder): Unit = {
    this.cookies += enc.encode(cookie)
  }

  def setHeader(name: String, value: String): Unit = {
    if (this.headers.map(_._1).contains(name)) {
      logger.warn(s"Header $name already present")
    }
    this.headers += (name -> value)
  }

  def transform(ar: AsyncResult): AsyncResult = {
    ar.current match {
      case x: CommonResponse =>
        AsyncResult.completed(mergeTo(x))

      case _ => ar
    }
  }

  def mergeTo(x: CommonResponse): CommonResponse = {
    val cs = cookies.map { c =>
      (HttpHeaderNames.SET_COOKIE.toString, c)
    }
    CommonResponse(
      status = status,
      contentType = contentType,
      content = x.content,
      headers = cs ++ headers
    )
  }

  override def toString: String = {
    s"$status $contentType"
  }

  private def check(value: FilledStates.Value, oldV: String, newV: String): Unit = {
    if (filledStates.contains(value)) {
      logger.warn(s"Double changing in $value: $oldV->$newV")
    } else {
      filledStates += value
    }
  }
}
object MutableState {
  private object FilledStates extends Enumeration {
    type FilledState = Value
    val status, contentType = Value
  }
  def empty(clazz: Class[_]) = new MutableState(HttpResponseStatus.OK,
    "text/plain",
    AB[String](),
    AB[(String, String)](),
    clazz
  )
}
