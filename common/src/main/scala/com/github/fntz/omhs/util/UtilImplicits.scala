package com.github.fntz.omhs.util

import com.github.fntz.omhs._
import io.netty.handler.codec.http.FullHttpRequest

object UtilImplicits {

  implicit class RuleImplicits(val rule: Rule) extends AnyVal {
    def toDefs(request: FullHttpRequest): List[CurrentHttpRequestDef] = {
      if (rule.isRunWithRequest) {
        val currentRequest = CurrentHttpRequest.apply(request)
        List(CurrentHttpRequestDef(currentRequest))
      } else {
        Nil
      }
    }

    def materialize(request: FullHttpRequest): Either[UnhandledReason, List[ParamDef[_]]] = {
      RequestHelper.fetchAdditionalDefs(request, rule).map { additionalDefs =>
        additionalDefs ++ rule.toDefs(request)
      }
    }


  }

}
