package com.github.fntz.omhs

import scala.util.matching.Regex
import java.util.UUID
import scala.util.Try

sealed trait Param {
  def isRestParam: Boolean = false
  def isUserDefined: Boolean = false
  def check(in: String): Boolean
}
case class HardCodedParam(value: String) extends Param {
  override val isUserDefined: Boolean = true
  override def check(in: String): Boolean = in == value
}
case object StringParam extends Param {
  override def check(in: String): Boolean = true
}
case object LongParam extends Param {
  override def check(in: String): Boolean = {
    if (in.isEmpty || in.contains(".")) {
      false
    } else {
      Try(in.toLong).isSuccess
    }
  }
}
case object UUIDParam extends Param {
  override def check(in: String): Boolean = {
    if (in.length == 36) {
      Try(UUID.fromString(in)).isSuccess
    } else {
      false
    }
  }
}

case class RegexParam(re: Regex) extends Param {
  override def check(in: String): Boolean = {
    re.findFirstIn(in).isDefined
  }
}

case object * extends Param {
  override def check(in: String): Boolean = true
  override def isRestParam: Boolean = true
}

case class ParseResult(success: Boolean, defs: Vector[ParamDef[_]]) {
  val isSuccess: Boolean = success
}
object ParseResult {
  val failed: ParseResult = ParseResult(success = false, Vector.empty)
}

object Param {

  def convert(p: Param, in: String): ParamDef[_] = {
    p match {
      case HardCodedParam(value) => EmptyDef(value)
      case LongParam => LongDef(in.toLong)
      case StringParam => StringDef(in)
      case UUIDParam => UUIDDef(UUID.fromString(in))
      case RegexParam(re) => RegexDef(re.findFirstMatchIn(in).get.toString) // TODO
      case * => TailDef(Vector(in)) // unreachable
    }
  }

  def parse(target: String, params: Vector[Param]): ParseResult = {
    val tmp = target.replaceAll("""\?.*""", "")
      .split("/").map(_.trim).filterNot(_.isEmpty)
    if (tmp.isEmpty) {
      ParseResult.failed
    } else {
      var i = 0
      val tmpLength = tmp.length - 1
      val paramsLength = params.length - 1
      val buffer = scala.collection.mutable.ArrayBuffer.empty[ParamDef[_]]
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
                buffer += TailDef(tmp.slice(i, tmpLength + 1).toVector)
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
              buffer += TailDef(tmp.slice(i, tmpLength + 1).toVector)
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
        ParseResult(success = true, buffer.toVector)
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