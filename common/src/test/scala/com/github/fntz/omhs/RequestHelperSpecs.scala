package com.github.fntz.omhs

import io.netty.handler.codec.DecoderResult
import io.netty.handler.codec.http.cookie.{ClientCookieEncoder, DefaultCookie}
import io.netty.handler.codec.http.{DefaultFullHttpRequest, HttpHeaderNames, HttpMethod, HttpVersion}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class RequestHelperSpecs extends Specification {

  private val uri = "/test"
  private val version = HttpVersion.HTTP_1_1
  private val method = HttpMethod.POST
  case class Search(q: String)
  implicit val searchQueryParser = new QueryReader[Search] {
    override def read(queries: Map[String, List[String]]): Option[Search] = {
      queries.get("q").flatMap(_.headOption).map(Search)
    }
  }
  private val header = HeaderParam("User-Agent", None)
  private val cookie = CookieParam("ABC", None)
  private val query = QueryParam[Search]
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
        RequestHelper.fetchAdditionalDefs(request, rule) ==== Right(Nil)
      }
      "parse body with error for some reason" in new Test {
        rule.body[Foo]
        request.setDecoderResult(DecoderResult.failure(new RuntimeException("boom")))

        rule.isParseBody must beTrue
        RequestHelper.fetchAdditionalDefs(request, rule) ==== Left(BodyIsUnparsable)
      }
      "parse body with success" in new Test {
        rule.body[Foo]
        rule.isParseBody must beTrue

        RequestHelper.fetchAdditionalDefs(request, rule) ==== Right(List(BodyDef(Foo(1))))
      }
    }
    "header" should {
      "do not return error when headers is empty in Rule" in new Test {
        rule.currentHeaders must be empty

        RequestHelper.fetchAdditionalDefs(request, rule) ==== Right(Nil)
      }
      "fail when header is missing" in new Test {
        rule.header(header)
        rule.currentHeaders must not be empty

        RequestHelper.fetchAdditionalDefs(request, rule) ==== Left(HeaderIsMissing(header.headerName))
      }
      "parse header with success" in new Test {
        rule.header(header)
        request.headers().add(header.headerName, hValue)

        RequestHelper.fetchAdditionalDefs(request, rule) ==== Right(List(HeaderDef(hValue)))
      }
    }

    "cookie" should {
      "do not return error when cookies is empty in Rule" in new Test {
        rule.currentCookies must be empty

        RequestHelper.fetchAdditionalDefs(request, rule) ==== Right(Nil)
      }
      "fail when cookie is missing" in new Test {
        rule.cookie(cookie)
        rule.currentCookies must not be empty

        RequestHelper.fetchAdditionalDefs(request, rule) ==== Left(CookieIsMissing(cookie.cookieName))
      }
      "parse cookie with success" in new Test {
        rule.cookie(cookie)
        val c = new DefaultCookie(cookie.cookieName, "foo")
        val add = ClientCookieEncoder.STRICT.encode(c)
        request.headers().add(HttpHeaderNames.COOKIE, add)

        RequestHelper.fetchAdditionalDefs(request, rule) ==== Right(List(CookieDef(c)))
      }
    }

    "query" should {
      "fail when query is empty" in new Test {
        rule.query()(searchQueryParser)
        rule.isFetchQuery must beTrue

        RequestHelper.fetchAdditionalDefs(request, rule) ==== Left(QueryIsUnparsable(Map.empty))
      }
      "fail when query is unparsable" in new Test {
        rule.query()(searchQueryParser)
        rule.isFetchQuery must beTrue
        request.setUri("test?q1=foo")

        RequestHelper.fetchAdditionalDefs(request, rule) ==== Left(QueryIsUnparsable(Map("q1" -> List("foo"))))
      }

      "success parse query" in new Test {
        rule.query()(searchQueryParser)
        rule.isFetchQuery must beTrue
        request.setUri("test?q=foo")

        RequestHelper.fetchAdditionalDefs(request, rule) ==== Right(List(QueryDef(Search("foo"))))
      }
    }

    "both success" in new Test {
      rule.body[Foo].header(header)
      request.headers().add(header.headerName, hValue)

      RequestHelper.fetchAdditionalDefs(request, rule) ==== Right(List(BodyDef(Foo(1)), HeaderDef(hValue)))
    }
  }

  trait Test extends Scope {
    val request = default
    val rule = new Rule(method).path(uri)
  }

}
