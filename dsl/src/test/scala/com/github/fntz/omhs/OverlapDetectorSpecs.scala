package com.github.fntz.omhs

import org.specs2.mutable.Specification
import com.github.fntz.omhs.util.OverlapDetector
import io.netty.handler.codec.http.HttpMethod

class OverlapDetectorSpecs extends Specification {

  import RoutingDSL._
  import OverlapDetector._

  private def r = Rule(HttpMethod.GET)

  "overlap detector" should {
    "isOverlapping" in {
      isOverlapping(
        r.path("a")
         .path(string)
         .path(long),
        r.path("b")
          .path(string)
          .path(long)
      ) must beFalse

      isOverlapping(
        r.path("a")
          .path(string)
          .path(long),
        r.path("a")
          .path(string)
          .path(long)
      ) must beTrue

      isOverlapping(
        r.path("a")
          .path(string)
          .path(long),
        r.path("a")
          .path(long)
      ) must beFalse

      isOverlapping(
        r.path("a")
          .path(string)
          .path(long),
        r.path("a")
          .path(string)
          .path(long)
          .path(uuid)
      ) must beFalse

      isOverlapping(
        r.path("a").path("b"),
        r.path("a").path("b").path("c").path(string)
      ) must beFalse

      // tail
      isOverlapping(
        r.path("a").path(*),
        r.path("b")
      ) must beFalse

      isOverlapping(
        r.path("a").path("b"),
        r.path("a").path(*)
      ) must beTrue

      isOverlapping(
        r.path("a").path("b"),
        r.path("a").path("b").path(*)
      ) must beFalse
    }
  }

}
