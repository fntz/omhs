package com.github.fntz.omhs.internal

import java.util.UUID
import scala.collection.mutable.{ArrayBuffer => AB}

object ParamsParser {
  def convert(p: PathParam, in: String): PathParamDef[_] = {
    p match {
      case HardCodedParam(value) => EmptyDef(value)
      case _: LongParam => LongDef(in.toLong)
      case _: StringParam => StringDef(in)
      case _: UUIDParam => UUIDDef(UUID.fromString(in))
      // NOTE: normal, because we do it after `check`-call
      case RegexParam(re, _, _) => RegexDef(re.findFirstMatchIn(in).get.toString)
      // NOTE: `xs` contains alternatives, but `in` is needed for us
      case _: AlternativeParam => AlternativeDef(in)
      // NOTE: unreachable
      case _ => TailDef(List(in))
    }
  }

  // todo check perf against tail-rec impl
  def parse(target: String, params: Vector[PathParam]): ParseResult = {
    val path = target.replaceAll("""\?.*""", "")
      .split("/").map(_.trim).filterNot(_.isEmpty)
    if (params.forall(x => x.isUserDefined && x.name == target)) {
      ParseResult(success = true, EmptyDef(target) :: Nil)
    } else if (path.isEmpty) {
      ParseResult.failed
    } else {
      var index = 0
      val pathLength = path.length - 1
      val paramsLength = params.length - 1
      val buffer = AB[PathParamDef[_]]()
      var success = true
      var doneByRest = false
      val isPathWalk = pathLength > paramsLength
      var counter = if (isPathWalk) { pathLength } else { paramsLength }

      // long path but short params
      if (isPathWalk) {
        while(counter >= 0 && success && !doneByRest && index <= pathLength) {
          val part = path(index)
          if (index <= paramsLength) {
            val param = params(index)
            if (param.isRestParam) {
              doneByRest = true
              val end = path.slice(index, pathLength + 1).toList
              if (end.isEmpty) { // I do not need to use empty list
                success = false
                counter = 0
              }
              buffer += TailDef(end)
            } else {
              success = param.check(part)
              if (success) {
                buffer += convert(param, part)
              }
            }
          } else {
            success = false // no params
            counter = 0
          }
          index = index + 1
          counter = counter - 1
        }
      } else {
        while(counter >= 0 && success && !doneByRest && index <= paramsLength) {
          val param = params(index)
          if (param.isRestParam) {
            val end = path.slice(index, pathLength + 1).toList
            if (end.isEmpty) {
              success = false
              counter = 0
            }
            doneByRest = true
            buffer += TailDef(end)
          } else {
            if (index <= pathLength) {
              val part = path(index)
              success = param.check(part)
              if (success) {
                buffer += convert(param, part)
              }
            } else {
              success = false // no path but params are present
              counter = 0
            }
          }
          index = index + 1
          counter = counter - 1
        }
      }

      if (success) {
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