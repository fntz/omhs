package com.github.fntz.omhs

import scala.collection.mutable.{ArrayBuffer => AB}

class Route {
  private val rules: AB[RuleAndF] = new AB[RuleAndF]()
  private var unhandledDefault = (_: UnhandledReason) => {
    new CommonResponse(
      status = 500, // todo by reason plz
      contentType = "text/plain",
      content = "boom"
    )
  }

  def current: Vector[RuleAndF] = rules.toVector

  def addRule[T <: RuleAndF](x: T): Route = {
    rules += x
    this
  }

  def ::(other: Route): Route = {
    rules ++= other.rules
    this
  }

  def ::[T <: RuleAndF](other: T): Route = {
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
