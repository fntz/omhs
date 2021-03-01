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

  val r1 = Get().path("test").path(StringParam) ~> { x: String =>
    "asd"
  }

  val r2 = Get().path("test").path(LongParam)
    .path(StringParam) ~> { (x: Long, y :String) =>
    "qwe"
  }

  val r3 = Post()
    .path("example").body[Person]() ~> { (person: Person) =>
    println(s"========> ${person}")
    "333"
  }
  val r = r1 ++ r2 ++ r3

  r.onUnhandled { r =>
    println(s"reason: $r")
    "boom"
  }

  DefaultServer.run(9000, r.toHandler)

//  val x = "test"
//
//  get(x / LongParam) ~> { x: Long =>
//    println(x)
//  }


}
