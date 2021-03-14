package com.github.fntz.omhs.moar

import com.github.fntz.omhs.{AsyncResult, CommonResponse}
import io.netty.handler.codec.http.HttpResponseStatus

class MutableState(var status: HttpResponseStatus, var contentType: String) {
  def setStatus(responseStatus: HttpResponseStatus): Unit = {
    this.status = responseStatus
  }

  def setStatus(intStatus: Int): Unit = {
    setStatus(HttpResponseStatus.valueOf(intStatus))
  }

  def setContentType(contentType: String): Unit = {
    this.contentType = contentType
  }

  def transform(ar: AsyncResult): AsyncResult = {
    ar.current match {
      case x: CommonResponse =>
        AsyncResult.completed(mergeTo(x))

      case _ => ar
    }
  }

  def mergeTo(x: CommonResponse): CommonResponse = {
    new CommonResponse(
      status = status,
      contentType = contentType,
      content = x.content
    )
  }

  override def toString: String = {
    s"$status $contentType"
  }
}
object MutableState {
  val empty = new MutableState(HttpResponseStatus.OK, "text/plain")
}
