import com.github.fntz.omhs.methods._
import com.github.fntz.omhs._
import play.api.libs.json._


object Main extends App {
  import ParamDSL._
  import p._
  import Methods._
  import DefaultHttpHandler._
  import RuleDSL._

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

  val r1 = Get().path("test").path(StringParam) ~> { x: String =>
    "asd"
  }

  val r2 = Get().path("test").path(LongParam)
    .path(StringParam) ~> { (x: Long, y: String) =>
    s"qwe: ${x} and ${y}"
  }

  val r3 = Post()
    .path("example").body[Person]() ~> { (person: Person) =>
    println(s"========> ${person}")
    person
  }
  val r = r1 ++ r2 ++ r3

  r.onUnhandled { r =>
    println(s"reason: $r")
    new CommonResponse(500, "text/plain", r.toString)
  }

  DefaultServer.run(9000, r.toHandler)

//  val x = "test"
//
//  get(x / LongParam |: body[Person] |: header["X-Header"])
  //  ~> { (x: Long, person, xHeader) =>
//    println(x)
//  }


}
