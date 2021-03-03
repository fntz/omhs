package com.github.fntz.omhs

import org.specs2.mutable.Specification

import java.util.UUID

class ParamSpecs extends Specification {

  import ParamDSL._

  private val uuid = UUID.randomUUID()

  private def parse(target: String, xs: Vector[Param]) = {
    Param.parse(target, xs.collect { case x: PathParam => x })
  }

  "params check" should {
    "#check" in {
      val a = "test" / LongParam / "abc"
      parse("/test/123/abc", a) ==== ParseResult(
        success = true, List(EmptyDef("test"), LongDef(123), EmptyDef("abc"))
      )
      parse("/test/asd/abc", a).isSuccess must beFalse
      parse("test/", a.asInstanceOf[Vector[PathParam]]).isSuccess must beFalse

      val b = LongParam / "test"
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

      val d = "test" / UUIDParam / "abc" / *
      parse(s"/test/$uuid", d).isSuccess must beFalse
      parse(s"/foo/$uuid/abc/123/bar", d).isSuccess must beFalse
      parse(s"/test/$uuid/abc/123", d) ==== ParseResult(
        success = true, List(EmptyDef("test"), UUIDDef(uuid),
          EmptyDef("abc"), TailDef(List("123")))
      )
      parse(s"/test/$uuid/abc/123/bar", d) ==== ParseResult(
        success = true, List(EmptyDef("test"), UUIDDef(uuid),
          EmptyDef("abc"), TailDef(List("123", "bar")))
      )

      val e = UUIDParam / LongParam
      parse(s"/test/$uuid", e).isSuccess must beFalse
      parse(s"/$uuid/asd", e).isSuccess must beFalse
      parse(s"/$uuid/123/asd", e).isSuccess must beFalse
      parse(s"/$uuid/123/123", e).isSuccess must beFalse
      parse(s"/$uuid/123", e) ==== ParseResult(
        success = true, List(UUIDDef(uuid), LongDef(123))
      )

      val f = UUIDParam / "test"
      parse(s"$uuid/test?asd=123", f) ==== ParseResult(
        success = true, List(UUIDDef(uuid), EmptyDef("test"))
      )
    }

    "long" in {
      LongParam.check("123") must beTrue
      LongParam.check("-123") must beTrue
      LongParam.check("123.0") must beFalse
      LongParam.check("asd") must beFalse
      LongParam.check(s"${Long.MaxValue}") must beTrue
    }

    "uuid" in {
      UUIDParam.check(UUID.randomUUID().toString) must beTrue
      UUIDParam.check(UUID.randomUUID().toString + "asd") must beFalse
      UUIDParam.check("asd") must beFalse
    }

    "regex" in {
      val re = RegexParam("gr[ae]y".r)
      re.check("asd") must beFalse
      re.check("grey") must beTrue
      re.check("gray") must beTrue
    }
  }

}
