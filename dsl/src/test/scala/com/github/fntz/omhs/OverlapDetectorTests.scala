package com.github.fntz.omhs

import com.github.fntz.omhs.util.OverlapDetector
import io.netty.handler.codec.http.HttpMethod
import munit.FunSuite

class OverlapDetectorTests extends FunSuite {

  import RoutingDSL._
  import OverlapDetector._

  private def r = Rule(HttpMethod.GET)

  test("overlap detection") {
    assert(isOverlapping(
      r.path("a").path(string),
      r.path("a").path("b")
    ))

    assert(!isOverlapping(
      r.path("a").path(string).path("c"),
      r.path("a").path("b")
    ))

    assert(!isOverlapping(
      r.path("a").path("b"),
      r.path("a").path(string).path("c")
    ))

    assert(isOverlapping(
      r.path("a").path("b").path(string),
      r.path("a").path("b").path("c")
    ))

    assert(isOverlapping(
      r.path("a").path(string),
      r.path("a").path(uuid)
    ))

    assert(!isOverlapping(
      r.path("a")
        .path(string)
        .path(long),
      r.path("b")
        .path(string)
        .path(long)
    ))

    assert(isOverlapping(
      r.path("a")
        .path(string)
        .path(long),
      r.path("a")
        .path(string)
        .path(long)
    ))

    assert(!isOverlapping(
      r.path("a")
        .path(string)
        .path(long),
      r.path("a")
        .path(long)
    ))

    assert(!isOverlapping(
      r.path("a")
        .path(string)
        .path(long),
      r.path("a")
        .path(string)
        .path(long)
        .path(uuid)
    ))

    assert(!isOverlapping(
      r.path("a").path("b"),
      r.path("a").path("b").path("c").path(string)
    ))

    // tail
    assert(!isOverlapping(
      r.path("a").path(*),
      r.path("b")
    ))

    assert(isOverlapping(
      r.path("a").path("b"),
      r.path("a").path(*)
    ))

    assert(!isOverlapping(
      r.path("a").path("b"),
      r.path("a").path("b").path(*)
    ))

    assert(!isOverlapping(
      r.path("templates").path(*),
      r.path("js").path(*)
    ))
  }


}
