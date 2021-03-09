package com.github.fntz.omhs

import java.util.UUID

object ParamParser {
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

case class ParseResult(success: Boolean, defs: List[PathParamDef[_]]) {
  val isSuccess: Boolean = success
}
object ParseResult {
  val failed: ParseResult = ParseResult(success = false, Nil)
}