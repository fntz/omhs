package com.github.fntz.omhs

// content-type
sealed trait Response
case class CommonResponse(
                         status: Int,
                         contentType: String,
                         content: String // todo should be array of bytes
                         ) extends Response
case class AsyncResponse[T](asyncResult: AsyncResult[T]) extends Response
object CommonResponse {
  val empty = new CommonResponse(200, "text/plain", "")
}
