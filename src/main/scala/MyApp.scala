import com.github.fntz.omhs._
import io.netty.handler.codec.http.multipart.MixedFileUpload
import com.github.fntz.omhs.swagger.{ExternalDocumentation, Response, Server, SwaggerImplicits}
import io.netty.channel.ChannelPipeline
import io.netty.handler.codec.http.{FullHttpResponse, HttpMethod}
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import play.api.libs.json.Json
import com.github.fntz.omhs.playjson.JsonSupport

import scala.concurrent.{ExecutionContext, Future}



object MyApp extends App {
  import AsyncResult._
  import AsyncResult.Implicits._
  import AsyncResult.Streaming._
  import JsonSupport._
  import RoutingDSL._
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

  implicit val bodyReader = new BodyReader[Search] {
    override def read(str: String): Search = Search("dsa")
  }

  val a = "a"
  val b = "b"
  val c = "c"
  val r = get("test" / (a | b | c)) ~> { () =>
    "done"
  }

  val ro = new Route().addRule(r)
  OMHSServer.run(9000, ro.toHandler)

}
