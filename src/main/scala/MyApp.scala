import com.github.fntz.omhs.methods.Methods
import com.github.fntz.omhs.{*, BodyWriter, CommonResponse, DefaultHttpHandler, LongParam, ParamDSL, ParamDef, RegexParam, RuleDSL, StringParam, UUIDParam, p}

import java.util.UUID

object MyApp extends App {
  import ParamDSL._
  import p._
  import Methods._
  import RuleDSL._
  import DefaultHttpHandler._

  case class Person(id: Int, name: String)

  implicit val bodyWriter = new BodyWriter[String] {
    override def write(w: String): CommonResponse = {
      new CommonResponse(
        200, "text/plain", w
      )
    }
  }

  val x = "test"
  val xx = "/a/".r
  val k = RegexParam(xx)

  val r1 = get("api" / StringParam) ~> { (x: String) =>
    println("="*100)
    s"123: ${x}"
  }

  println(s"---------> $r1")

  val r = get(x / LongParam) ~> { (x: Long) =>
    println("--------")
    s"tst: ${x}"
  }


  val t = r :: r1

  DefaultServer.run(9000, t.toHandler)





}
