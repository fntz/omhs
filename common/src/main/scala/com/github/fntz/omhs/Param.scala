package com.github.fntz.omhs

import scala.util.matching.Regex
import java.util.UUID
import scala.util.Try

sealed trait Param {
  def isRestParam: Boolean = false
  def isUserDefined: Boolean = false
  def isPathParam: Boolean = true
  def check(in: String): Boolean
}

// todo rename
object ParamD {
  def string(name: String, description: Option[String]): StringParam = StringParam(name, description)
  def string(name: String): StringParam = string(name, None)
  def string: StringParam = string("string-param", None)

  def long(name: String, description: Option[String]): LongParam = LongParam(name, description)
  def long(name: String): LongParam = long(name, None)
  def long: LongParam = long("long-param", None)

  def uuid(name: String, description: Option[String]): UUIDParam = UUIDParam(name, description)
  def uuid(name: String): UUIDParam = uuid(name, None)
  def uuid: UUIDParam = uuid("uuid-param", None)

  def regex(re: Regex, name: String, description: Option[String]): RegexParam =
    RegexParam(re, name, description)
  def regex(re: Regex, name: String): RegexParam = RegexParam(re, name, None)
  def regex(re: Regex): RegexParam = RegexParam(re, "regex-param", None)

  def header(headerName: String): HeaderParam =
    header(headerName, None)
  def header(headerName: String, description: Option[String]): HeaderParam =
    HeaderParam(headerName, description)

  def cookie(headerName: String): CookieParam =
    cookie(headerName, None)
  def cookie(headerName: String, description: Option[String]): CookieParam =
    CookieParam(headerName, description)

  def query[T](implicit qr: QueryReader[T]): QueryParam[T] =
    QueryParam[T]()(qr)

  def file: FileParam = FileParam("file", None)
  def file(name: String): FileParam = FileParam(name, None)
  def file(name: String, description: String): FileParam =
    FileParam(name, Some(description))

  def body[T](implicit r: BodyReader[T]): BodyParam[T] =
    BodyParam[T]()(r)
}

sealed trait PathParam extends Param {
  def name: String
  def description: Option[String]
}
case class HardCodedParam(value: String) extends PathParam {
  override val isUserDefined: Boolean = true
  override def check(in: String): Boolean = in == value
  override def name: String = value
  override def description: Option[String] = None

  override def toString: String = value
}

case class StringParam(name: String, description: Option[String]) extends PathParam {
  override def check(in: String): Boolean = true
  override def toString: String = ":string"
}

case class LongParam(name: String, description: Option[String]) extends PathParam {
  override def check(in: String): Boolean = {
    if (in.isEmpty || in.contains(".")) {
      false
    } else {
      Try(in.toLong).isSuccess
    }
  }

  override def toString: String = ":long"
}
case class UUIDParam(name: String, description: Option[String]) extends PathParam {
  override def check(in: String): Boolean = {
    if (in.length == 36) {
      Try(UUID.fromString(in)).isSuccess
    } else {
      false
    }
  }

  override def toString: String = ":uuid"
}

case class RegexParam(re: Regex, name: String, description: Option[String]) extends PathParam {
  override def check(in: String): Boolean = {
    re.findFirstIn(in).isDefined
  }

  override def toString: String = s"$re.re"
}

case object * extends PathParam {
  override def check(in: String): Boolean = true
  override def isRestParam: Boolean = true

  override def toString: String = "*"

  override def name: String = "*"

  override def description: Option[String] = None
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

object ParamDSL {
  implicit class ParamImplicits(val param: Param) extends AnyVal {
    def /(other: Param): Vector[Param] = Vector(param, other)
    def /(other: String): Vector[Param] = Vector(param, HardCodedParam(other))
  }
  implicit class StringToParamImplicits(val str: String) extends AnyVal {
    def /[T <: Param](other: T): Vector[Param] =
      Vector(HardCodedParam(str), other)
    def /(other: String): Vector[Param] =
      Vector(HardCodedParam(str), HardCodedParam(other))
  }
  implicit class VectorParamsImplicits(val xs: Vector[Param]) extends AnyVal {
    def /(o: Param): Vector[Param] = xs :+ o
    def /(o: String): Vector[Param] = xs :+ HardCodedParam(o)
  }
}