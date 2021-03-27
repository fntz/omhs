package com.github.fntz.omhs

import com.github.fntz.omhs.internal.{AlternativeParam, CookieParam, FileParam, HardCodedParam, HeaderParam, LongParam, StringParam, TailDef, TailParam, UUIDParam}
import io.netty.handler.codec.http.HttpMethod

import munit.FunSuite

class RuleTests extends FunSuite {

  case class Foo(id: Int)
  implicit val queryReader = new QueryReader[Foo] {
    override def read(queries: Map[String, Iterable[String]]): Option[Foo] =
      Some(Foo(1))
  }
  implicit val fooReader = new BodyReader[Foo] {
    override def read(str: String): Foo = Foo(1)
  }

  private def testRule(testName: String)(body: Rule => Unit)(implicit loc: munit.Location): Unit = {
    test(testName) {
      body.apply(new Rule())
    }
  }

  testRule("set method to rule") { rule =>
    rule.withMethod(HttpMethod.PATCH)
    assertEquals(rule.currentMethod, HttpMethod.PATCH)
  }

  testRule("set paths to rule") { rule =>
    assert(rule.currentParams.isEmpty)

    val p1 = LongParam("a", None)
    val p2 = StringParam("b", None)
    val p3 = TailParam
    val p4 = AlternativeParam(List("a", "b", "c"))

    rule.path("test")
      .path(p1)
      .path(p2)
      .path(p3)
      .path(p4)

    assertEquals(rule.currentParams.size, 5)
    assertEquals(rule.currentParams,
      Vector(
        HardCodedParam("test"),
        p1, p2, p3, p4
      )
    )
  }

  testRule("set bodyReader") { rule =>
    assert(!rule.isNeedToParseBody)
    rule.body[Foo]
    assert(rule.isNeedToParseBody)
  }

  testRule("set queryReader") { rule =>
    assert(!rule.isNeedToDecodeQuery)
    rule.query[Foo]
    assert(rule.isNeedToDecodeQuery)
  }

  testRule("use with request") { rule =>
    assert(!rule.isNeedToPassCurrentRequest)
    rule.withRequest()
    assert(rule.isNeedToPassCurrentRequest)
  }

  testRule("use with file") { rule =>
    assert(!rule.isNeedToFetchFiles)
    rule.withFiles(FileParam("file", None))
    assert(rule.isNeedToFetchFiles)
  }

  testRule("add headers to rule") { rule =>
    assert(rule.currentHeaders.isEmpty)
    val h1 = HeaderParam("a", None)
    rule.header(h1)
    assertEquals(rule.currentHeaders, Vector(h1))
    val h2 = HeaderParam("b", None)
    rule.header(h2)
    assertEquals(rule.currentHeaders, Vector(h1, h2))
    val h3 = HeaderParam("c", None)
    rule.header(h3)
    assertEquals(rule.currentHeaders, Vector(h1, h2, h3))
  }

  testRule("add cookies to rule") { rule =>
    assert(rule.currentCookies.isEmpty)

    val c1 = CookieParam("a", None)
    rule.cookie(c1)
    assertEquals(rule.currentCookies, Vector(c1))
    val c2 = CookieParam("b", None)
    rule.cookie(c2)
    assertEquals(rule.currentCookies, Vector(c1, c2))
    val c3 = CookieParam("c", None)
    rule.cookie(c3)
    assertEquals(rule.currentCookies, Vector(c1, c2, c3))
  }

  testRule("`same` should clone rule") { rule =>
    val c = CookieParam("c1", None)
    val h = HeaderParam("h1", None)
    val f = FileParam("f", None)
    val p1 = HardCodedParam("test")
    val p2 = AlternativeParam(List("a", "b", "c"))
    val p3 = TailParam
    val p4 = UUIDParam("asd", None)
    val p5 = StringParam("asd", None)
    rule.path(p1)
      .path(p2)
      .path(p3)
      .body[Foo]
      .query[Foo]
      .header(h)
      .cookie(c)
      .withFiles(f)
      .withRequest()
      .withMethod(HttpMethod.POST)

    val copy = rule.same
    assertEquals(copy.currentParams, rule.currentParams)
    assertEquals(copy.currentCookies, rule.currentCookies)
    assertEquals(copy.currentHeaders, rule.currentHeaders)
    assertEquals(copy.isNeedToParseBody, rule.isNeedToParseBody)
    assertEquals(copy.currentFileParam, rule.currentFileParam)
    assertEquals(copy.currentMethod, rule.currentMethod)
    assertEquals(copy.isNeedToDecodeQuery, rule.isNeedToDecodeQuery)

    // do not affect original
    copy.path(p4)
    assertEquals(rule.currentParams, Vector(p1, p2, p3))
    assertEquals(copy.currentParams, Vector(p1, p2, p3, p4))

    // do not affect copy
    rule.path(p5)
    assertEquals(rule.currentParams, Vector(p1, p2, p3, p5))
    assertEquals(copy.currentParams, Vector(p1, p2, p3, p4))

    // clear paths
    val copy1 = rule.same(true)
    assert(copy1.currentParams.isEmpty)
    assertEquals(copy1.currentCookies, rule.currentCookies)
    assertEquals(copy1.currentHeaders, rule.currentHeaders)
    assertEquals(copy1.isNeedToParseBody, rule.isNeedToParseBody)
    assertEquals(copy1.currentFileParam, rule.currentFileParam)
    assertEquals(copy1.currentMethod, rule.currentMethod)
    assertEquals(copy1.isNeedToDecodeQuery, rule.isNeedToDecodeQuery)
  }

}
