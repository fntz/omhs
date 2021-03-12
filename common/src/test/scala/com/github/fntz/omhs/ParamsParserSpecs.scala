package com.github.fntz.omhs

import com.github.fntz.omhs.internal._
import org.specs2.mutable.Specification

import java.util.UUID

class ParamsParserSpecs extends Specification {

  import RoutingDSL._

  private val genUuid = UUID.randomUUID()

  "params check" should {
    "#check" in {
      val a = "test" / long / "abc"
      parse("/test/123/abc", a) ==== ParseResult(
        success = true, List(EmptyDef("test"), LongDef(123), EmptyDef("abc"))
      )
      parse("/test/asd/abc", a).isSuccess must beFalse
      parse("test/", a).isSuccess must beFalse

      val b = long / "test"
      parse("/test/123", b).isSuccess must beFalse
      parse("/123/123", b).isSuccess must beFalse
      parse("123", b).isSuccess must beFalse
      parse("123/test", b) ==== ParseResult(
        success = true, List(LongDef(123), EmptyDef("test"))
      )

      val c = "test" / "abc"
      parse("/test/123", c).isSuccess must beFalse
      parse("/test/abc/123", c).isSuccess must beFalse
      parse("/test/abc", c) ==== ParseResult(
        success = true, List(EmptyDef("test"), EmptyDef("abc"))
      )

      val d = "test" / uuid / "abc" / *
      parse(s"/test/$genUuid", d).isSuccess must beFalse
      parse(s"/foo/$genUuid/abc/123/bar", d).isSuccess must beFalse
      parse(s"/test/$genUuid/abc/123", d) ==== ParseResult(
        success = true, List(EmptyDef("test"), UUIDDef(genUuid),
          EmptyDef("abc"), TailDef(List("123")))
      )
      parse(s"/test/$genUuid/abc/123/bar", d) ==== ParseResult(
        success = true, List(EmptyDef("test"), UUIDDef(genUuid),
          EmptyDef("abc"), TailDef(List("123", "bar")))
      )

      val e = uuid / long
      parse(s"/test/$genUuid", e).isSuccess must beFalse
      parse(s"/$genUuid/asd", e).isSuccess must beFalse
      parse(s"/$genUuid/123/asd", e).isSuccess must beFalse
      parse(s"/$genUuid/123/123", e).isSuccess must beFalse
      parse(s"/$genUuid/123", e) ==== ParseResult(
        success = true, List(UUIDDef(genUuid), LongDef(123))
      )

      val f = uuid / "test"
      parse(s"$genUuid/test?asd=123", f) ==== ParseResult(
        success = true, List(UUIDDef(genUuid), EmptyDef("test"))
      )

      val g = "a" | "b" | "c"
      parse("/test", g).isSuccess must beFalse
      List("a", "b", "c").foreach { x =>
        parse(s"/$x", g) ==== ParseResult(
          success = true, List(AlternativeDef(x))
        )
      }

      val h = string / ("a" | "b" | "c")
      parse("/test", h).isSuccess must beFalse
      List("a", "b", "c").foreach { x =>
        parse(s"test/$x", h) ==== ParseResult(
          success = true, List(StringDef("test"), AlternativeDef(x))
        )
      }

      val i = long / ("a" | "b" | "c") / uuid
      parse("/test", i).isSuccess must beFalse
      parse("/test/a/test", i).isSuccess must beFalse
      parse(s"$genUuid/a/123", i).isSuccess must beFalse
      List("a", "b", "c").foreach { x =>
        parse(s"/123/$x/$genUuid", i) ==== ParseResult(
          success = true, List(LongDef(123), AlternativeDef(x), UUIDDef(genUuid))
        )
      }

      val j = long / ("a" | "b" | "c") / *
      List("a", "b", "c").foreach { x =>
        parse(s"/123/$x/$genUuid/123/abc", j) ==== ParseResult(
          success = true, List(
            LongDef(123),
            AlternativeDef(x),
            TailDef(List(s"$genUuid", "123", "abc")))
        )
      }

      success
    }

    "long" in {
      long.check("123") must beTrue
      long.check("-123") must beTrue
      long.check("123.0") must beFalse
      long.check("asd") must beFalse
      long.check(s"${Long.MaxValue}") must beTrue
    }

    "uuid" in {
      uuid.check(UUID.randomUUID().toString) must beTrue
      uuid.check(UUID.randomUUID().toString + "asd") must beFalse
      uuid.check("asd") must beFalse
    }

    "regex" in {
      val re = regex("gr[ae]y".r)
      re.check("asd") must beFalse
      re.check("grey") must beTrue
      re.check("gray") must beTrue
    }

    "/" in {
      val route = HardCodedParam("/")
      route.check("/asd") must beFalse
      route.check("/") must beTrue
    }

    "alternative" in {
      val xs = List("a", "b", "c")
      val r1 = AlternativeParam(xs)
      xs.foreach { x => r1.check(x) must beTrue }
      r1.check("/test") must beFalse
    }
  }

  private def parse(target: String, pl: PathLikeParam) = {
    ParamsParser.parse(target, pl.rule.currentParams)
  }

  private def parse(target: String, pl: NoPathMoreParam) = {
    ParamsParser.parse(target, pl.rule.currentParams)
  }

}
