package com.github.fntz.omhs

import org.specs2.mutable.Specification

import java.util.UUID

class ParamSpecs extends Specification {

  import ParamDSL._
  import ParamD._

  private val genUuid = UUID.randomUUID()

  "params check" should {
    "#check" in {
      val a = "test" / long / "abc"
      parse("/test/123/abc", a) ==== ParseResult(
        success = true, List(EmptyDef("test"), LongDef(123), EmptyDef("abc"))
      )
      parse("/test/asd/abc", a).isSuccess must beFalse
      parse("test/", a.asInstanceOf[Vector[PathParam]]).isSuccess must beFalse

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
  }

  private def parse(target: String, xs: Vector[Param]) = {
    Param.parse(target, xs.collect { case x: PathParam => x })
  }

}
