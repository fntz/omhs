package com.github.fntz.omhs

class ExecutableRule(val rule: Rule) {

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

