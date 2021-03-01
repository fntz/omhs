package com.github.fntz.omhs

import com.github.fntz.omhs.HttpMethod.HttpMethod

import scala.collection.mutable.{ArrayBuffer => AB}

class Route {
  private val rules: AB[Rule] = new AB[Rule]()

  def current: Vector[Rule] = rules.toVector

  def addRule(x: Rule) = {
    rules += x
    this
  }

  def ::(other: Route) = {
    other.rules.foreach(addRule)
  }

  def ::(rule: Rule) = {
    addRule(rule)
  }

  override def toString: String = {
    rules.map(_.toString).mkString("\n")
  }
}

class Rule(val method: HttpMethod) {
  private val paths: AB[Param] = new AB[Param]()

  def draw: Vector[Param] = paths.toVector

  // : Reader
  def body[T] = ???

  def path(x: String) = {
    paths += HardCodedParam(x)
    this
  }

  def path(x: Param) = {
    paths += x
    this
  }

  def cookie(x: String) = {
    this
  }

  def header(x: String) = {
    this
  }

  def ::(other: Rule) = {
    val r = new Route
    r.addRule(this)
      .addRule(other)
  }

  override def toString: String = {
    s"$method ${paths.mkString("/")}"
  }

}

object Get {
  def apply(): Rule = new Rule(HttpMethod.Get)
}
object Post {
  def apply(): Rule = new Rule(HttpMethod.Post)
}
object Put {
  def apply(): Rule = new Rule(HttpMethod.Put)
}
object Delete {
  def apply(): Rule = new Rule(HttpMethod.Delete)
}
