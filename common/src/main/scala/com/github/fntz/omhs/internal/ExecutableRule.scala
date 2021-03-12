package com.github.fntz.omhs.internal

import com.github.fntz.omhs.{AsyncResult, CommonResponse, Route, Rule}
import io.netty.handler.codec.http.HttpMethod

/**
 * This class describe a function which we should run whene rule is matched
 *  for user-request
 * @param rule - set of restrictions
 */
class ExecutableRule(val rule: Rule) {

  lazy val method: HttpMethod = rule.currentMethod

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

