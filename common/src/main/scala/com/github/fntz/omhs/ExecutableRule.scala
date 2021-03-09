package com.github.fntz.omhs

import com.github.fntz.omhs.internal.ParamDef
import io.netty.handler.codec.http.HttpMethod

class ExecutableRule(val rule: Rule) {

  lazy val method: HttpMethod = rule.method

  def ::[T <: ExecutableRule](other: T): Route = {
    val r = new Route
    r.addRule(this)
      .addRule(other)
  }

  def run(defs: List[ParamDef[_]]): AsyncResult = {
    AsyncResult.completed(CommonResponse.empty)
  }

  override def toString: String = {
    s"Rule[$rule]"
  }
}

