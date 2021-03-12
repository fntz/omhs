package com.github.fntz.omhs.internal

import com.github.fntz.omhs.{BodyReader, QueryReader, Rule}

import scala.util.matching.Regex
import java.util.UUID
import scala.util.Try

sealed trait Param {
  def isRestParam: Boolean = false
  def isUserDefined: Boolean = false
  def isPathParam: Boolean = true
  def check(in: String): Boolean
}

// set access level to private-package
sealed trait PathParam extends Param {
  def name: String
  def description: Option[String]
}
sealed trait PathParamNoTail extends PathParam
case class HardCodedParam(value: String) extends PathParamNoTail {
  override val isUserDefined: Boolean = true
  override def check(in: String): Boolean = in == value
  override def name: String = value
  override def description: Option[String] = None

  override def toString: String = value
}

case class StringParam(name: String, description: Option[String]) extends PathParamNoTail {
  override def check(in: String): Boolean = true
  override def toString: String = ":string"
}

case class LongParam(name: String, description: Option[String]) extends PathParamNoTail {
  override def check(in: String): Boolean = {
    if (in.isEmpty || in.contains(".")) {
      false
    } else {
      Try(in.toLong).isSuccess
    }
  }

  override def toString: String = ":long"
}
case class UUIDParam(name: String, description: Option[String]) extends PathParamNoTail {
  override def check(in: String): Boolean = {
    if (in.length == 36) {
      Try(UUID.fromString(in)).isSuccess
    } else {
      false
    }
  }

  override def toString: String = ":uuid"
}

case class RegexParam(re: Regex, name: String, description: Option[String]) extends PathParamNoTail {
  override def check(in: String): Boolean = {
    re.findFirstIn(in).isDefined
  }

  override def toString: String = s"$re.re"
}

case object TailParam extends PathParam {
  override def check(in: String): Boolean = true
  override def isRestParam: Boolean = true

  override def toString: String = "*"

  override def name: String = "*"

  override def description: Option[String] = None
}

case class AlternativeParam(paths: List[String]) extends PathParamNoTail {
  override def name: String = "|"

  override def description: Option[String] = None

  override def check(in: String): Boolean = paths.contains(in)

  override def toString: String = s"${paths.mkString("|")}"
}

case class HeaderParam(headerName: String, description: Option[String]) extends Param {
  override def check(in: String): Boolean = false

  override val isPathParam: Boolean = false
}

case class CookieParam(cookieName: String, description: Option[String]) extends Param {
  override def check(in: String): Boolean = false

  override val isPathParam: Boolean = false
}

case class BodyParam[T]()(implicit val reader: BodyReader[T]) extends Param {
  override def check(in: String): Boolean = false

  override val isPathParam: Boolean = false

  override def toString: String = "body[]" // todo typeOf[T]
}

case class FileParam(name: String, description: Option[String]) extends Param {
  override def check(in: String): Boolean = false

  override val isPathParam: Boolean = false

  override def toString: String = "file"
}

case class QueryParam[T]()(implicit val reader: QueryReader[T]) extends Param {
  override def check(in: String): Boolean = false

  override val isPathParam: Boolean = false

  override def toString: String = "query"
}
