package com.github.fntz.omhs.impl

import munit.FunSuite

import scala.compiletime.testing.{typeCheckErrors, typeChecks}

class CompilationTests extends FunSuite {

  test("doesn't compile: CurrentRequest should be the last argument") {
    val errors = typeCheckErrors(
      """
         import com.github.fntz.omhs._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / string / string) ~> { (a: String, req: CurrentHttpRequest, b: String) =>
            "done"
         }
         """
    )
    assert(errors.exists(_.message.contains("CurrentHttpRequest must be the last argument in the function")))
  }

  test("doesn't compile: Stream should be the last argument") {
    val errors = typeCheckErrors(
      """
         import com.github.fntz.omhs.streams.ChunkedOutputStream
         import com.github.fntz.omhs._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / string / string) ~> { (a: String, stream: ChunkedOutputStream, b: String) =>
            "done"
         }
         """
    )
    assert(errors.exists(_.message.contains("ChunkedOutputStream must be the last argument in the function")))
  }

  test("doesn't compile: Stream or CurrentHttpRequest should be the last argument") {
    val errors = typeCheckErrors(
      """
         import com.github.fntz.omhs.streams.ChunkedOutputStream
         import com.github.fntz.omhs._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / string / string) ~> { (a: String, stream: ChunkedOutputStream,
          b: String, req: CurrentHttpRequest) =>
            "done"
         }
         """
    )
    assert(
      errors.exists(_.message.contains("must be the last arguments in the function"))
    )
  }

  test("doesn't compile: when parameters count is not the same as function arguments length") {
    val errors = typeCheckErrors(
      """
         import com.github.fntz.omhs._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / long / string) ~> { (s: String) =>
            "done"
         }
         """
    )
    assert(errors.exists(_.message.contains("Args lengths are not the same")))
  }

  test("doesn't compile: if the alternative is in not params") {
    val errors = typeCheckErrors(
      """
         import com.github.fntz.omhs._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get(string / ("a" | "b" | "c")) ~> { (s: String) =>
            "done"
         }
         """
    )
    assert(errors.exists(_.message.contains("Args lengths are not the same")))
  }

  test("doesn't compile: when parameters are in incorrect sequence#1") {
    val errors = typeCheckErrors(
      """
         import com.github.fntz.omhs._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / long / string) ~> { (s: String, l: Long) =>
            "done"
         }
         """
    )
    assert(errors.exists(_.message.contains("Incorrect type for `s`, required: Long")))
  }

  test("doesn't compile: when parameters are in incorrect sequence#2") {
    val errors = typeCheckErrors(
      """
         import io.netty.handler.codec.http.multipart.FileUpload
         import com.github.fntz.omhs._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / long <<< file) ~> { (file: FileUpload, l: Long) =>
            "done"
         }
         """
    )
    assert(
      errors.exists(_.message.contains("Incorrect type for `file`, required: Long"))
    )
  }

  test("doesn't compile: `status` without `route`-function") {
    val errors = typeCheckErrors(
      """
         import com.github.fntz.omhs._
         import com.github.fntz.omhs.moar._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / long) ~> { (l: Long) =>
            status(200)
            "done"
         }
         """
    )
    assert(errors.exists(_.message.contains("`status` must be wrapped in an `route` block")))
  }

  test("doesn't compile: `setHeader` without `route`-function") {
    val errors = typeCheckErrors(
      """
         import com.github.fntz.omhs._
         import com.github.fntz.omhs.moar._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / long) ~> { (l: Long) =>
            setHeader("a", "b")
            "done"
         }
         """
    )
    assert(errors.exists(_.message.contains("`setHeader` must be wrapped in an `route` block")))
  }

  test("doesn't compile: `setCookie` without `route`-function") {
    val errors = typeCheckErrors(
      """
         import com.github.fntz.omhs._
         import com.github.fntz.omhs.moar._
         import AsyncResult.Implicits._
         import RoutingDSL._
         import io.netty.handler.codec.http.cookie.ServerCookieEncoder

         implicit val enc = ServerCookieEncoder.STRICT
         get("file" / long) ~> { (l: Long) =>
            setCookie("a", "b")
            "done"
         }
         """
    )
    assert(errors.exists(_.message.contains("`setCookie` must be wrapped in an `route` block")))
  }

  test("doesn't compile: `contentType` without `route`-function") {
    val errors = typeCheckErrors(
      """
         import com.github.fntz.omhs._
         import com.github.fntz.omhs.moar._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / long) ~> { (l: Long) =>
            contentType("text/plain")
            "done"
         }
         """
    )
    assert(errors.exists(_.message.contains("`contentType` must be wrapped in an `route` block")))
  }

  test("ignore arguments at all") {
    assert(typeChecks(
      """
         import com.github.fntz.omhs._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / string) ~> { () =>
            "done"
         }
         """
    ))
  }

  test("pass CurrentRequestParam") {
    assert(typeChecks(
      """
         import com.github.fntz.omhs._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / string) ~> { (a: String, req: CurrentHttpRequest) =>
            "done"
         }
         """
    ))
  }

  test("pass Stream") {
    assert(typeChecks(
      """
         import com.github.fntz.omhs.streams.ChunkedOutputStream
         import com.github.fntz.omhs._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / string) ~> { (a: String, stream: ChunkedOutputStream) =>
            "done"
         }
         """
    ))
  }

  test("Stream or CurrentHttpRequest should be the last argument#1") {
    assert(typeChecks(
      """
         import com.github.fntz.omhs.streams.ChunkedOutputStream
         import com.github.fntz.omhs._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / string) ~> { (a: String, stream: ChunkedOutputStream, req: CurrentHttpRequest) =>
            "done"
         }
         """
    ))
  }

  test("Stream or CurrentHttpRequest should be the last argument#2") {
    assert(typeChecks(
      """
         import com.github.fntz.omhs.streams.ChunkedOutputStream
         import com.github.fntz.omhs._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / string) ~> { (a: String, req: CurrentHttpRequest, stream: ChunkedOutputStream) =>
            "done"
         }
         """
    ))
  }

  test("pass Long") {
    assert(typeChecks(
      """
         import com.github.fntz.omhs._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / long) ~> { (l: Long) =>
            "done"
         }
         """
    ))
  }

  test("pass String") {
    assert(typeChecks(
      """
         import com.github.fntz.omhs._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / string) ~> { (l: String) =>
            "done"
         }
         """
    ))
  }

  test("pass UUID") {
    assert(typeChecks(
      """
         import com.github.fntz.omhs._
         import AsyncResult.Implicits._
         import RoutingDSL._
         import java.util.UUID

         get("file" / uuid) ~> { (l: UUID) =>
            "done"
         }
         """
    ))
  }

  test("pass Regex") {
    assert(typeChecks(
      """
         import com.github.fntz.omhs._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / regex("".r)) ~> { (l: String) =>
            "done"
         }
         """
    ))
  }

  test("pass Tail") {
    assert(typeChecks(
      """
         import com.github.fntz.omhs._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / *) ~> { (l: List[String]) =>
            "done"
         }
         """
    ))
  }

  test("alternative syntax") {
    assert(typeChecks(
      """
         import com.github.fntz.omhs._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("test" / ("a" | "b" | "c")) ~> { (alt: String) =>
            "done"
         }
         """
    ))
  }

  test("pass Header") {
    assert(typeChecks(
      """
         import com.github.fntz.omhs._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" << header("User-Agent")) ~> { (l: String) =>
            "done"
         }
         """
    ))
  }

  test("pass Cookie") {
    assert(typeChecks(
      """
         import io.netty.handler.codec.http.cookie.Cookie
         import com.github.fntz.omhs._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" << cookie("foo")) ~> { (l: Cookie) =>
            "done"
         }
         """
    ))
  }

  test("pass Query") {
    assert(typeChecks(
      """
         import com.github.fntz.omhs._
         import AsyncResult.Implicits._
         import RoutingDSL._

         case class Search(q: String)
         given QueryReader[Search] with
           override def read(queries: Map[String, Iterable[String]]): Option[Search] =
             queries.get("q").flatMap(_.headOption).map(Search)

         get("file" :? query[Search]) ~> { (q: Search) =>
            "done"
         }
         """
    ))
  }

  test("pass Body") {
    assert(typeChecks(
      """
         import com.github.fntz.omhs._
         import AsyncResult.Implicits._
         import RoutingDSL._

         case class Foo(id: Int)
         given BodyReader[Foo] with
           override def read(str: String): Foo = Foo(1)

         get("file" <<< body[Foo]) ~> { (l: Foo) =>
            "done"
         }
         """
    ))
  }

  test("pass File") {
    assert(typeChecks(
      """
         import io.netty.handler.codec.http.multipart.FileUpload
         import com.github.fntz.omhs._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" <<< file) ~> { (l: List[FileUpload]) =>
            "done"
         }
         """
    ))
  }

  test("success compilation#1") {
    assert(typeChecks(
      """
        import com.github.fntz.omhs._
        import AsyncResult.Implicits._
        import RoutingDSL._

        get(string) ~> { (x: String) => "done" }
      """
    ))
  }

  test("success compilation#2") {
    assert(typeChecks(
      """
        import com.github.fntz.omhs._
        import AsyncResult.Implicits._
        import RoutingDSL._
        import java.util.UUID

        get(string / "test" / long / uuid) ~> { (x: String, l: Long, u: UUID) => "done" }
      """
    ))
  }
}
