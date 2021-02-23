import com.github.fntz.omhs.methods._
import com.github.fntz.omhs._
import scala.reflect.runtime.universe._

object Main extends App {
  import ParamDSL._
  import p._
  import Methods._

  get("test" / StringParam) ~> { x: Long =>
    println(x)
  }

//  implicit class pext(val x: p) extends AnyVal {
//    def ~>(f: () => Unit) = println(0)
//    def ~>[T](f: T => Unit) = println(1)
//    def ~>[T1, T2](f: (T1, T2) => Unit) = println(2)
//  }
//
//  get("test" / LongParam) ~> { x: Long =>
//    println(x)
//  }
//
//  get("test" / LongParam) ~> { (x: Long, a: String) =>
//    println(s"$x $a")
//  }
//
//  get("test"/ StringParam) ~> { () =>
//    println(0)
//  }

  /*
    get("test" / LongParam) ~> { (x: Long) =>

    }
   */

//  Methods.get("test" / LongParam) { case Tuple1(rq: Long) =>
//    println("333")
//    println(rq)
//  }




}
