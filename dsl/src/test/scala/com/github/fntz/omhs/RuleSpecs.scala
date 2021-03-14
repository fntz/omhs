package com.github.fntz.omhs

import com.github.fntz.omhs.internal.{AlternativeParam, CookieParam, FileParam, HardCodedParam, HeaderParam, LongParam, StringParam, TailDef, TailParam, UUIDParam}
import io.netty.handler.codec.http.HttpMethod
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class RuleSpecs extends Specification {

  case class Foo(id: Int)
  implicit val queryReader = new QueryReader[Foo] {
    override def read(queries: Map[String, Iterable[String]]): Option[Foo] =
      Some(Foo(1))
  }
  implicit val fooReader = new BodyReader[Foo] {
    override def read(str: String): Foo = Foo(1)
  }

  "rule" should {
    "set method" in new Test {
      rule.withMethod(HttpMethod.PATCH)
      rule.currentMethod ==== HttpMethod.PATCH
    }

    "set paths" in new Test {
      rule.currentParams must be empty

      val p1 = LongParam("a", None)
      val p2 = StringParam("b", None)
      val p3 = TailParam
      val p4 = AlternativeParam(List("a", "b", "c"))

      rule.path("test")
        .path(p1)
        .path(p2)
        .path(p3)
        .path(p4)

      rule.currentParams must have size(5)

      rule.currentParams ==== Vector(
        HardCodedParam("test"),
        p1, p2, p3, p4
      )
    }

    "set bodyReader" in new Test {
      rule.isNeedToParseBody must beFalse
      rule.body[Foo]
      rule.isNeedToParseBody must beTrue
    }

    "set queryReader" in new Test {


      rule.isNeedToDecodeQuery must beFalse
      rule.query[Foo]
      rule.isNeedToDecodeQuery must beTrue
    }

    "use with request" in new Test {
      rule.isNeedToPassCurrentRequest must beFalse
      rule.withRequest()
      rule.isNeedToPassCurrentRequest must beTrue
    }

    "use with file" in new Test {
      rule.isNeedToFetchFiles must beFalse
      rule.withFiles(FileParam("file", None))
      rule.isNeedToFetchFiles must beTrue
    }

    "add headers" in new Test {
      rule.currentHeaders must be empty

      val h1 = HeaderParam("a", None)
      rule.header(h1)
      rule.currentHeaders ==== Vector(h1)
      val h2 = HeaderParam("b", None)
      rule.header(h2)
      rule.currentHeaders ==== Vector(h1, h2)
      val h3 = HeaderParam("c", None)
      rule.header(h3)
      rule.currentHeaders ==== Vector(h1, h2, h3)
    }

    "add cookie" in new Test {
      rule.currentCookies must be empty

      val h1 = CookieParam("a", None)
      rule.cookie(h1)
      rule.currentCookies ==== Vector(h1)
      val h2 = CookieParam("b", None)
      rule.cookie(h2)
      rule.currentCookies ==== Vector(h1, h2)
      val h3 = CookieParam("c", None)
      rule.cookie(h3)
      rule.currentCookies ==== Vector(h1, h2, h3)
    }

    "same: copy rule" in new Test {
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
      copy.currentParams ==== rule.currentParams
      copy.currentCookies ==== rule.currentCookies
      copy.currentHeaders ==== rule.currentHeaders
      copy.isNeedToParseBody ==== rule.isNeedToParseBody
      copy.currentFileParam ==== rule.currentFileParam
      copy.currentMethod === rule.currentMethod
      copy.isNeedToDecodeQuery ==== rule.isNeedToDecodeQuery

      // do not affect original
      copy.path(p4)
      rule.currentParams ==== Vector(p1, p2, p3)
      copy.currentParams ==== Vector(p1, p2, p3, p4)

      // do not affect copy
      rule.path(p5)
      rule.currentParams ==== Vector(p1, p2, p3, p5)
      copy.currentParams ==== Vector(p1, p2, p3, p4)

      // clear paths
      val copy1 = rule.same(true)
      copy1.currentParams must be empty

      copy1.currentCookies ==== rule.currentCookies
      copy1.currentHeaders ==== rule.currentHeaders
      copy1.isNeedToParseBody ==== rule.isNeedToParseBody
      copy1.currentFileParam ==== rule.currentFileParam
      copy1.currentMethod === rule.currentMethod
      copy1.isNeedToDecodeQuery ==== rule.isNeedToDecodeQuery
    }
  }

  class Test() extends Scope {
    val rule = new Rule()
  }

}
