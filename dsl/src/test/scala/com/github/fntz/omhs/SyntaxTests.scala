package com.github.fntz.omhs

import com.github.fntz.omhs.internal.{AlternativeParam, HardCodedParam}
import munit.FunSuite

class SyntaxTests extends FunSuite {

  import RoutingDSL._

  case class Foo(id: Int)
  implicit val fooReader: BodyReader[Foo] = (_: String) => Foo(1)
  implicit val fooQReader: QueryReader[Foo] = (_: Map[String, Iterable[String]]) =>
    Some(Foo(1))

  test("build for string") {
    assertEquals(("a" / "b").rule.currentParams, Vector(h("a"), h("b")))
    assertEquals(("a" / string / uuid).rule.currentParams, Vector(h("a"), string, uuid))
    assertEquals(("a" / *).rule.currentParams, Vector(h("a"), *))
    assertEquals(("a" | "b").rule.currentParams, Vector(AlternativeParam(List("a", "b"))))
    assertEquals(("a" / ("b" | "c")).rule.currentParams, Vector(
      h("a"), AlternativeParam(List("b", "c"))
    ))
  }

  test("build for params") {
    assertEquals((string / "a").rule.currentParams, Vector(string, h("a")))
    assertEquals((string / uuid).rule.currentParams, Vector(string, uuid))
    assertEquals((string / "a" / long).rule.currentParams, Vector(string, h("a"), long))
    assertEquals((string / *).rule.currentParams, Vector(string, *))
    assertEquals((string / ("a" | "b")).rule.currentParams,
      Vector(string, AlternativeParam(List("a", "b")))
    )
  }

  test("with body") {
    val r1 = string <<< body[Foo]
    assertEquals(r1.rule.currentParams, Vector(string))
    assert(r1.rule.isNeedToParseBody)

    val r2 = "a" <<< body[Foo]
    assertEquals(r2.rule.currentParams, Vector(h("a")))
    assert(r2.rule.isNeedToParseBody)
  }

  test("with file") {
    val r1 = string <<< file
    assertEquals(r1.rule.currentParams, Vector(string))
    assertEquals(r1.rule.currentFileParam, file)

    val r2 = "a" <<< file
    assertEquals(r2.rule.currentParams, Vector(h("a")))
    assertEquals(r1.rule.currentFileParam, file)
  }

  test("with query") {
    val r1 = string :? query[Foo]
    assertEquals(r1.rule.currentParams, Vector(string))
    assert(r1.rule.isNeedToDecodeQuery)

    val r2 = "a" :? query[Foo]
    assertEquals(r2.rule.currentParams, Vector(h("a")))
    assert(r2.rule.isNeedToDecodeQuery)
  }

  test("with headers") {
    val r1 = string << header("a")
    assertEquals(r1.rule.currentParams, Vector(string))
    assertEquals(r1.rule.currentHeaders, Vector(header("a")))

    val r2 = "a" << header("a")
    assertEquals(r2.rule.currentParams, Vector(h("a")))
    assertEquals(r2.rule.currentHeaders, Vector(header("a")))
  }

  test("with cookies") {
    val r1 = string << cookie("a")
    assertEquals(r1.rule.currentParams, Vector(string))
    assertEquals(r1.rule.currentCookies, Vector(cookie("a")))

    val r2 = "a" << cookie("a")
    assertEquals(r2.rule.currentParams, Vector(h("a")))
    assertEquals(r2.rule.currentCookies, Vector(cookie("a")))
  }


  private def h(s: String): HardCodedParam = HardCodedParam(s)

}
