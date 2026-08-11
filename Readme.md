# One More Http Server

Routing DSL on Netty.

* No extra functional stack (shapeless / scalaz / zio / cats / …) — only Netty.
* Cross-built for **Scala 2.13** and **Scala 3**. The routing DSL is the same on both.

# Install

```scala
"com.github.fntz" %% "omhs-dsl" % "0.0.6"
// optional JSON modules
"com.github.fntz" %% "omhs-play-support" % "0.0.6"
"com.github.fntz" %% "omhs-circe-support" % "0.0.6"
"com.github.fntz" %% "omhs-jsoniter-support" % "0.0.6"
```

### Before you start

Scala 2 and Scala 3 need slightly different project settings.

**Scala 2.13**

```sbt
scalacOptions += "-Ydelambdafy:inline" // https://github.com/scala/bug/issues/10554

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % "compile",
  "io.netty" % "netty-codec-http" % NettyVersion,
  "io.netty" % "netty-codec-http2" % NettyVersion
)
```

**Scala 3**

```sbt
// no -Ydelambdafy:inline, no scala-reflect
libraryDependencies ++= Seq(
  "io.netty" % "netty-codec-http" % NettyVersion,
  "io.netty" % "netty-codec-http2" % NettyVersion
)
```

# Idea

`OMHS` is a Netty routing library in the spirit of [sinatra.rb](http://sinatrarb.com/).

When a request matches a path, OMHS runs your function and passes path/header/body parameters as ordinary function arguments:

```text
/foo/bar          ->  () => ...
/foo/:string      ->  (s: String) => ...
```

Every handler should return something that can become an HTTP response. Internally that is a `Response` (`CommonResponse` for normal replies, `StreamResponse` for streaming).

In real apps the payload often comes from a DB or remote call and is wrapped in `Future`, `zio.Task`, etc. OMHS uses `AsyncResult` as the bridge: handlers return `AsyncResult`, and implicits/givens can lift `String`, `Future[T]`, and so on into it.

```scala
get("test" / uuid) ~> { (id: UUID) =>
  AsyncResult.completed(CommonResponse.plain(s"$id".getBytes))
}
```

Example: turn a ZIO task into `AsyncResult`:

```scala
val value = zio.Runtime.default.unsafeRun(task) // task yields CommonResponse
AsyncResult.completed(value)
```

Paths are written as HTTP method + URL pieces, with helpers such as `uuid`, `string`, `long`, `regex`, and the catch-all `*`.

Headers and cookies are not part of path matching; they are extracted and passed into the handler when you declare them.

For `body` / `query` you provide a reader that turns the raw input into your type.

# Examples

* [Sample app](https://github.com/fntz/omhs/blob/master/src/main/scala/MyApp.scala)
* [ZIO-based example](https://github.com/truerss/truerss/blob/master/src/main/scala/truerss/api/SourcesApi.scala)

# Using

The snippets below use the same DSL on Scala 2 and Scala 3. Where setup differs (implicits vs givens), both forms are shown.

### Basic

```scala
import com.github.fntz.omhs.RoutingDSL._
import com.github.fntz.omhs.AsyncResult
import AsyncResult.Implicits._ // String / Future → AsyncResult

get(string / "test" / uuid) ~> { (x: String, u: UUID) =>
  "done"
}

get("test" / *) ~> { (xs: List[String]) =>
  "done"
}

get("foo" | "bar") ~> { (choice: String) =>
  "done"
}
```

### Headers / cookies

```scala
post("test" << header("User-Agent") << cookie("name") << header("Accept")) ~> {
  (userAgent: String, name: String, accept: String) =>
    "done"
}
```

### Query readers

To read the query string (`?foo=bar`), implement `QueryReader`.

**Scala 2**

```scala
case class SearchQuery(query: String)

implicit val querySearchReader: QueryReader[SearchQuery] =
  new QueryReader[SearchQuery] {
    override def read(queries: Map[String, Iterable[String]]): Option[SearchQuery] =
      queries.get("query").flatMap(_.headOption).map(SearchQuery)
  }
```

**Scala 3**

```scala
case class SearchQuery(query: String)

given QueryReader[SearchQuery] with
  def read(queries: Map[String, Iterable[String]]): Option[SearchQuery] =
    queries.get("query").flatMap(_.headOption).map(SearchQuery)
```

Then, on both Scala 2 and Scala 3:

```scala
get("test" :? query[SearchQuery]) ~> { (q: SearchQuery) =>
  "done"
}
```

### Body readers

Implement `BodyReader` to parse the request body.

**Scala 2**

```scala
case class Person(id: Int)

implicit val personBodyReader: BodyReader[Person] =
  new BodyReader[Person] {
    override def read(str: String): Person = ???
  }
```

**Scala 3**

```scala
case class Person(id: Int)

given BodyReader[Person] with
  def read(str: String): Person = ???
```

Then, on both:

```scala
post("test" <<< body[Person]) ~> { (p: Person) =>
  "done"
}
```

JSON helpers are available for [play-json](https://github.com/playframework/play-json),
[circe](https://github.com/circe/circe), and [jsoniter](https://github.com/plokhotnyuk/jsoniter-scala).

### Files

```scala
import io.netty.handler.codec.http.multipart.FileUpload

post("test" <<< file) ~> { (files: List[FileUpload]) =>
  "done"
}
```

### Current request

```scala
get(string / "test") ~> { (s: String, request: CurrentHttpRequest) =>
  "done"
}
```

`CurrentHttpRequest` (and `ChunkedOutputStream`) must be the last argument, or the last two when both are used.

### Streaming

```scala
import com.github.fntz.omhs.streams.ChunkedOutputStream
import AsyncResult.Streaming._ // ChunkedOutputStream → AsyncResult

get("streaming") ~> { (stream: ChunkedOutputStream) =>
  stream.write("123".getBytes())
  stream.write("456".getBytes())
  stream.write("789".getBytes())
  stream << "000"
  stream
}
```

Valid last-argument shapes:

```scala
(stream: ChunkedOutputStream, req: CurrentHttpRequest) => ...
(req: CurrentHttpRequest, stream: ChunkedOutputStream) => ...
```

### From `Future` to `AsyncResult`

```scala
implicit val ec: ExecutionContext = executor // Scala 2
// given ExecutionContext = executor        // Scala 3

import com.github.fntz.omhs.AsyncResult.Implicits._

get("persons" / long) ~> { (id: Long) =>
  Future(DB.persons.byId(id))
}
```

# Moar? Sinatra-like DSL

`route { ... }` rewrites helpers such as `status` / `contentType` onto a mutable response state. The same code works on Scala 2 and Scala 3.

```scala
import com.github.fntz.omhs.moar._
import io.netty.handler.codec.http.cookie.{DefaultCookie, ServerCookieEncoder}

val rule = get("test" / string) ~> route { (x: String) =>
  if (x == "foo") {
    implicit val enc: ServerCookieEncoder = ServerCookieEncoder.STRICT // or `given` on Scala 3
    status(200)
    setHeader("foo", "bar")
    setCookie("asd", "qwe")
    val c = new DefaultCookie("a", "b")
    c.setDomain("example.com")
    setCookie(c)
    setHeader("x-header", "v-value")
    contentType("application/custom-type")
    "done"
  } else {
    status(400)
    setHeader("y-header", "y-value")
    contentType("application/custom-another-type")
    "not-found"
  }
}
```

Available helpers inside `route`:

* `contentType`
* `status`
* `setCookie`
* `setHeader`

These must not be used outside a `route` block (the compiler rejects that).

### Error handling

```scala
val route = new Route().addRules(r1, r2, r3).onUnhandled {
  case PathNotFound(p) =>
    CommonResponse.json(404, s"$p not found")
  case _ =>
    CommonResponse.json(500, "boom")
}
```

Unhandled reasons:

```text
PathNotFound(path: String)
QueryIsUnparsable(params: Map[String, Iterable[String]])
CookieIsMissing(cookieName: String)
HeaderIsMissing(headerName: String)
BodyIsUnparsable(ex: Throwable)
FilesIsUnparsable(ex: Throwable)
UnhandledException(ex: Throwable)
```

### Options

```scala
val customSetup = Setup(
  timeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME
    .withZone(ZoneOffset.UTC).withLocale(Locale.US),
  sendServerHeader = false,
  cookieDecoderStrategy = CookieDecoderStrategy.Lax,
  maxContentLength = 512 * 1024,
  enableCompression = false,
  chunkSize = 1000,
  isSupportHttp2 = true
)
```

### Run the server

```scala
val rule1 = ...
val rule2 = ...
val rule3 = ...

val route = new Route().addRules(rule1, rule2, rule3)
val server = OMHSServer.init(9000, route.toHandler)

// or customize ServerBootstrap
val server = OMHSServer.run(
  port = 9000,
  handler = route.toHandler,
  sslContext = Some(OMHSServer.getJdkSslContext),
  serverBootstrapChanges = (s: ServerBootstrap) => {
    s.option(...).childOption(...)
  }
)

server.start()
server.stop()
```

# Macro / codegen logging

Pass `-Domhs.logLevel=verbose|info|none` to sbt / JVM options to inspect generated routing code.

### TODO

* More settings / swagger polish

# License

MIT
