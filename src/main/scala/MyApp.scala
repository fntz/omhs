import com.github.fntz.omhs._
import com.github.fntz.omhs.internal.{ExecutableRule, ParamDef, StringDef}
import com.github.fntz.omhs.moar.MutableState
import io.netty.handler.codec.http.multipart.MixedFileUpload
import com.github.fntz.omhs.swagger.{ExternalDocumentation, Response, Server, SwaggerImplicits}
import io.netty.channel.ChannelPipeline
import io.netty.handler.codec.http.{FullHttpResponse, HttpMethod, HttpResponseStatus}
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

  import com.github.fntz.omhs.moar._

  /*
  // how to do:

   if (x == "asd")
     status 404
   else
     status 200

    response is fullHttpResponse

    contentType("json") => replace as
      response.headers().set("content-type", "json")

    get(string / "test") ~> run { (x: String) =>
       contentType("json")
       cookie("asd", "dsa")
       status 200
       // or status HttpResponseStatus.OK
       "done"
    } => ExecutableRule(r: get(string / "test")) {

      def run2(defs: List[ParamDef]) = {
        val response = new DefaultHttpResponse()
        response.headers.set("content-type", "json")
        response.headers.set("cookie", "asd=dsa")
        response.setStatus(200)
        "done" // <--------------- TODO how to transform? is it needs to transform?
      }
    }

   */
  val x = 100
//  val z = get(string) >> route {
//    if (x == 100) {
//      contentType("application/javascript")
//    } else {
//      contentType("application/javascript")
//    }
//    AsyncResult.completed(CommonResponse.empty)
//  }

  // content is not needed


  val k = get("test" / string) ~> route { (x1: String) =>
    if (x1 == "foo") {
      status(201)
      status(200)
      contentType("application/js")
      "asd"
    }
    else {
      status(404)
      contentType("text/plain")
      "not found"
    }
  }

  val route1 = new Route().addRule(k)

  OMHSServer.run(9000, route1.toHandler)


}
