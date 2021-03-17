
# One More Http Server

Netty-based dsl
 
* No additional dependencies (shapeless/scalaz/zio/cats/etc...), only netty (scala-reflect for compiling time)

todo configure timeouts/message aggregator http2: length -> 413 too large
505 -  HTTP Version Not Supported ? 

# install:

```scala
 "com.github.fntz" %% "dsl" % "0.0.1-SNAPSHOT"
```

### before work: 
1. add `-Ydelambdafy:inline` in scalacOptions
2. add reflect: `"org.scala-lang" % "scala-reflect" % scalaVersion.value % "compile"`

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
If your work with play-json, then your could to add `"com.github.fntz" %% "omhs-play-support" % "0.0.1-SNAPSHOT"` 

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

get("asd") ~> {() => 
  Future("dsad")
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

```


### Options:

```scala
val customSetup = Setup(
  timeFormatter = ???,
  sendServerHeader = false,
  cookieDecoderStrategy = CookieDecoderStrategy.Lax,
  maxContentLength = 512*1024,
  enableCompression = false,
  chunkSize = 1000
)
```

### Run server

```scala 
val rule1 = ...
val rule2 = ...
val rule3 = ...

val route = new Route().addRule(rule1).addRule(rule2).addRule(rule3)
OMHSServer.run(9000, route.toHandler)

// change netty-pipeline or ServerBootstrap
OMHSServer.run(
    port = 9000, 
    handler = route.toHandler,
    serverBootstrapChanges = (s: ServerBootstrap) => {
        s.options(...).childOptions(...)
    }
  )

```

# check codegen: 
pass `-Domhs.logLevel=verbose|info|none` to sbt/options


  

todo:
* swagger (70% ready)

### License: MIT

