package com.github.fntz.omhs.test

import com.github.fntz.omhs._
import com.github.fntz.omhs.internal.ExecutableRule
import com.github.fntz.omhs.streams.ChunkedOutputStream
import com.github.fntz.omhs.util.AdditionalHeadersConstants
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.cookie.{ClientCookieEncoder, Cookie, DefaultCookie, ServerCookieDecoder, ServerCookieEncoder}
import io.netty.handler.codec.http.multipart.FileUpload
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.util.CharsetUtil
import org.specs2.mutable.Specification
import org.specs2.specification.{AfterAll, Scope}
import play.api.libs.json.Json

import java.nio.file.Files
import java.util.UUID

class RoutingSpecs extends Specification with AfterAll {

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

  "routing" in {
    val r1 = get("test" / string) ~> { (x: String) => x }

    "match string" in new RouteTest(r1, "/test/foo") {
      status ==== HttpResponseStatus.OK
      content ==== "foo"
    }

    val r2 = get("test" / long) ~> { (x: Long) => s"$x" }
    "match long" in new RouteTest(r2, "/test/123") {
      status ==== HttpResponseStatus.OK
      content ==== "123"
    }

    val genUuid = UUID.randomUUID()
    val r3 = get("test" / uuid) ~> {(x: UUID) => s"$x"}
    "match uuid"  in new RouteTest(r3, s"/test/$genUuid") {
      status ==== HttpResponseStatus.OK
      content ==== s"$genUuid"
    }

    val r4 = get("test" / regex("^foo".r)) ~> { (x: String) => s"$x" }
    "match regex/200" in new RouteTest(r4, "/test/foobar") {
      status ==== HttpResponseStatus.OK
      content ==== "foo"
    }

    "match regex/404" in new RouteTest(r4, "/test/asd") {
      status ==== HttpResponseStatus.NOT_FOUND
    }

    val r5 = get("test" / *) ~> { (xs: List[String]) => xs.mkString(".") }
    "match tail" in new RouteTest(r5, s"/test/123/foo/bar/$uuid") {
      status ==== HttpResponseStatus.OK
      content ==== s"123.foo.bar.$uuid"
    }

    val r6 = get("test" << header(headerName)) ~> { (x: String) => x }
    "match header" in new RouteTest(r6, "/test") {
      override def makeRequest(path: String): FullHttpRequest = {
        val r =  req(path)
        r.headers().set(headerName, headerValue)
        r
      }
      status ==== HttpResponseStatus.OK
      content ==== headerValue
    }

    def write(x: Foo): String = Json.toJson(x)(Json.writes[Foo]).toString
    val r7 = post("test" <<< body[Foo]) ~> { (x: Foo) =>
      write(x)
    }
    val foo = Foo(1000)
    "fetch body" in new RouteTest(r7, "/test") {
      override def makeRequest(path: String): FullHttpRequest = {
        val r = req(path)
        r.setMethod(HttpMethod.POST)
        val z = r.replace(Unpooled.copiedBuffer(write(foo).getBytes(CharsetUtil.UTF_8)))
        z
      }
      status ==== HttpResponseStatus.OK
      content ==== write(foo)
    }

    val r8 = post("test" <<< file) ~> { (xs: List[FileUpload]) =>
      s"${xs.map(_.getName).mkString(", ")}"
    }
    "fetch files" in {
      pending
    }

    val r9 = put("test" <<< body[Foo]) ~> {() => ""}
    "unparsed body" in new RouteTest(r9, "/test") {
      override def makeRequest(path: String): DefaultFullHttpRequest = {
        val r = req(path)
        r.setMethod(HttpMethod.PUT)
        r
      }
      status ==== HttpResponseStatus.BAD_REQUEST
      content must contain("body is incorrect")
    }

    "unparsed files" in {
      pending
    }

    val r11 = get("test" << header(headerValue)) ~> {() => "ok"}
    "header is missing" in new RouteTest(r11, "/test") {
      status ==== HttpResponseStatus.BAD_REQUEST
      content must contain("")
    }

    val r12 = get("test" / long) ~> {() => "ok" }
    "path is not matched" in new RouteTest(r12, "/test/foo") {
      status ==== HttpResponseStatus.NOT_FOUND
      content must contain("/test/foo")
    }

    val r13 = get("test" / long) ~> {() =>
      throw new RuntimeException("boom")
      "ok"
    }
    "function throw exception: UnhandledException" in new RouteTest(r13, "test/123") {
      status ==== HttpResponseStatus.INTERNAL_SERVER_ERROR
      content must contain("boom")
    }

    val cookieName = "foo"
    val r14 = get("test" << cookie(cookieName)) ~> { (c: Cookie) =>
      c.value()
    }
    "pass cookie" in new RouteTest(r14, "test") {
      def v = "bar"
      override def makeRequest(path: String): FullHttpRequest = {
        val r = req("test")
        val c = new DefaultCookie(cookieName, v)
        val add = ClientCookieEncoder.STRICT.encode(c)
        r.headers().add(HttpHeaderNames.COOKIE, add)
        r
      }
      status ==== HttpResponseStatus.OK
      content ==== v
    }

    "cookie is missing" in new RouteTest(r14, "test") {
      status ==== HttpResponseStatus.BAD_REQUEST
      content must contain(s"cookie: foo is missing")
    }

    case class Search(query: String)
    implicit val queryReader = new QueryReader[Search] {
      override def read(queries: Map[String, Iterable[String]]): Option[Search] = {
        queries.get("query").flatMap(_.headOption).map(Search)
      }
    }
    val r15 = get("test" :? query[Search]) ~> { (s: Search) => s.query }
    "pass query" in new RouteTest(r15, "test?query=123") {
      status ==== HttpResponseStatus.OK
      content ==== "123"
    }

    val r16 = get("test" / ("a" | "b")) ~> { (s: String) => s }
    "use alternative params" in new RouteTest(r16, "test/a") {
      status ==== HttpResponseStatus.OK
      content ==== "a"
    }

    val r17 = get("test" / ("a" | "b")) ~> { (s: String) => s }
    "use alternative params" in new RouteTest(r16, "test/c") {
      status ==== HttpResponseStatus.NOT_FOUND
    }

    "fail when query is unparsable" in new RouteTest(r15, "test") {
      status ==== HttpResponseStatus.BAD_REQUEST
      content must contain("query is unparsable")
    }

    val r18 = get("test") ~> { (stream: ChunkedOutputStream) =>
      stream.write("123".getBytes())
      stream << "456".getBytes()
      stream << "789".getBytes()
    }
    "chunked stream support" in new RouteTest(r18, "/test", isStream = true) {
      messagesSize = 3 // 3 writes
      contentType ==== HttpHeaderValues.APPLICATION_OCTET_STREAM.toString
      status ==== HttpResponseStatus.OK
      content ==== "123456789"
    }
  }

  "moar/syntax" should {
    import moar._
    val r18 = get("test" / string) ~> route { (x: String) =>
      if (x == "foo") {
        implicit val enc = ServerCookieEncoder.STRICT
        val d = new DefaultCookie("test", "bar")
        d.setDomain("example.com")
        setCookie(d)
        setCookie("a", "b")
        status(200)
        setHeader("x-test", "foo")
        setHeader("x-ooo", "qwe")
        contentType("application/javascript")
        "ok"
      } else {
        status(404)
        contentType("x-type/none")
        setHeader("foo", "bar")
        "not_found"
      }
    }
    "run with route#1" in new RouteTest(r18, "/test/foo") {
      status ==== HttpResponseStatus.OK
      contentType ==== "application/javascript"
      val cs = response.headers().get(HttpHeaderNames.SET_COOKIE)
      val cookies = ServerCookieDecoder.STRICT.decode(cs)
      cookies.size ==== 2
      response.headers().get("x-test") ==== "foo"
      response.headers().get("x-ooo") ==== "qwe"
      Option(response.headers().get("foo")) must beNone
      content ==== "ok"
    }

    "run with route#2" in new RouteTest(r18, "/test/foo1") {
      status ==== HttpResponseStatus.NOT_FOUND
      response.headers().get("foo") ==== "bar"
      Option(response.headers().get("x-test")) must beNone
      Option(response.headers().get("x-ooo")) must beNone
      contentType ==== "x-type/none"
      content ==== "not_found"
    }
  }

  "setup" should {
    import AdditionalHeadersConstants._
    val r = get("test") ~> {() => "done"}

    "server header" should {
      "enable" in new RouteTest(r, "/test") {
        response.headers().get(HttpHeaderNames.SERVER) must contain("netty")
      }

      "disable" in new RouteTest(r, "/test", isStream = false,
        Setup.default.withSendServerHeader(false)
      ) {
        Option(response.headers().get(HttpHeaderNames.SERVER)) must beNone
      }
    }
  }

  private def req(path: String) = {
    new DefaultFullHttpRequest(
      HttpVersion.HTTP_1_1, HttpMethod.GET, path
    )
  }

  private class RouteTest(rule: ExecutableRule, path: String,
                          isStream: Boolean = false,
                          setup: Setup = Setup.default
                         ) extends Scope {
    def makeRequest(path: String): FullHttpRequest = req(path)
    val ro = (new Route).addRule(rule).toHandler(setup)
    val channel = new EmbeddedChannel(new LoggingHandler(LogLevel.DEBUG))
    channel.pipeline()
      .addFirst(new LoggingHandler(LogLevel.DEBUG))
      .addLast("codec", new HttpRequestDecoder())
      .addLast("aggregator", new HttpObjectAggregator(512*1024))
      .addLast(ro)
    channels += channel
    val request = makeRequest(path)
    channel.writeInbound(request)
    var status = HttpResponseStatus.NOT_IMPLEMENTED
    var contentType = "test/test"
    var content = ""
    var messagesSize = channel.outboundMessages().size
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
