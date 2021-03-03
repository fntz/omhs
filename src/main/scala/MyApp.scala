import com.github.fntz.omhs.methods.Methods
import com.github.fntz.omhs.{*, BodyParam, BodyReader, BodyWriter, CommonResponse, DefaultHttpHandler, HeaderParam, LongParam, ParamDSL, ParamDef, RegexParam, Route, RuleDSL, StringParam, UUIDParam, p}
import play.api.libs.json.Json

import java.util.UUID

object MyApp extends App {
  import ParamDSL._
  import p._
  import Methods._
  import RuleDSL._
  import DefaultHttpHandler._

  case class Person(id: Int, name: String)

  implicit val personBodyReader = new BodyReader[Person] {
    override def read(str: String): Person =
      Json.parse(str).as[Person](Json.reads[Person])
  }
  implicit val bodyWriter = new BodyWriter[String] {
    override def write(w: String): CommonResponse = {
      new CommonResponse(
        200, "text/plain", w
      )
    }
  }
  implicit val bodyWriterPerson = new BodyWriter[Person] {
    override def write(w: Person): CommonResponse = {
      new CommonResponse(
        200,
        "application/json",
        Json.toJson(w)(Json.writes[Person]).toString
      )
    }
  }

  val x = "test"
  val xx = "/a/".r
  val k = RegexParam(xx)

//  val r1 = post("api" / BodyParam[Person]) ~> { (x: Person) =>
//    println("="*100)
//    x
//  }
//
//  println(s"---------> $r1")

  val r = get(x / HeaderParam("User-Agent")) ~> { (x: String) =>
    println(s"-------- ${x}")
    s"tst: ${x}"
  }

  val t = (new Route).addRule(r)
//  val t = r :: r1

  DefaultServer.run(9000, t.toHandler)





}
