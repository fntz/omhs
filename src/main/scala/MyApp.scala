import com.github.fntz.omhs.methods.Methods
import com.github.fntz.omhs.{*, CommonResponse, DefaultHttpHandler, LongParam, ParamDSL, ParamDef, RegexParam, RuleDSL, StringParam, UUIDParam, p}

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
    new CommonResponse(200, "text/plain", s"123: ${x}")
  }

  println(s"---------> $r1")

  val r = get(x / LongParam) ~> { (x: Long) =>
    println("--------")
    new CommonResponse(200, "text/plain", s"123: ${x}")
  }


  val t = r :: r1

  DefaultServer.run(9000, t.toHandler)





}
