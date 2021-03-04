import com.github.fntz.omhs.methods.Methods
import com.github.fntz.omhs._
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

object MyApp extends App {
  import DefaultHttpHandler._
  import Methods._
  import ParamDSL._
  import p._
  import AsyncResult._
  import AsyncResult.Implicits._

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

  val x = "test"
  val xx = "/a/".r
  val k = RegexParam(xx)
/*
  val r1 = post("api" / BodyParam[Person]) ~> { (x: Person, req: CurrentHttpRequest) =>
    println(s"----> ${req.headers}")
    Future {
      x
    }
  } */

  val r = get(HeaderParam("User-Agent") / x / StringParam / LongParam) ~> { (x: String, z: String, y: Long) =>
    s"tst: $y ----> ${x} and $z"
  }
//
//
//
  val t = (new Route).addRule(r)
//  val t = r :: r1

  DefaultServer.run(9000, t.toHandler)

//  val s = new HttpServer
//  s.run(9000)







}
