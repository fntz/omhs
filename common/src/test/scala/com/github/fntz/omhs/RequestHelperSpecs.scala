package com.github.fntz.omhs

import io.netty.handler.codec.DecoderResult
import io.netty.handler.codec.http.{DefaultFullHttpRequest, HttpMethod, HttpVersion}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class RequestHelperSpecs extends Specification {

  private val uri = "/test"
  private val version = HttpVersion.HTTP_1_1
  private val method = HttpMethod.POST
  private val header = "User-Agent"
  private val hValue = "test-suite"
  private def default = new DefaultFullHttpRequest(
    version,
    method,
    uri
  )

  case class Foo(bar: Int)

  implicit val fooBodyReader = new BodyReader[Foo] {
    override def read(str: String): Foo = {
      Foo(1)
    }
  }

  "run method" should {
    "body" should {
      "do not parse body when not need" in new Test {
        rule.isParseBody must beFalse
        RequestHelper.materialize(request, rule) ==== Right(Nil)
      }
      "parse body with error for some reason" in new Test {
        rule.body[Foo]
        request.setDecoderResult(DecoderResult.failure(new RuntimeException("boom")))

        rule.isParseBody must beTrue
        RequestHelper.materialize(request, rule) ==== Left(BodyIsUnparsable)
      }
      "parse body with success" in new Test {
        rule.body[Foo]
        rule.isParseBody must beTrue

        RequestHelper.materialize(request, rule) ==== Right(List(BodyDef(Foo(1))))
      }
    }
    "header" should {
      "do not return error when headers is empty in Rule" in new Test {
        rule.currentHeaders must be empty

        RequestHelper.materialize(request, rule) ==== Right(Nil)
      }
      "fail when header is missing" in new Test {
        rule.header(header)
        rule.currentHeaders must not be empty

        RequestHelper.materialize(request, rule) ==== Left(HeaderIsMissing(header))
      }
      "parse header with success" in new Test {
        rule.header(header)
        request.headers().add(header, hValue)

        RequestHelper.materialize(request, rule) ==== Right(List(HeaderDef(hValue)))
      }
    }

    "both success" in new Test {
      rule.body[Foo].header(header)
      request.headers().add(header, hValue)

      RequestHelper.materialize(request, rule) ==== Right(List(BodyDef(Foo(1)), HeaderDef(hValue)))
    }
  }

  trait Test extends Scope {
    val request = default
    val rule = new Rule(method).path(uri)
  }

}
