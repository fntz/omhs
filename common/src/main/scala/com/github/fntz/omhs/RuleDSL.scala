package com.github.fntz.omhs

object RuleDSL {

  implicit class RuleExt(val rule: Rule) extends AnyVal {
    def ~>[R: BodyWriter](f: () => R) = RuleAndF0(rule, f)
    def ~>[T, R: BodyWriter](f: T => R) = RuleAndF1(rule, f)
    def ~>[T1, T2, R: BodyWriter](f: (T1, T2) => R) = RuleAndF2(rule, f)
  }

}