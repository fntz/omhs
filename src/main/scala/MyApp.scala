import com.github.fntz.omhs.methods.Methods
import com.github.fntz.omhs._
import io.netty.handler.codec.http.multipart.MixedFileUpload
import play.api.libs.json.Json
import com.github.fntz.omhs.swagger.{ExternalDocumentation, Response, Server, SwaggerImplicits}
import io.netty.channel.ChannelPipeline
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.logging.{LogLevel, LoggingHandler}

import scala.concurrent.{ExecutionContext, Future}

object MyApp extends App {
  import DefaultHttpHandler._
  import Methods._
  import ParamDSL._
  import p._
  import AsyncResult._
  import AsyncResult.Implicits._
  import AsyncResult.Streaming._

  import scala.concurrent.ExecutionContext.Implicits.global

  case class Person(id: Int, name: String)

  implicit val personBodyReader = new BodyReader[Person] {
    override def read(str: String): Person =
      Json.parse(str).as[Person](Json.reads[Person])
  }
  implicit val bodyWriter = new BodyWriter[String] {
    override def write(w: String): CommonResponse = {
      CommonResponse(
        200, "text/plain", w
      )
    }
  }

  implicit val bodyWriterPerson = new BodyWriter[Person] {
    override def write(w: Person): CommonResponse = {
      CommonResponse.json(
        Json.toJson(w)(Json.writes[Person]).toString
      )
    }
  }

  import SwaggerImplicits._
  val q = "asd"

  case class Search(query: String)
  implicit val queryStringReader = new QueryReader[Search] {
    override def read(queries: Map[String, List[String]]): Option[Search] = {
      queries.get("query").flatMap(_.headOption).map(Search)
    }
  }

  val rs = get("test" / query[Search]) ~> { (q: Search) =>
    println("~"*100)
    println(s"done: ${q}")
    "ok"
  }

  implicit val bodyReader = new BodyReader[Search] {
    override def read(str: String): Search = Search("dsa")
  }

  val rf = post("file" / file) ~> { (l: List[MixedFileUpload]) =>
    "done"
  }

  val rss = rs
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
    .addRule(rss).addRule(rf)
    .toSwagger.swagger("swagger")

  OHMSServer.run(9000, t.toHandler)

//  DefaultServer.run(9000, t.toHandler)

//  val s = new HttpServer
//  s.run(9000)







}
