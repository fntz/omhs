package com.github.fntz.omhs

import io.netty.handler.codec.http.{HttpHeaderValues, HttpResponseStatus}
import io.netty.util.CharsetUtil

// content-type
sealed trait Response
case class CommonResponse(
                         status: HttpResponseStatus,
                         contentType: String,
                         content: Array[Byte]
                         ) extends Response

case class AsyncResponse[T](asyncResult: AsyncResult[T]) extends Response
object CommonResponse {
  val empty = new CommonResponse(HttpResponseStatus.OK, "text/plain", "")

  def json(content: String): CommonResponse = {
    json(HttpResponseStatus.OK.code(), content)
  }

  def json(status: Int, content: String): CommonResponse = {
    CommonResponse(
      status = status,
      contentType = HttpHeaderValues.APPLICATION_JSON.toString,
      content = content
    )
  }

  def apply(status: Int, contentType: String,
            content: String): CommonResponse = {
    new CommonResponse(HttpResponseStatus.valueOf(status), contentType,
      content.getBytes(CharsetUtil.UTF_8))
  }
}
