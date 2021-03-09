package com.github.fntz.omhs

import com.github.fntz.omhs.internal.ExecutableRule
import io.netty.handler.codec.http.FullHttpResponse

import scala.collection.mutable.{ArrayBuffer => AB}

class Route {
  private val rules: AB[ExecutableRule] = new AB[ExecutableRule]()
  private var unhandledDefault = (reason: UnhandledReason) => {
    val result = reason match {
      case PathNotFound(value) => (404, value)
      case CookieIsMissing(value) => (400, s"cookie: $value is missing")
      case HeaderIsMissing(value) => (400, s"header: $value is missing")
      case BodyIsUnparsable(ex) => (400, s"body is incorrect: $ex")
      case FilesIsUnparsable(_) => (500, s"files is corrupted")
      case QueryIsUnparsable(params) =>
        val q = params.map(x => s"${x._1}=${x._2.mkString(",")}").mkString(",")
        (400, s"query is unparsable: $q")
      case UnhandledException(ex) => (500, s"$ex")
    }
    CommonResponse(
      status = result._1,
      contentType = "text/plain",
      content = result._2
    )
  }
  private var defaultResponseHandler = (response: FullHttpResponse) => response

  def current: Vector[ExecutableRule] = rules.toVector

  // update response somehow
  def rewrite(response: FullHttpResponse): FullHttpResponse =
    defaultResponseHandler.apply(response)

  def onEveryResponse(f: FullHttpResponse => FullHttpResponse): Route = {
    defaultResponseHandler = f
    this
  }

  def addRule(x: ExecutableRule): Route = {
    rules += x
    this
  }

  def ::(other: Route): Route = {
    rules ++= other.rules
    this
  }

  def ::[T <: ExecutableRule](other: T): Route = {
    addRule(other)
    this
  }

  def onUnhandled(f: UnhandledReason => CommonResponse): Route = {
    unhandledDefault = f
    this
  }

  def currentUnhandled: UnhandledReason => CommonResponse = {
    unhandledDefault
  }

  override def toString: String = {
    rules.map(_.toString).mkString("\n")
  }
}
