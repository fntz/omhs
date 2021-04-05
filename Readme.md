
# One More Http Server

Routing DSL on Netty
 
* no additional dependencies (shapeless/scalaz/zio/cats/etc...), only Netty (scala-reflect for compiling time)

# install:

```scala
"com.github.fntz" %% "omhs-dsl" % "0.0.5"
// play-json support
"com.github.fntz" %% "omhs-play-json-support" % "0.0.5"
// circle-json support
"com.github.fntz" %% "omhs-circe-support" % "0.0.5"
// jsoniter support
"com.github.fntz" %% "omhs-jsoniter-support" % "0.0.5"
```

### before work:
1. add `-Ydelambdafy:inline` in `scalacOptions`
2. add reflect: `"org.scala-lang" % "scala-reflect" % scalaVersion.value % "compile"`
3. add `Netty` libraries: 

```sbt
"io.netty" % "netty-codec-http" % NettyVersion,
"io.netty" % "netty-codec-http2" % NettyVersion
```

# idea
`OMHS` is a library for creating routing on top of `Netty`, like [sinatra.rb](http://sinatrarb.com/).

`OMHS` will execute `function` on every path match.

`/foo/bar` -> execute function.

Params should be passed from the matched path to function as arguments.

`/foo/:string: -> {(s: String) => ...}`

Every function should return an object which should be possible to deserialize to HTTP-response.

In `OMHS` it is `Response`-type. For simple requests (fire-and-forget let's say) I use `CommonResponse`,
for streaming is `StreamResponse`.

But in everyday life, we do not work usually with objects like a simple string,
number, most of the cases are get strings/numbers from a database, or a remote connection, these results probably are wrapped into scala `Future`, or `zio.Task`, or another IO-like structure.
So for compatibility with another's libraries,
OMHS use the `AsyncResult` object to translate library-wrapped result to OMHS result.
Therefore every function should return `AsyncResult` of `Response`.

```scala
get("test" / uuid) ~> {(uuid: UUID) =>
  AsyncResult.completed(CommonResponse.plain(s"$uuid".getBytes))
}
```

Transform ZIO-Task to `AsyncResult`:

```scala
val value = zio.Runtime.default.unsafeRun(task) // task returns CommonResponse
AsyncResult.completed(value)
```

Paths are described as method + url like structure: `/foo/bar/` +
additional helpers: `uuid`/`string`/`long`/and `regex`/ or full url-path matcher: `*`.

`headers` and `cookies` do not participate in matching, just paths.

For deserializing `body`/`query` need to implement a deserialization strategy:
transform from raw string to necessary object.

# Examples:

[code](https://github.com/fntz/omhs/blob/master/src/main/scala/MyApp.scala)

[code based on zio.Task](https://github.com/truerss/truerss/blob/master/src/main/scala/truerss/api/SourcesApi.scala)

# Using

### Basic

```scala
import com.github.fntz.omhs.RoutingDSL._
import com.github.fntz.omhs.AsyncResult
import AsyncResult.Implicits._ // useful implicits for string/futures
// simple methods
get(string / "test" / uuid) ~> { (x: String, u: UUID) => 
  "done"  
}

// * 
get("test" / *) ~> {(xs: List[String]) => 
  "done"
}

// alternative syntax
get("foo" | "bar") ~> { (choice: String) =>
  "done"  
}
```

### Headers/Cookies syntax 

```scala
// headers / cookies 
post("test" << header("User-Agent") << cookie("name") << header("Accept")) 
  ~> {(userAgent: String, name: String, accept: String)} => {
  "done"  
}
```

### Query Readers

for reading Query part (`?foo=bar`) need to implement `QueryReader`: 

```scala
case class SearchQuery(query: String) 
implicit val querySearchReader = new QueryReader[SearchQuery] {
  override def read(queries: Map[String, Iterable[String]]): Option[SearchQuery] = {
    queries.get("query").flatMap(_.headOption).map(SearchQuery)
  }
}
```

```scala
// queries

("test" :? query[SearchQuery]) ~> {(q: SearchQuery) => "done" }
```

### Read Body

for reading body from current request need to implement `BodyReader`. 

```scala
case class Person(id: Int)
implicit val personBodyReader = new BodyReader[Person] {
  override def read(str: String): Person = ???
}
```

and then:

```scala
post("test" <<< body[Person]) ~> { (p: Person) ~> "done" }
```

Curently the project supports [play-json](https://github.com/playframework/play-json),  
[circle](https://github.com/circe/circe), and [jsoniter](https://github.com/plokhotnyuk/jsoniter-scala)

### Files

```scala
import io.netty.handler.codec.http.multipart.FileUpload

post("test" <<< file) ~> {(f: List[FileUpload]) => "done"}
```

### Access to the Current Request

```scala
get(string / "test") ~> { (s: String, request: CurrentHttpRequest) => 
  "done"
}
```

### Streaming

```scala 
import com.github.fntz.omhs.streams.ChunkedOutputStream
import AsyncResult.Streaming._ // <- from stream to AsyncResult
get("steaming") ~> { (stream: ChunkedOutputStream) => 
    stream.write("123".getBytes())
    stream.write("456".getBytes())
    stream.write("789".getBytes())
    // or with << 
    stream << "000"
    stream  
}
```

Note: ChunkedOutputStream must be last or penultimate argument:

`(stream: ChunkedOutputStream, req: CurrentHttpRequest) =>` or `(req: CurrentHttpRequest, stream: ChunkedOutputStream) =>` is valid.


### From Scala Future to AsyncResponse

```scala

implicit val ec = executor

import com.github.fntz.omhs.AsyncResult.Implicits._ 

get("persons" / long) ~> {(id: Long) => 
  Future(DB.persons.byId(id))      // as example
}

```

# Moar? sinatra like dsl

```scala 
import moar._ 

val rule = get("test" / string) ~> route { (x: String) => 
  if (x == "foo") {
    implicit val enc = ServerCookieEncoder.STRICT // need for setting cookie
    status(200)
    setHeader("foo", "bar")
    setCookie("asd", "qwe")
    val c = new DefaultCookie("a", "b")
    c.setDomain("example.com")
    setCookie(c)
    setHeader("x-header", "v-value")
    contentType("apllication/custom-type")
    "done" 
  } else {
    status(400)
    setHeader("y-header", "y-value")
    contentType("application/custom-another-type")
    "not-found"
  }
}

// available functions are:
- contentType
- status
- setCookie
- setHeader

```

### Error handling in application

```scala
val route = new Route().addRules(r1, r2, r3).onUnhandled {
    case PathNotFound(p) =>
      CommonResponse.json(404, s"$p not found")
    case _ =>
      CommonResponse.json(500, "boom")
  }
```

#### reasons are:

```
+ PathNotFound(path: String)
+ QueryIsUnparsable(params: Map[String, Iterable[String]])
+ CookieIsMissing(cookieName: String)
+ HeaderIsMissing(headerName: String)
+ BodyIsUnparsable(ex: Throwable)
+ FilesIsUnparsable(ex: Throwable)
+ UnhandledException(ex:  Throwable)
```

### Options:

```scala
val customSetup = Setup(
  timeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME
    .withZone(ZoneOffset.UTC).withLocale(Locale.US),
  sendServerHeader = false,
  cookieDecoderStrategy = CookieDecoderStrategy.Lax,
  maxContentLength = 512*1024,
  enableCompression = false,
  chunkSize = 1000,
  isSupportHttp2 = true
)
```


### Run server

```scala 
val rule1 = ...
val rule2 = ...
val rule3 = ...

val route = new Route().addRules(rule1, rule2, rule3)
val server = OMHSServer.init(9000, route.toHandler)

// you can change ServerBootstrap
val server = OMHSServer.run(
    port = 9000, 
    handler = route.toHandler,
    sslContext = Some(OMHSServer.getJdkSslContext),
    serverBootstrapChanges = (s: ServerBootstrap) => {
        s.options(...).childOptions(...)
    }
  )

// then 

server.start()  
  
// and stop programmatically

server.stop()  

```

# check codegen: 
pass `-Domhs.logLevel=verbose|info|none` to sbt/options

### TODO

* more settings/swagger


# License: MIT

