package com.github.fntz.omhs

import io.netty.handler.codec.http.{HttpHeaderValues, HttpResponseStatus}
import io.netty.util.CharsetUtil

// content-type
case class CommonResponse(
                           status: HttpResponseStatus,
                           contentType: String,
                           content: Array[Byte]
                         )

object CommonResponse {
  val empty = new CommonResponse(
    status = HttpResponseStatus.OK,
    contentType = "text/plain",
    content = Array.emptyByteArray
  )

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

  def plain(content: String): CommonResponse = {
    plain(HttpResponseStatus.OK.code(), content)
  }

  def plain(status: Int, content: String): CommonResponse = {
    CommonResponse(
      status = status,
      contentType = HttpHeaderValues.TEXT_PLAIN.toString,
      content = content
    )
  }

  def apply(status: Int, contentType: String,
            content: String): CommonResponse = {
    new CommonResponse(HttpResponseStatus.valueOf(status), contentType,
      content.getBytes(CharsetUtil.UTF_8))
  }
}
