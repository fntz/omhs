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
}

sealed trait PathParam extends Param
case class HardCodedParam(value: String) extends PathParam {
  override val isUserDefined: Boolean = true
  override def check(in: String): Boolean = in == value

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
}

case class HeaderParam(headerName: String, description: Option[String]) extends Param {
  override def check(in: String): Boolean = true

  override val isPathParam: Boolean = false
}

// cookie: https://www.programcreek.com/java-api-examples/?api=io.netty.handler.codec.http.Cookie

case class BodyParam[T]()(implicit val reader: BodyReader[T]) extends Param {
  override def check(in: String): Boolean = true

  override val isPathParam: Boolean = false

  override def toString: String = "body[]" // todo typeOf[T]
}

case object FileParam extends Param {
  override def check(in: String): Boolean = true

  override val isPathParam: Boolean = false

  override def toString: String = "file"
}

case class ParseResult(success: Boolean, defs: List[PathParamDef[_]]) {
  val isSuccess: Boolean = success
}
object ParseResult {
  val failed: ParseResult = ParseResult(success = false, Nil)
}

object Param {

  def convert(p: PathParam, in: String): PathParamDef[_] = {
    p match {
      case HardCodedParam(value) => EmptyDef(value)
      case _: LongParam => LongDef(in.toLong)
      case _: StringParam => StringDef(in)
      case _: UUIDParam => UUIDDef(UUID.fromString(in))
      case RegexParam(re, _, _) => RegexDef(re.findFirstMatchIn(in).get.toString) // TODO
      case _ => TailDef(List(in))                 // unreachable
    }
  }
  // todo make functional
  def parse(target: String, params: Vector[PathParam]): ParseResult = {
    val tmp = target.replaceAll("""\?.*""", "")
      .split("/").map(_.trim).filterNot(_.isEmpty)
    if (tmp.isEmpty) {
      ParseResult.failed
    } else {
      var i = 0
      val tmpLength = tmp.length - 1
      val paramsLength = params.length - 1
      val buffer = scala.collection.mutable.ArrayBuffer.empty[PathParamDef[_]]
      var flag = true
      var doneByRest = false
      val useTmp = tmpLength > paramsLength
      var counter = if (useTmp) { tmpLength } else { paramsLength }

      while(counter >= 0 && flag && !doneByRest) {
        if (useTmp) {
          // long path but short params
          if (i <= tmpLength) {
            val part = tmp(i)
            if (i <= paramsLength) {
              val param = params(i)
              if (param.isRestParam) {
                doneByRest = true
                buffer += TailDef(tmp.slice(i, tmpLength + 1).toList)
              }
              flag = param.check(part)
              if (flag && !doneByRest) {
                buffer += convert(param, part)
              }

            } else {
              flag = false // no params
              counter = 0
            }
          }
        } else {
          if (i <= paramsLength) {
            val param = params(i)
            if (param.isRestParam) {
              doneByRest = true
              buffer += TailDef(tmp.slice(i, tmpLength + 1).toList)
            }
            if (i <= tmpLength) {
              val part = tmp(i)
              flag = param.check(part)
              if (flag && !doneByRest) {
                buffer += convert(param, part)
              }
            } else {
              flag = false // no path but params are present
              counter = 0
            }
          }
        }

        i = i + 1
        counter = counter - 1
      }
      if (flag) {
        ParseResult(success = true, buffer.toList)
      } else {
        ParseResult.failed
      }
    }
  }

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