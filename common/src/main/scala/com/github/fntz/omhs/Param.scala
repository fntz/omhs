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
sealed trait PathParam extends Param
case class HardCodedParam(value: String) extends PathParam {
  override val isUserDefined: Boolean = true
  override def check(in: String): Boolean = in == value

  override def toString: String = value
}
case object StringParam extends PathParam {
  override def check(in: String): Boolean = true

  override def toString: String = ":string"
}
case object LongParam extends PathParam {
  override def check(in: String): Boolean = {
    if (in.isEmpty || in.contains(".")) {
      false
    } else {
      Try(in.toLong).isSuccess
    }
  }

  override def toString: String = ":long"
}
case object UUIDParam extends PathParam {
  override def check(in: String): Boolean = {
    if (in.length == 36) {
      Try(UUID.fromString(in)).isSuccess
    } else {
      false
    }
  }

  override def toString: String = ":uuid"
}

case class RegexParam(re: Regex) extends PathParam {
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

case class HeaderParam(name: String) extends Param {
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
      case LongParam => LongDef(in.toLong)
      case StringParam => StringDef(in)
      case UUIDParam => UUIDDef(UUID.fromString(in))
      case RegexParam(re) => RegexDef(re.findFirstMatchIn(in).get.toString) // TODO
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