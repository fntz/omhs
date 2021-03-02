package com.github.fntz.omhs

import com.github.fntz.omhs.HttpMethod.HttpMethod

import scala.collection.mutable.{ArrayBuffer => AB}

class Rule(val method: HttpMethod) {
  private val paths: AB[Param] = new AB[Param]()

  private var reader: BodyReader[_] = null // todo None instead

  def params: Vector[Param] = paths.toVector

  def currentReader = reader

  // : Reader
  def body[T]()(implicit reader: BodyReader[T]): Rule = {
    this.reader = reader
    this
  }

  def path(x: String): Rule = {
    paths += HardCodedParam(x)
    this
  }

  def path(x: Param): Rule = {
    paths += x
    this
  }

  def cookie(x: String): Rule = {
    this
  }

  def header(x: String): Rule = {
    this
  }

//  def ::(other: Rule): Route = {
//    val r = new Route
//    r.addRule(this)
//      .addRule(other)
//  }

  override def toString: String = {
    s"$method ${paths.mkString("/")}"
  }

}

object Get {
  def apply(): Rule = new Rule(HttpMethod.GET)
}
object Post {
  def apply(): Rule = new Rule(HttpMethod.POST)
}
object Put {
  def apply(): Rule = new Rule(HttpMethod.PUT)
}
object Delete {
  def apply(): Rule = new Rule(HttpMethod.DELETE)
}
