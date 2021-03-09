import com.github.fntz.omhs.macros.RoutingImplicits
import com.github.fntz.omhs._
import io.netty.handler.codec.http.multipart.MixedFileUpload
import com.github.fntz.omhs.swagger.{ExternalDocumentation, Response, Server, SwaggerImplicits}
import io.netty.channel.ChannelPipeline
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import play.api.libs.json.Json
import com.github.fntz.omhs.playjson.JsonSupport

import scala.concurrent.{ExecutionContext, Future}

object MyApp extends App {
  import OMHSHttpHandler._
  import RoutingImplicits._
  import ParamDSL._
  import p._
  import AsyncResult._
  import AsyncResult.Implicits._
  import AsyncResult.Streaming._
  import JsonSupport._

  import scala.concurrent.ExecutionContext.Implicits.global

  case class Person(id: Int, name: String)

  implicit val personJson = Json.format[Person]
  implicit val personBodyReader = JsonSupport.writer[Person]()
  implicit val bodyWriter = new BodyWriter[String] {
    override def write(w: String): CommonResponse = {
      CommonResponse(
        200, "text/plain", w
      )
    }
  }

  implicit val bodyWriterPerson = JsonSupport.reader[Person]()

  import SwaggerImplicits._
  val q = "asd"

  case class Search(query: String)
  implicit val queryStringReader = new QueryReader[Search] {
    override def read(queries: Map[String, Iterable[String]]): Option[Search] = {
      queries.get("query").flatMap(_.headOption).map(Search)
    }
  }

//  val rs = get("test" / query[Search]) ~> { (q: Search) =>
//    println("~"*100)
//    println(s"done: ${q}")
//    "ok"
//  }

  implicit val bodyReader = new BodyReader[Search] {
    override def read(str: String): Search = Search("dsa")
  }

  val z = get("asd" / "asd") ~> { () =>
    "asd"
  }

  val rf = get("file" / string) ~> { (l: String) =>
    "done"
  }

//  val rss = rs
//    .toSwagger.withTags("foo", "bar")
//    .withResponse(200, Response("description"))
//    .withDescription("test api")
//    .withExternalDocs(ExternalDocumentation("http://example.com", Some("ext")))
//    .withOperationId("opId")
//    .withSummary("summary")
//    .withDeprecated(false)


  val t = (new Route).onEveryResponse((r: FullHttpResponse) => {
    r.headers().set("text", "boom")
    r
  })
    .addRule(rf)
    .toSwagger.swagger("swagger")

  OMHSServer.run(9000, t.toHandler)

//  DefaultServer.run(9000, t.toHandler)

//  val s = new HttpServer
//  s.run(9000)







}
