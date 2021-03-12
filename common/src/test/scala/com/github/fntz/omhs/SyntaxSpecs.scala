package com.github.fntz.omhs

import com.github.fntz.omhs.internal.{AlternativeParam, HardCodedParam}
import org.specs2.mutable.Specification

class SyntaxSpecs extends Specification {

  import RoutingDSL._

  case class Foo(id: Int)
  implicit val fooReader: BodyReader[Foo] = (_: String) => Foo(1)
  implicit val fooQReader: QueryReader[Foo] = (_: Map[String, Iterable[String]]) =>
    Some(Foo(1))

  "syntax/dsl" should {
    "build for string" in {
      ("a" / "b").rule.currentParams ==== Vector(h("a"), h("b"))
      ("a" / string / uuid).rule.currentParams ==== Vector(h("a"), string, uuid)
      ("a" / *).rule.currentParams ==== Vector(h("a"), *)
      ("a" | "b").rule.currentParams ==== Vector(AlternativeParam(List("a", "b")))
      ("a" / ("b" | "c")).rule.currentParams ==== Vector(
        h("a"), AlternativeParam(List("b", "c"))
      )
    }

    "build for params" in {
      (string / "a").rule.currentParams ==== Vector(string, h("a"))
      (string / uuid).rule.currentParams ==== Vector(string, uuid)
      (string / "a" / long).rule.currentParams ==== Vector(string, h("a"), long)
      (string / *).rule.currentParams ==== Vector(string, *)
      (string / ("a" | "b")).rule.currentParams ==== Vector(string, AlternativeParam(List("a", "b")))
    }

    "with body" in {
      val r1 = string <<< body[Foo]
      r1.rule.currentParams ==== Vector(string)
      r1.rule.isNeedToParseBody must beTrue

      val r2 = "a" <<< body[Foo]
      r2.rule.currentParams ==== Vector(h("a"))
      r2.rule.isNeedToParseBody must beTrue
    }

    "with file" in {
      val r1 = string <<< file
      r1.rule.currentParams ==== Vector(string)
      r1.rule.currentFileParam ==== file

      val r2 = "a" <<< file
      r2.rule.currentParams ==== Vector(h("a"))
      r1.rule.currentFileParam ==== file
    }

    "with query" in {
      val r1 = string :? query[Foo]
      r1.rule.currentParams ==== Vector(string)
      r1.rule.isNeedToDecodeQuery must beTrue

      val r2 = "a" :? query[Foo]
      r2.rule.currentParams ==== Vector(h("a"))
      r2.rule.isNeedToDecodeQuery must beTrue
    }

    "with headers" in {
      val r1 = string << header("a")
      r1.rule.currentParams ==== Vector(string)
      r1.rule.currentHeaders ==== Vector(header("a"))

      val r2 = "a" << header("a")
      r2.rule.currentParams ==== Vector(h("a"))
      r2.rule.currentHeaders ==== Vector(header("a"))
    }

    "with cookie" in {
      val r1 = string << cookie("a")
      r1.rule.currentParams ==== Vector(string)
      r1.rule.currentCookies ==== Vector(cookie("a"))

      val r2 = "a" << cookie("a")
      r2.rule.currentParams ==== Vector(h("a"))
      r2.rule.currentCookies ==== Vector(cookie("a"))
    }
  }

  private def h(s: String): HardCodedParam = HardCodedParam(s)

}
