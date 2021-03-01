import com.github.fntz.omhs.methods._
import com.github.fntz.omhs._

object Main extends App {
  import ParamDSL._
  import p._
  import Methods._
  import DefaultHttpHandler._
  import RuleDSL._

  val r1 = Get().path("test").path(StringParam) ~> { x: String =>
    "asd"
  }

  val r2 = Get().path("test").path(LongParam)
    .path(StringParam) ~> { (x: Long, y :String) =>
    "qwe"
  }

  val r3 = Post().path("example") ~> { () =>
    "333"
  }
  val r = r1 ++ r2 ++ r3

  DefaultServer.run(9000, r.toHandler)

//  val x = "test"
//
//  get(x / LongParam) ~> { x: Long =>
//    println(x)
//  }


}
