import com.github.fntz.omhs.methods.Methods
import com.github.fntz.omhs.{*, CommonResponse, DefaultHttpHandler, LongParam, ParamDSL, RegexParam, RuleDSL, StringParam, UUIDParam, p}

import java.util.UUID

object MyApp extends App {
  import ParamDSL._
  import p._
  import Methods._
  import RuleDSL._
  import DefaultHttpHandler._

  case class Person(id: Int, name: String)



  val x = "test"
  val xx = "/a/".r
  val k = RegexParam(xx)
  val r1 = get("api" / LongParam) ~> { (x: Long) =>
    println("="*100)
    new CommonResponse(200, "text/plain", s"lng: ${x}")
  }

  val r = get(x / *) ~> { (x: List[String]) =>
    println("--------")
    new CommonResponse(200, "text/plain", s"123: ${x}")
  }


  val t = r :: r1

  DefaultServer.run(9000, t.toHandler)





}
