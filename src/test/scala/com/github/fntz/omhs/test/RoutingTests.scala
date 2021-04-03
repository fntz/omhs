package com.github.fntz.omhs.test

import com.github.fntz.omhs._
import com.github.fntz.omhs.internal.ExecutableRule
import com.github.fntz.omhs.streams.ChunkedOutputStream
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.cookie.{ClientCookieEncoder, Cookie, DefaultCookie, ServerCookieDecoder, ServerCookieEncoder}
import io.netty.handler.codec.http.multipart.FileUpload
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.util.{AsciiString, CharsetUtil}
import munit.FunSuite
import play.api.libs.json.Json

import java.nio.file.Files
import java.util.UUID

class RoutingTests extends FunSuite {

  import RoutingDSL._
  import AsyncResult.Implicits._
  import AsyncResult.Streaming._

  case class Foo(id: Int)
  implicit val fooReader: BodyReader[Foo] = (str: String) => {
    Json.parse(str).as[Foo](Json.reads[Foo])
  }

  private val channels = scala.collection.mutable.ArrayBuffer[EmbeddedChannel]()
  private val headerName = "X-foo"
  private val headerValue = "bar"

  private val file1 = java.io.File.createTempFile("tests", ".txt")
  file1.deleteOnExit()
  Files.write(file1.toPath, "test".getBytes(CharsetUtil.UTF_8))

  case class Search(query: String)
  implicit val queryReader = new QueryReader[Search] {
    override def read(queries: Map[String, Iterable[String]]): Option[Search] = {
      queries.get("query").flatMap(_.headOption).map(Search)
    }
  }


  val stringRoute = get("test" / string) ~> { (x: String) => x }
  routeTest(
    "check `string` in a route",
    stringRoute,
    "/test/foo"
  ) { result =>
    result.isOk
    result.contains("foo")
  }

  val longRoute = get("test" / long) ~> { (x: Long) => s"$x" }
  routeTest(
    "check `long` in a route",
    longRoute,
    path = "/test/123"
  ) { result =>
    result.isOk
    result.contains("123")
  }

  val genUuid = UUID.randomUUID()
  val uuidRoute = get("test" / uuid) ~> {(x: UUID) => s"$x"}
  routeTest(
    "check `uuid` in a route",
    uuidRoute,
    path = s"/test/$genUuid"
  ) { result =>
    result.isOk
    result.contains(s"$genUuid")
  }

  val regexRoute = get("test" / regex("^foo".r)) ~> { (x: String) => s"$x" }
  routeTest(
    "check `regex` in a route",
    regexRoute,
    "/test/foobar"
  ) { result =>
    result.isOk
    result.contains("foo")
  }

  routeTest(
    "check `regex`#404",
    regexRoute,
    path = "/test/asd"
  ) { result =>
    result.hasStatus(HttpResponseStatus.NOT_FOUND)
  }

  val tailRoute = get("test" / *) ~> { (xs: List[String]) => xs.mkString(".") }
  routeTest(
    "match tail of the path",
    tailRoute,
    path = s"/test/123/foo/bar/$uuid"
  ) { result =>
    result.isOk
    result.contains(s"123.foo.bar.$uuid")
  }


  val routeWithHeader = get("test" << header(headerName)) ~> { (x: String) => x }
  routeTest(
    "fetch header from request",
    routeWithHeader,
    request = Some({
      val r =  req("/test")
      r.headers().set(headerName, headerValue)
      r
    })
  ) { result =>
    result.isOk
    result.contains(headerValue)
  }

  def write(x: Foo): String = Json.toJson(x)(Json.writes[Foo]).toString
  val routeWithBody = post("test" <<< body[Foo]) ~> { (x: Foo) =>
    write(x)
  }
  val foo = Foo(1000)
  routeTest(
    "fetch body from request",
    routeWithBody,
    request = Some({
      val r = req("/test")
      r.setMethod(HttpMethod.POST)
      val z = r.replace(Unpooled.copiedBuffer(write(foo).getBytes(CharsetUtil.UTF_8)))
      z
    })
  ) { result =>
    result.isOk
    result.contains(write(foo))
  }

  val routeWithFile = post("test" <<< file) ~> { (xs: List[FileUpload]) =>
    s"${xs.map(_.getName).mkString(", ")}"
  }
  test("fetch files".ignore) {

  }

  routeTest(
    "handle when body is incorrect",
    routeWithBody,
    request = Some({
      val r = req("/test")
      r.setMethod(HttpMethod.POST)
      r
    })
  ) { result =>
    result.hasStatus(HttpResponseStatus.BAD_REQUEST)
    result.contains("body is incorrect")
  }

  test("unparsed files".ignore) {

  }

  routeTest(
    "handle when header is missing",
    routeWithHeader
  ) { result =>
    result.hasStatus(HttpResponseStatus.BAD_REQUEST)
    result.contains(s"header: $headerName is missing")
  }

  val commonRoute = get("test" / long) ~> {() => "ok" }

  routeTest(
    "handle when path is not matched",
    commonRoute,
    path = "/test/foo"
  ) { result =>
    result.hasStatus(HttpResponseStatus.NOT_FOUND)
    result.contains("/test/foo")
  }

  val routeWithException = get("test" / long) ~> {() =>
    throw new RuntimeException("boom")
    "ok"
  }
  routeTest(
    "failed, when a function throw some exception: UnhandledException",
    routeWithException,
    path = "test/123"
  ) { result =>
    result.hasStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR)
  }

  val cookieName = "cookie-foo"
  val cookieValue = "cookie-bar"
  val cookieRouting = get("test" << cookie(cookieName)) ~> { (c: Cookie) =>
    c.value()
  }

  routeTest(
    "cookie routing",
    cookieRouting,
    request = Some({
      val r = req("test")
      val c = new DefaultCookie(cookieName, cookieValue)
      val add = ClientCookieEncoder.STRICT.encode(c)
      r.headers().add(HttpHeaderNames.COOKIE, add)
      r
    })
  ) { result =>
    result.isOk
    assertEquals(result.content, cookieValue)
  }

  routeTest(
    "failed when a cookie is missing in the headers",
    cookieRouting
  ) { result =>
    result.hasStatus(HttpResponseStatus.BAD_REQUEST)
    result.contains(s"cookie: $cookieName is missing")
  }

  val queryRoute = get("test" :? query[Search]) ~> { (s: Search) => s.query }
  routeTest(
    "query route",
    queryRoute,
    path = "test?query=123"
  ) { result =>
    result.isOk
    result.contains("123")
  }

  val alternativeRoute = get("test" / ("a" | "b")) ~> { (s: String) => s }

  routeTest(
    "use alternative params#1",
    alternativeRoute,
    path = "test/a"
  ) { result =>
    result.isOk
    result.contains("a")
  }

  routeTest(
    "use alternative params#2",
    alternativeRoute,
    path = "test/b"
  ) { result =>
    result.isOk
    result.contains("b")
  }

  routeTest(
    "use alternative params, when the alternative is not found",
    alternativeRoute,
    path = "test/c"
  ) { result =>
    result.hasStatus(HttpResponseStatus.NOT_FOUND)
  }

  routeTest(
    "fail when query is not parsable",
    queryRoute
  ) { result =>
    result.hasStatus(HttpResponseStatus.BAD_REQUEST)
    result.contains("query is unparsable")
  }

  val streamRoute = get("test") ~> { (stream: ChunkedOutputStream) =>
    stream.write("123".getBytes)
    stream << "456"
    stream << "789"
  }
  routeTest(
    "chunked stream support",
    streamRoute,
    isStream = true
  ) { result =>
    result.isOk
    assertEquals(result.messagesSize, 4) // 3 chunks + first empty response
    result.hasContentType(HttpHeaderValues.APPLICATION_OCTET_STREAM.toString)
    result.contains("123456789")
  }

  routeTest(
    "head response should be empty",
    head("/test") ~> {() => "abc"},
    request = Some({
      val r = req("/test")
      r.setMethod(HttpMethod.HEAD)
      r
    })
  ) { result =>
    result.isOk
    result.isEmpty
  }

  import moar._
  val r18 = get("test" / string) ~> route { (x: String) =>
    if (x == "foo") {
      implicit val enc = ServerCookieEncoder.STRICT
      val d = new DefaultCookie("test", "bar")
      d.setDomain("example.com")
      setCookie(d)
      setCookie("foo", "bar")
      status(200)
      setHeader("x-test-header-1", "header-value-1")
      setHeader("x-test-header-2", "header-value-2")
      contentType("application/javascript")
      "ok"
    } else {
      status(404)
      contentType("x-type/none")
      setHeader("x-header-foo", "bar")
      "not_found"
    }
  }

  routeTest(
    "moar-syntax: all parameters should sat correctly",
    r18,
    "/test/foo"
  ) { result =>
    result.isOk
    result.hasContentType("application/javascript")
    val cs = result.header(HttpHeaderNames.SET_COOKIE)
    val cookies = ServerCookieDecoder.STRICT.decode(cs)
    assertEquals(cookies.size, 2)
    assertEquals(result.header("x-test-header-1"), "header-value-1")
    assertEquals(result.header("x-test-header-2"), "header-value-2")
    assert(Option(result.header("x-header-foo")).isEmpty)
    result.contains("ok")
  }

  routeTest(
    "moar-syntax: all parameters should sat correctly",
    r18,
    path = "/test/foo1"
  ) { result =>
    result.hasStatus(HttpResponseStatus.NOT_FOUND)
    assertEquals(result.header("x-header-foo"), "bar")
    assert(Option(result.header("x-test-header-1")).isEmpty)
    assert(Option(result.header("x-test-header-2")).isEmpty)
    result.hasContentType("x-type/none")
    result.contains("not_found")
  }

  routeTest(
    "setup: server header should be added into response",
    get("test") ~> {() => "done"}
  ) { result =>
    assert(result.header(HttpHeaderNames.SERVER).contains("netty"))
  }

  routeTest(
    "setup: server header should not be added when setup is disabled",
    get("test") ~> {() => "done"},
    setup = Setup.default.withSendServerHeader(false)
  ) { result =>
    assert(Option(result.header(HttpHeaderNames.SERVER)).isEmpty)
  }


  routeTest(
    "rewriter: add necessary headers into response",
    get("test") ~> {() => "done"},
    rewriter = (x: HttpResponse) => {
      x.headers().set("foo", "bar")
      x
    }
  ) { result =>
    result.isOk
    assertEquals(result.header("foo"), "bar")
  }

  routeTest(
    "detect ajax request#1",
    get("test") ~> {(c: CurrentHttpRequest) => s"${c.isXHR}"}
  ) { result =>
    result.isOk
    result.contains("false")
  }

  routeTest(
    "detect ajax request#2",
    get("test") ~> {(c: CurrentHttpRequest) => s"${c.isXHR}"},
    request = Some({
      val r = req("test")
      r.headers().add(HttpHeaderNames.X_REQUESTED_WITH, "xmlhttprequest")
      r
    })
  ) { result =>
    result.isOk
    result.contains("true")
  }

  routeTest(
    "detect ajax request#3",
    get("test") ~> {(c: CurrentHttpRequest) => s"${c.isXHR}"},
    request = Some({
      val r = req("test")
      r.headers().add(HttpHeaderNames.X_REQUESTED_WITH, "foo")
      r
    })
  ) { result =>
    result.isOk
    result.contains("false")
  }


  private def req(path: String) = {
    new DefaultFullHttpRequest(
      HttpVersion.HTTP_1_1, HttpMethod.GET, path
    )
  }



  def routeTest(
                 testName: String,
                 rule: ExecutableRule,
                 path: String = "/test",
                 isStream: Boolean = false,
                 setup: Setup = Setup.default,
                 request: Option[FullHttpRequest] = None,
                 rewriter: HttpResponse => HttpResponse = identity
               )(body: TestResult => Unit)(implicit loc: munit.Location): Unit = {
    test(testName) {
      val tmpRequest = request
      new RouteTest(rule, path, isStream, setup, rewriter){
        val testResult = TestResult(
          status = response.status(),
          contentType = contentType,
          messagesSize = messagesSize,
          response = response,
          content = content
        )
        body.apply(testResult)

        override def makeRequest(path: String): FullHttpRequest = {
          tmpRequest match {
            case Some(r) => r
            case None =>
              super.makeRequest(path)
          }
        }
      }
    }
  }

  private case class TestResult(
                               status: HttpResponseStatus,
                               contentType: String,
                               content: String,
                               messagesSize: Int,
                               response: DefaultHttpResponse
                             ) {
    def header(name: String): String = {
      response.headers().get(name)
    }

    def header(name: AsciiString): String = {
      response.headers().get(name)
    }

    def isOk(implicit loc: munit.Location): Unit = {
      assertEquals(status, HttpResponseStatus.OK)
    }

    def hasStatus(status: HttpResponseStatus)(implicit loc: munit.Location): Unit = {
      assertEquals(status, status)
    }

    def hasContentType(expectedContentType: String)(implicit loc: munit.Location): Unit = {
      assertEquals(contentType, expectedContentType)
    }

    def contains(expectedContentPart: String)(implicit loc: munit.Location): Unit = {
      assert(content.contains(expectedContentPart))
    }

    def isEmpty(implicit loc: munit.Location): Unit = {
      assert(content.isEmpty)
    }
  }

  private class RouteTest(rule: ExecutableRule, path: String,
                          isStream: Boolean = false,
                          setup: Setup = Setup.default,
                          f: HttpResponse => HttpResponse = identity
                         ) {
    def makeRequest(path: String): FullHttpRequest = req(path)
    val ro = (new Route).addRule(rule).onEveryHttpResponse(f).toHandler(setup)
    val channel = new EmbeddedChannel(new LoggingHandler(LogLevel.DEBUG))
    channel.pipeline()
      .addFirst(new LoggingHandler(LogLevel.DEBUG))
      .addLast("codec", new HttpRequestDecoder())
      .addLast("aggregator", new HttpObjectAggregator(setup.maxContentLength))
      .addLast(ro)
    channels += channel
    val request = makeRequest(path)
    channel.writeInbound(request)
    var status = HttpResponseStatus.NOT_IMPLEMENTED
    var contentType = "test/test"
    var content = ""
    val messagesSize = channel.outboundMessages().size
    var response: DefaultHttpResponse = null
    if (isStream) {
      import scala.collection.JavaConverters._
      channel.outboundMessages().asScala.collect {
        case httpContent: DefaultHttpContent =>
          content = content + httpContent.content().toString(CharsetUtil.UTF_8)
        case resp: DefaultHttpResponse =>
          status = resp.status()
          contentType = resp.headers().get(HttpHeaderNames.CONTENT_TYPE)
          response = resp
      }
    } else {
      response = channel.readOutbound()
      status = response.status()
      contentType = response.headers().get(HttpHeaderNames.CONTENT_TYPE)
      content = response.asInstanceOf[DefaultFullHttpResponse].content().toString(CharsetUtil.UTF_8)
    }
  }

  override def afterAll(): Unit = {
    channels.foreach { ch =>
      ch.close().sync().addListener(ChannelFutureListener.CLOSE)
    }
  }
}
