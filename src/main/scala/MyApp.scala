import com.github.fntz.omhs.methods.Methods
import com.github.fntz.omhs._
import io.netty.handler.codec.http.multipart.MixedFileUpload
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

object MyApp extends App {
  import DefaultHttpHandler._
  import Methods._
  import ParamDSL._
  import p._
  import ParamD._
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


  val q = "asd"
  val rs = get("test" / header("dasd")) ~> { (x: String) =>
    println("~"*100)
    println("done")
    ""
  }
//
//  val t = (new Route).addRule(rs)
//
//  DefaultServer.run(9000, t.toHandler)

//  val s = new HttpServer
//  s.run(9000)







}
