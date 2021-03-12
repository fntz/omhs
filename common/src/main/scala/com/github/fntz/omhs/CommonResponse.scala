package com.github.fntz.omhs

import io.netty.handler.codec.http.{HttpHeaderValues, HttpResponseStatus}
import io.netty.util.CharsetUtil

sealed trait Response

/**
 * @param status - valid response status
 * @param contentType - content-type for content
 * @param content - information for sending to client
 * @param headers - additional headers if needed
 */
case class CommonResponse(
                           status: HttpResponseStatus,
                           contentType: String,
                           content: Array[Byte],
                           headers: Map[String, String] = Map.empty
                         ) extends Response {
  def withHeaders(add: Map[String, String]): CommonResponse = {
    copy(headers = headers ++ add)
  }

  def withHeader(header: String, value: String): CommonResponse = {
    copy(headers = headers ++ Map(header -> value))
  }
}

case class StreamResponse(contentType: String,
                          it: Iterator[Array[Byte]],
                          headers: Map[String, String] = Map.empty
                         ) extends Response {
  def withHeaders(add: Map[String, String]): StreamResponse = {
    copy(headers = headers ++ add)
  }

  def withHeader(header: String, value: String): StreamResponse = {
    copy(headers = headers ++ Map(header -> value))
  }
}

object CommonResponse {
  val empty = new CommonResponse(
    status = HttpResponseStatus.OK,
    contentType = HttpHeaderValues.TEXT_PLAIN.toString,
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
