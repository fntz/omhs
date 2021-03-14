package com.github.fntz.omhs.moar

import com.github.fntz.omhs.{AsyncResult, CommonResponse}
import io.netty.handler.codec.http.HttpResponseStatus
import scala.collection.mutable.{ArrayBuffer => AB}

import org.slf4j.LoggerFactory

class MutableState(var status: HttpResponseStatus,
                   var contentType: String,
                   private var clazz: Class[_]
                  ) {
  import MutableState._

  private val logger = LoggerFactory.getLogger(clazz)
  private val filledStates = new AB[FilledStates.Value](FilledStates.values.size)

  def setStatus(responseStatus: HttpResponseStatus): Unit = {
    check(FilledStates.status, this.status, responseStatus)
    this.status = responseStatus
  }

  def setStatus(intStatus: Int): Unit = {
    setStatus(HttpResponseStatus.valueOf(intStatus))
  }

  def setContentType(contentType: String): Unit = {
    check(FilledStates.contentType, this.contentType, contentType)
    this.contentType = contentType
  }

  private def check(value: FilledStates.Value, oldV: AnyRef, newV: AnyRef): Unit = {
    if (filledStates.contains(value)) {
      logger.warn(s"Double changing in $value: $oldV->$newV")
    } else {
      filledStates += value
    }
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
  private object FilledStates extends Enumeration {
    type FilledState = Value
    val status, contentType = Value
  }
  def empty(clazz: Class[_]) = new MutableState(HttpResponseStatus.OK,
    "text/plain",
    clazz
  )
}
