package com.github.fntz.omhs

import com.github.fntz.omhs.HttpMethod.HttpMethod

import scala.collection.mutable.{ArrayBuffer => AB}

class Rule(val method: HttpMethod) {
  private val paths: AB[Param] = new AB[Param]()

  private var reader: BodyReader[_] = null // todo None instead

  private var isBodyNeeded = false

  def params: Vector[Param] = paths.toVector

  def isParseBody = isBodyNeeded

  def currentReader = reader

  // : Reader
  def body[T]()(implicit reader: BodyReader[T]): Rule = {
    this.reader = reader
    this.isBodyNeeded = true
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

  override def toString: String = {
    s"$method ${paths.mkString("/")}"
  }

}

/*
class RF(override val method: HttpMethod) extends Rule(method) {
  def run[R](x: Long, p: String, f: (Long, String) => CommonResponse)
            (implicit bw: BodyWriter[R]): CommonResponse = {
    f.apply(x, p)
  }
}
*/

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
