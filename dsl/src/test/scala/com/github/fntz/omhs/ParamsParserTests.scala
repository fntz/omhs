package com.github.fntz.omhs

import com.github.fntz.omhs.internal._
import munit.FunSuite

import java.util.UUID

class ParamsParserTests extends FunSuite {

  import RoutingDSL._

  private val genUuid = UUID.randomUUID()

  test("params check") {
    val a = "test" / long / "abc"
    assertEquals(parse("/test/123/abc", a), ParseResult(
      success = true, List(EmptyDef("test"), LongDef(123), EmptyDef("abc"))
    ))
    assert(!parse("/test/asd/abc", a).isSuccess)
    assert(!parse("test/", a).isSuccess)

    val b = long / "test"
    assert(!parse("/test/123", b).isSuccess)
    assert(!parse("/123/123", b).isSuccess)
    assert(!parse("123", b).isSuccess)
    assertEquals(parse("123/test", b), ParseResult(
      success = true, List(LongDef(123), EmptyDef("test"))
    ))

    val c = "test" / "abc"
    assert(!parse("/test/123", c).isSuccess)
    assert(!parse("/test/abc/123", c).isSuccess)
    assertEquals(parse("/test/abc", c), ParseResult(
      success = true, List(EmptyDef("test"), EmptyDef("abc"))
    ))

    val d = "test" / uuid / "abc" / *
    assert(!parse(s"/test/$genUuid", d).isSuccess)
    assert(!parse(s"/test/$genUuid/abc", d).isSuccess)
    assert(!parse(s"/foo/$genUuid/abc/123/bar", d).isSuccess)
    assertEquals(parse(s"/test/$genUuid/abc/123", d), ParseResult(
      success = true, List(EmptyDef("test"), UUIDDef(genUuid),
        EmptyDef("abc"), TailDef(List("123")))
    ))
    assertEquals(parse(s"/test/$genUuid/abc/123/bar", d), ParseResult(
      success = true, List(EmptyDef("test"), UUIDDef(genUuid),
        EmptyDef("abc"), TailDef(List("123", "bar")))
    ))

    val e = uuid / long
    assert(!parse(s"/test/$genUuid", e).isSuccess)
    assert(!parse(s"/$genUuid/asd", e).isSuccess)
    assert(!parse(s"/$genUuid/123/asd", e).isSuccess)
    assert(!parse(s"/$genUuid/123/123", e).isSuccess)
    assertEquals(parse(s"/$genUuid/123", e), ParseResult(
      success = true, List(UUIDDef(genUuid), LongDef(123))
    ))

    val f = uuid / "test"
    assertEquals(parse(s"$genUuid/test?asd=123", f), ParseResult(
      success = true, List(UUIDDef(genUuid), EmptyDef("test"))
    ))

    val g = "a" | "b" | "c"
    assert(!parse("/test", g).isSuccess)
    List("a", "b", "c").foreach { x =>
      assertEquals(parse(s"/$x", g), ParseResult(
        success = true, List(AlternativeDef(x))
      ))
    }

    val h = string / ("a" | "b" | "c")
    assert(!parse("/test", h).isSuccess)
    List("a", "b", "c").foreach { x =>
      assertEquals(parse(s"test/$x", h), ParseResult(
        success = true, List(StringDef("test"), AlternativeDef(x))
      ))
    }

    val i = long / ("a" | "b" | "c") / uuid
    assert(!parse("/test", i).isSuccess)
    assert(!parse("/test/a/test", i).isSuccess)
    assert(!parse(s"$genUuid/a/123", i).isSuccess)
    List("a", "b", "c").foreach { x =>
      assertEquals(parse(s"/123/$x/$genUuid", i), ParseResult(
        success = true, List(LongDef(123), AlternativeDef(x), UUIDDef(genUuid))
      ))
    }

    val j = long / ("a" | "b" | "c") / *
    List("a", "b", "c").foreach { x =>
      assertEquals(parse(s"/123/$x/$genUuid/123/abc", j), ParseResult(
        success = true, List(
          LongDef(123),
          AlternativeDef(x),
          TailDef(List(s"$genUuid", "123", "abc")))
      ))
    }
  }

  test("`long`") {
    assert(long.check("123"))
    assert(long.check("-123"))
    assert(!long.check("123.0"))
    assert(!long.check("asd"))
    assert(long.check(s"${Long.MaxValue}"))
  }

  test("`uuid`") {
    assert(uuid.check(UUID.randomUUID().toString))
    assert(!uuid.check(UUID.randomUUID().toString + "asd"))
    assert(!uuid.check("asd"))
  }

  test("`regex`") {
    val re = regex("gr[ae]y".r)
    assert(!re.check("asd"))
    assert(re.check("grey"))
    assert(re.check("gray"))
  }

  test("`/`") {
    val route = HardCodedParam("/")
    assert(!route.check("/asd"))
    assert(route.check("/"))
  }

  test("alternative") {
    val xs = List("a", "b", "c")
    val r1 = AlternativeParam(xs)
    xs.foreach { x => assert(r1.check(x)) }
    assert(!r1.check("/test"))
  }

  private def parse(target: String, pl: PathLikeParam) = {
    ParamsParser.parse(target, pl.rule.currentParams)
  }

  private def parse(target: String, pl: NoPathMoreParam) = {
    ParamsParser.parse(target, pl.rule.currentParams)
  }

}
