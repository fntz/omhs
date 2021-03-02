package com.github.fntz.omhs

abstract class RuleAndF(val rule: Rule) {
  def ::[T <: RuleAndF](other: T): Route = {
    val r = new Route
    r.addRule(this)
      .addRule(other)
  }

  def run(defs: Vector[ParamDef[_]]): CommonResponse
}
case class RuleAndF0[R](override val rule: Rule,
                        func: Function0[R])(implicit w: BodyWriter[R]) extends RuleAndF(rule) {
  override def run(defs: Vector[ParamDef[_]]): CommonResponse = {
    w.write(func.apply())
  }
}
case class RuleAndF1[T, R](override val rule: Rule,
                           func: Function1[T, R])
                          (implicit w: BodyWriter[R]) extends RuleAndF(rule) {
  override def run(defs: Vector[ParamDef[_]]): CommonResponse = {
    w.write(func.apply(defs(0).value.asInstanceOf[T]))
  }
}
case class RuleAndF2[T1, T2, R](override val rule: Rule,
                                func: Function2[T1, T2, R])(implicit w: BodyWriter[R])
  extends RuleAndF(rule) {
  override def run(defs: Vector[ParamDef[_]]): CommonResponse = {
    w.write(func.apply(defs(0).value.asInstanceOf[T1], defs(1).value.asInstanceOf[T2]))
  }
}
