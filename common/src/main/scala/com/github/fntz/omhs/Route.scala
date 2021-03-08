package com.github.fntz.omhs

import scala.collection.mutable.{ArrayBuffer => AB}

// todo add something like
// new Route().adRule(r).onEveryResponseHeader("h-v", "value")
// .onEveryResponseHeader((request) => ("h-v", request.get_somethng + "!")

class Route {
  private val rules: AB[ExecutableRule] = new AB[ExecutableRule]()
  private var unhandledDefault = (reason: UnhandledReason) => {
    val result = reason match {
      case PathNotFound(value) => (404, value)
      case CookieIsMissing(value) => (400, s"cookie: $value is missing")
      case HeaderIsMissing(value) => (400, s"header: $value is missing")
      case BodyIsUnparsable => (400, s"body is incorrect")
      case FilesIsUnparsable(t) => (500, s"files is corrupted")
      case UnhandledException(ex) => (500, s"$ex")
    }
    CommonResponse(
      status = result._1,
      contentType = "text/plain",
      content = result._2
    )
  }

  def current: Vector[ExecutableRule] = rules.toVector

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
