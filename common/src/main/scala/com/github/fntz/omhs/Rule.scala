package com.github.fntz.omhs

import com.github.fntz.omhs.HttpMethod.HttpMethod

import scala.collection.mutable.{ArrayBuffer => AB}

class Route {
  private val rules: AB[Rule] = new AB[Rule]()
  private val rulesAndF: AB[RuleAndF] = new AB[RuleAndF]()
  private var unhandledDefault = (x: UnhandledReason) => {
    "boom"
  }

  def current: Vector[Rule] = rules.toVector

  def currentF: Vector[RuleAndF] = rulesAndF.toVector

  def addF[T <: RuleAndF](x: T): Route = {
    rulesAndF += x
    this
  }

  def addRule(x: Rule): Route = {
    rules += x
    this
  }

  def ::(other: Route): Route = {
    rules ++= other.rules
    this
  }

  def ++[T <: RuleAndF](other: T): Route = {
    addF(other)
    this
  }

  def ::(rule: Rule): Route = {
    addRule(rule)
    this
  }

  def onUnhandled(f: UnhandledReason => String): Route = {
    unhandledDefault = f
    this
  }

  def currentUnhandled: UnhandledReason => String = {
    unhandledDefault
  }

  override def toString: String = {
    rules.map(_.toString).mkString("\n")
  }
}

abstract class RuleAndF(val rule: Rule) {
  def ++[T <: RuleAndF](other: T): Route = {
    val r = new Route
    r.addF(this)
     .addF(other)
  }

  def run(defs: Vector[ParamDef[_]]): String
}
case class RuleAndF0(override val rule: Rule,
                     func: Function0[String]) extends RuleAndF(rule) {
  override def run(defs: Vector[ParamDef[_]]): String = {
    func.apply()
  }
}
case class RuleAndF1[T](override val rule: Rule,
                        func: Function1[T, String]) extends RuleAndF(rule) {
  override def run(defs: Vector[ParamDef[_]]): String = {
    func.apply(defs(0).value.asInstanceOf[T])
  }
}
case class RuleAndF2[T1, T2](override val rule: Rule,
                             func: Function2[T1, T2, String])
  extends RuleAndF(rule) {
  override def run(defs: Vector[ParamDef[_]]): String = {
    func.apply(defs(0).value.asInstanceOf[T1], defs(1).value.asInstanceOf[T2])
  }
}

object RuleDSL {
  implicit class RuleExt(val rule: Rule) extends AnyVal {
    def ~>(f: () => String) = RuleAndF0(rule, f)
    def ~>[T](f: T => String) = RuleAndF1(rule, f)
    def ~>[T1, T2](f: (T1, T2) => String) = RuleAndF2(rule, f)
  }
}

class Rule(val method: HttpMethod) {
  private val paths: AB[Param] = new AB[Param]()

  private var reader: BodyReader[_] = null // todo None instead

  def params: Vector[Param] = paths.toVector

  def currentReader = reader

  // : Reader
  def body[T]()(implicit reader: BodyReader[T]) = {
    this.reader = reader
    this
  }

  def path(x: String): Rule = {
    paths += HardCodedParam(x)
    this
  }

  def path(x: Param): Rule = {
    paths += x
    this
  }

  def cookie(x: String): Rule = {
    this
  }

  def header(x: String): Rule = {
    this
  }

  def ::(other: Rule): Route = {
    val r = new Route
    r.addRule(this)
      .addRule(other)
  }

  override def toString: String = {
    s"$method ${paths.mkString("/")}"
  }

}

object Get {
  def apply(): Rule = new Rule(HttpMethod.GET)
}
object Post {
  def apply(): Rule = new Rule(HttpMethod.POST)
}
object Put {
  def apply(): Rule = new Rule(HttpMethod.PUT)
}
object Delete {
  def apply(): Rule = new Rule(HttpMethod.DELETE)
}
