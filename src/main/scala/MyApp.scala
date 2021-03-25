import com.github.fntz.omhs._
import com.github.fntz.omhs.playjson.JsonSupport
import com.github.fntz.omhs.streams.ChunkedOutputStream
import io.netty.handler.codec.http.HttpResponse
import io.netty.handler.codec.http.cookie.{Cookie, ServerCookieEncoder}
import io.netty.handler.codec.http.multipart.FileUpload
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame
import io.netty.util.CharsetUtil
import play.api.libs.json.Json

import scala.concurrent.Future



object MyApp extends App {
  import RoutingDSL._
  import com.github.fntz.omhs.moar._
  import AsyncResult.Implicits._
  import AsyncResult.Streaming._

  import scala.concurrent.ExecutionContext.Implicits.global

  case class Person(id: Int, name: String)

  implicit val personJson = Json.format[Person]
  implicit val personBodyReader = JsonSupport.reader[Person]()
  implicit val personBodyWriter = JsonSupport.writer[Person]()

  case class Search(query: String)
  implicit val searchQueryReader = new QueryReader[Search] {
    override def read(queries: Map[String, Iterable[String]]): Option[Search] = {
      queries.get("query").flatMap(_.headOption).map(Search)
    }
  }


  implicit val cookieEncoder = ServerCookieEncoder.STRICT

  val simpleGet = get("test" / "foo") ~> {() =>
    "foo"
//  or  AsyncResult.completed("foo")
  }

  val getWithParams = get("bar" / string) ~> {(x: String) =>
    s"given: $x"
  }

  val currentReq = get("example") ~> {(req: CurrentHttpRequest) =>
    req.path
  }

  val list = get("example" / "foo" / *) ~> { (xs: List[String]) =>
    xs.mkString(", ")
  }

  val chunked = get("chunks") ~> {(stream: ChunkedOutputStream) =>
    stream << "123"
    stream << "456"
    stream << "789"
  }

  val postPerson = post("person" <<< body[Person]) ~> {(p: Person) =>
    Future(p)
  }

  val fileRoute = post("file" <<< file) ~> {(fs: List[FileUpload]) =>
    println(fs.head.content().toString(CharsetUtil.UTF_8))
    fs.map(_.getName).mkString(", ")
  }

  val queryRoute = get("search" :? query[Search]) ~> {(q: Search) =>
    q.query
  }

  val cookieOrHeaderRoute = get("ch" << header("foo") << cookie("abc") << header("mmm") ) ~> {(h1: String, c: Cookie, h2: String) =>
    h1 + c + h2
  }

  val moreRouting = get("more") ~> route {() =>
    setHeader("test", "abc")
    status(201)
    "done"
  }

  val rewriterH2 = (r: DefaultHttp2HeadersFrame) => {
    r.headers().set("test", "qwe")
    r
  }
  val rewriterH1 = (r: HttpResponse) => {
    r.headers().set("test", "qwe")
    r
  }
  val route1 = new Route().addRules(
    simpleGet,
    getWithParams,
    currentReq,
    list,
    chunked,
    postPerson,
    fileRoute,
    queryRoute,
    cookieOrHeaderRoute,
    moreRouting
  )
    .onEveryHttp2Response(rewriterH2)
    .onEveryHttpResponse(rewriterH1)
    .onUnhandled {
      case PathNotFound(p) =>
        CommonResponse.json(404, s"$p not found")
      case _ =>
        CommonResponse.json(500, "boom")
    }


  val server = OMHSServer.init(9000, route1.toHandler(Setup.default), None)

  server.start()

//  server.stop()


}
