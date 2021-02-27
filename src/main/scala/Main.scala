import com.github.fntz.omhs.methods._
import com.github.fntz.omhs._

object Main extends App {
  import ParamDSL._
  import p._
  import Methods._

  val x = "test"

  get(x / LongParam) ~> { x: Long =>
    println(x)
  }


  /*
  idea is :
    enum UnmatchReasons =
      pathNotFound
      cookieIsNotPresent
      headerIsNotPresent
      bodyIsUnparsable
      exception(t: Throwable)

    case class RouteMat() {
      val xs = ArrayBuffer[RB]
      def add(b: RB) = xs += b

      def onUnMatched = { reason =>

      }
    }

    case class RouteBuilder(m: method?) {
      def path(str: String)
      def path[T](n: name?) // swagger
      def cookie
      def header
      def body[T: Decoder]

      def handler[T...](f: T... => Result]

      def ::(o: RB) = new RouteMat(this ++ o)
      def ::(o: RM) = o.add(this)
    }

    val r = RouteBuilder(get)
     // or .get
     .path("test")
     .path[Long]
     .header("X-Custom-Header") // validator ? maybe in future
     .body[Person]

     val t = RouteBuilder...

     val e = RouteBuilder...



   */



}
