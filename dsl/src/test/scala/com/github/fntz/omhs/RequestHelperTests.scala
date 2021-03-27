package com.github.fntz.omhs

import com.github.fntz.omhs.internal._
import com.github.fntz.omhs.util.RequestHelper
import com.github.fntz.omhs.util.RequestHelper.E
import io.netty.handler.codec.DecoderResult
import io.netty.handler.codec.http.cookie.{ClientCookieEncoder, DefaultCookie}
import io.netty.handler.codec.http.{DefaultFullHttpRequest, HttpHeaderNames, HttpMethod, HttpVersion}
import munit.FunSuite

class RequestHelperTests extends FunSuite {

  private val uri = "/test"
  private val version = HttpVersion.HTTP_1_1
  private val method = HttpMethod.POST
  private val setup = Setup.default
  case class Search(q: String)
  implicit val searchQueryParser = new QueryReader[Search] {
    override def read(queries: Map[String, Iterable[String]]): Option[Search] = {
      queries.get("q").flatMap(_.headOption).map(Search)
    }
  }
  private val header = HeaderParam("User-Agent", None)
  private val cookie = CookieParam("ABC", None)
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

  private def fetch(request: DefaultFullHttpRequest, rule: Rule): E[ParamDef[_]] = {
    RequestHelper.fetchAdditionalDefs(request, rule, setup)
  }

  def testRule(testName: String)(body: (Rule, DefaultFullHttpRequest) => Unit)(implicit loc: munit.Location): Unit = {
    test(testName) {
      body.apply(Rule(method).path(uri), default)
    }
  }

  testRule("do not parse body when `isNeedToParseBody` is false") { (rule, request) =>
    assert(!rule.isNeedToParseBody)
    assertEquals(fetch(request, rule), Right(Nil))
  }

  testRule("parse body with error") { (rule, request) =>
    rule.body[Foo]
    val ex = new RuntimeException("boom")
    request.setDecoderResult(DecoderResult.failure(ex))

    assert(rule.isNeedToParseBody)
    assertEquals(fetch(request, rule), Left(BodyIsUnparsable(ex)))
  }

  testRule("parse body with success") { (rule, request) =>
    rule.body[Foo]
    assert(rule.isNeedToParseBody)

    assertEquals(fetch(request, rule), Right(List(BodyDef(Foo(1)))))
  }

  testRule("do not return error when headers is empty in Rule") { (rule, request) =>
    assert(rule.currentHeaders.isEmpty)
    assertEquals(fetch(request, rule), Right(Nil))
  }

  testRule("fail when header is missing") { (rule, request) =>
    rule.header(header)
    assert(rule.currentHeaders.nonEmpty)

    assertEquals(fetch(request, rule), Left(HeaderIsMissing(header.headerName)))
  }

  testRule("parse header with success") { (rule, request) =>
    rule.header(header)
    request.headers().add(header.headerName, hValue)

    assertEquals(fetch(request, rule), Right(List(HeaderDef(hValue))))
  }

  testRule("do not return error when cookies is empty in Rule") { (rule, request) =>
    assert(rule.currentCookies.isEmpty)

    assertEquals(fetch(request, rule), Right(Nil))
  }

  testRule("fail when cookie is missing") { (rule, request) =>
    rule.cookie(cookie)
    assert(rule.currentCookies.nonEmpty)

    assertEquals(fetch(request, rule), Left(CookieIsMissing(cookie.cookieName)))
  }

  testRule("parse cookie with success") { (rule, request) =>
    rule.cookie(cookie)
    val c = new DefaultCookie(cookie.cookieName, "foo")
    val add = ClientCookieEncoder.STRICT.encode(c)
    request.headers().add(HttpHeaderNames.COOKIE, add)

    assertEquals(fetch(request, rule), Right(List(CookieDef(c))))
  }

  testRule("fail when query is empty") { (rule, request) =>
    rule.query(searchQueryParser)
    assert(rule.isNeedToDecodeQuery)

    assertEquals(fetch(request, rule), Left(QueryIsUnparsable(Map.empty)))
  }

  testRule("fail when query is unparsable") { (rule, request) =>
    rule.query(searchQueryParser)
    assert(rule.isNeedToDecodeQuery)
    request.setUri("test?q1=foo")

    assertEquals(fetch(request, rule), Left(QueryIsUnparsable(Map("q1" -> List("foo")))))
  }

  testRule("success parse query") { (rule, request) =>
    rule.query(searchQueryParser)
    assert(rule.isNeedToDecodeQuery)
    request.setUri("test?q=foo")

    assertEquals(fetch(request, rule), Right(List(QueryDef(Search("foo")))))
  }

  testRule("success parse") { (rule, request) =>
    rule.body[Foo].header(header)
    request.headers().add(header.headerName, hValue)

    assertEquals(fetch(request, rule), Right(List(BodyDef(Foo(1)), HeaderDef(hValue))))
  }

}
