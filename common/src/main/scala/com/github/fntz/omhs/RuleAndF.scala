package com.github.fntz.omhs

// todo rename somehow
abstract class RuleAndF(val rule: Rule) {

  def ::[T <: RuleAndF](other: T): Route = {
    val r = new Route
    r.addRule(this)
      .addRule(other)
  }

  def run(defs: List[ParamDef[_]]): AsyncResult

  override def toString: String = {
    s"Rule[$rule]"
  }
}

