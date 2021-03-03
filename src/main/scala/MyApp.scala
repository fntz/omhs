import com.github.fntz.omhs.methods.Methods
import com.github.fntz.omhs._
import play.api.libs.json.Json

import scala.concurrent.Future

object MyApp extends App {
  import DefaultHttpHandler._
  import Methods._
  import ParamDSL._
  import p._
  import AsyncResult._

  import scala.concurrent.ExecutionContext.Implicits.global

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

  implicit def bwf[T](implicit writer: BodyWriter[T]) = new BodyWriter[Future[T]] {
    override def write(w: Future[T]): Response = {
      AsyncResponse(w.toAsync)
    }
  }

  val x = "test"
  val xx = "/a/".r
  val k = RegexParam(xx)

  val r1 = post("api" / BodyParam[Person]) ~> { (x: Person) =>
    Future{
      x
    }
  }

  val r = get(x / HeaderParam("User-Agent")) ~> { (x: String) =>
    println(s"-------- ${x}")
    s"tst: ${x}"
  }

//  val t = (new Route).addRule(r1)
  val t = r :: r1

  DefaultServer.run(9000, t.toHandler)





}
