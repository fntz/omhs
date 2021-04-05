package com.github.fntz.omhs.impl

import scala.tools.reflect.{ToolBox, ToolBoxError}
import munit.FunSuite

class CompilationTests extends FunSuite {

  test("doesn't compile: CurrentRequest should be the last argument") {
    doesntCompile(
      s"""
         import io.netty.handler.codec.http.multipart.FileUpload
         import com.github.fntz.omhs._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / string / string) ~> { (a: String, req: CurrentHttpRequest, b: String) =>
            "done"
         }
         """.stripMargin, s"CurrentHttpRequest must be the last argument in the function")
  }

  test("doesn't compile: Stream should be the last argument") {
    doesntCompile(
      s"""
         import com.github.fntz.omhs.streams.ChunkedOutputStream
         import com.github.fntz.omhs._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / string / string) ~> { (a: String, stream: ChunkedOutputStream, b: String) =>
            "done"
         }
         """.stripMargin, s"ChunkedOutputStream must be the last argument in the function")
  }

  test("doesn't compile: Stream or CurrentHttpRequest should be the last argument") {
    doesntCompile(
      s"""
         import com.github.fntz.omhs.streams.ChunkedOutputStream
         import com.github.fntz.omhs._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / string / string) ~> { (a: String, stream: ChunkedOutputStream,
          b: String, req: CurrentHttpRequest) =>
            "done"
         }
         """.stripMargin,
      s"com.github.fntz.omhs.CurrentHttpRequest or com.github.fntz.omhs.streams.ChunkedOutputStream must be the last arguments in the function")
  }

  test("doesn't compile: when parameters count is not the same as function arguments length") {
    doesntCompile(
      s"""
         import io.netty.handler.codec.http.multipart.FileUpload
         import com.github.fntz.omhs._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / long / string) ~> { (s: String) =>
            "done"
         }
         """.stripMargin, "Args lengths are not the same")
  }

  test("doesn't compile: if the alternative is in not params") {
    doesntCompile(
      s"""
         import com.github.fntz.omhs._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get(string / ("a" | "b" | "c")) ~> { (s: String) =>
            "done"
         }
         """.stripMargin, "Args lengths are not the same")
  }

  test("doesn't compile: when parameters are in incorrect sequence#1") {
    doesntCompile(
      s"""
         import io.netty.handler.codec.http.multipart.FileUpload
         import com.github.fntz.omhs._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / long / string) ~> { (s: String, l: Long) =>
            "done"
         }
         """.stripMargin, "Incorrect type for `s`, required: Long, given: String")
  }

  test("doesn't compile: when parameters are in incorrect sequence#2") {
    doesntCompile(
      s"""
         import io.netty.handler.codec.http.multipart.FileUpload
         import com.github.fntz.omhs._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / long <<< file) ~> { (file: FileUpload, l: Long) =>
            "done"
         }
         """.stripMargin, "Incorrect type for `file`, required: Long, given: io.netty.handler.codec.http.multipart.FileUpload")
  }

  test("doesn't compile: `status` without `route`-function") {
    doesntCompile(
      s"""
         import com.github.fntz.omhs._
         import com.github.fntz.omhs.moar._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / long) ~> { (l: Long) =>
            status(200)
            "done"
         }
         """.stripMargin, "`status` must be wrapped in an `route` block")
  }

  test("doesn't compile: `setHeader` without `route`-function") {
    doesntCompile(
      s"""
         import com.github.fntz.omhs._
         import com.github.fntz.omhs.moar._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / long) ~> { (l: Long) =>
            setHeader("a", "b")
            "done"
         }
         """.stripMargin, "`setHeader` must be wrapped in an `route` block")
  }

  test("doesn't compile: `setCookie` without `route`-function") {
    doesntCompile(
      s"""
         import com.github.fntz.omhs._
         import com.github.fntz.omhs.moar._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._
         import io.netty.handler.codec.http.cookie.ServerCookieEncoder

         implicit val enc = ServerCookieEncoder.STRICT
         get("file" / long) ~> { (l: Long) =>
            setCookie("a", "b")
            "done"
         }
         """.stripMargin, "`setCookie` must be wrapped in an `route` block")
  }

  test("doesn't compile: `contentType` without `route`-function") {
    doesntCompile(
      s"""
         import com.github.fntz.omhs._
         import com.github.fntz.omhs.moar._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / long) ~> { (l: Long) =>
            contentType("text/plain")
            "done"
         }
         """.stripMargin, "`contentType` must be wrapped in an `route` block")
  }

  test("ignore arguments at all") {
    compile(
      s"""
         import io.netty.handler.codec.http.multipart.FileUpload
         import com.github.fntz.omhs._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / string) ~> { () =>
            "done"
         }
         """.stripMargin)
  }

  test("pass CurrentRequestParam") {
    compile(
      s"""
         import io.netty.handler.codec.http.multipart.FileUpload
         import com.github.fntz.omhs._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / string) ~> { (a: String, req: CurrentHttpRequest) =>
            "done"
         }
         """.stripMargin)
  }

  test("pass Stream") {
    compile(
      s"""
         import com.github.fntz.omhs.streams.ChunkedOutputStream
         import com.github.fntz.omhs._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / string) ~> { (a: String, stream: ChunkedOutputStream) =>
            "done"
         }
         """.stripMargin)
  }

  test("Stream or CurrentHttpRequest should be the last argument#1") {
    compile(
      s"""
         import com.github.fntz.omhs.streams.ChunkedOutputStream
         import com.github.fntz.omhs._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / string) ~> { (a: String, stream: ChunkedOutputStream, req: CurrentHttpRequest) =>
            "done"
         }
         """.stripMargin)
  }

  test("Stream or CurrentHttpRequest should be the last argument#2") {
    compile(
      s"""
         import com.github.fntz.omhs.streams.ChunkedOutputStream
         import com.github.fntz.omhs._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / string) ~> { (a: String, req: CurrentHttpRequest, stream: ChunkedOutputStream) =>
            "done"
         }
         """.stripMargin)
  }

  test("pass Long") {
    compile(
      s"""
         import io.netty.handler.codec.http.multipart.FileUpload
         import com.github.fntz.omhs._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / long) ~> { (l: Long) =>
            "done"
         }
         """.stripMargin)
  }

  test("pass String") {
    compile(
      s"""
         import io.netty.handler.codec.http.multipart.FileUpload
         import com.github.fntz.omhs._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / string) ~> { (l: String) =>
            "done"
         }
         """.stripMargin)
  }

  test("pass UUID") {
    compile(
      s"""
         import io.netty.handler.codec.http.multipart.FileUpload
         import com.github.fntz.omhs._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._
         import java.util.UUID

         get("file" / uuid) ~> { (l: UUID) =>
            "done"
         }
         """.stripMargin)
  }

  test("pass Regex") {
    compile(
      s"""
         import io.netty.handler.codec.http.multipart.FileUpload
         import com.github.fntz.omhs._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / regex("".r)) ~> { (l: String) =>
            "done"
         }
         """.stripMargin)
  }

  test("pass Tail") {
    compile(
      s"""
         import io.netty.handler.codec.http.multipart.FileUpload
         import com.github.fntz.omhs._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" / *) ~> { (l: List[String]) =>
            "done"
         }
         """.stripMargin)
  }

  test("alternative syntax") {
    compile(
      s"""
         import com.github.fntz.omhs._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("test" / ("a" | "b" | "c")) ~> { (alt: String) =>
            "done"
         }
         """.stripMargin)
  }

  test("pass Header") {
    compile(
      s"""
         import io.netty.handler.codec.http.multipart.FileUpload
         import com.github.fntz.omhs._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" << header("User-Agent")) ~> { (l: String) =>
            "done"
         }
         """.stripMargin)
  }

  test("pass Cookie") {
    compile(
      s"""
         import io.netty.handler.codec.http.cookie.Cookie
         import com.github.fntz.omhs._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" << cookie("foo")) ~> { (l: Cookie) =>
            "done"
         }
         """.stripMargin)
  }

  test("pass Query") {
    compile(
      s"""
         import io.netty.handler.codec.http.cookie.Cookie
         import com.github.fntz.omhs._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         case class Search(q: String)
         implicit val qReader = new QueryReader[Search] {
           override def read(queries: Map[String, Iterable[String]]): Option[Search] = {
             queries.get("q").flatMap(_.headOption).map(Search)
           }
         }

         get("file" :? query[Search]) ~> { (q: Search) =>
            "done"
         }
         """.stripMargin)
  }

  test("pass Body") {
    compile(
      s"""
         import io.netty.handler.codec.http.multipart.FileUpload
         import com.github.fntz.omhs._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         case class Foo(id: Int)
         implicit val fooReader = new BodyReader[Foo] {
           override def read(str: String): Foo = Foo(1)
         }

         get("file" <<< body[Foo]) ~> { (l: Foo) =>
            "done"
         }
         """.stripMargin)
  }

  test("pass File") {
    compile(
      s"""
         import io.netty.handler.codec.http.multipart.FileUpload
         import com.github.fntz.omhs._
         import AsyncResult._
         import AsyncResult.Implicits._
         import RoutingDSL._

         get("file" <<< file) ~> { (l: List[FileUpload]) =>
            "done"
         }
         """.stripMargin)
  }

  test("success compilation#1") {
    compile(
      s"""
        import com.github.fntz.omhs._
        import AsyncResult._
        import AsyncResult.Implicits._
        import RoutingDSL._

        get(string) ~> { (x: String) => "done" }

      """.stripMargin)
  }

  test("success compilation#2") {
    compile(
      s"""
        import com.github.fntz.omhs._
        import AsyncResult._
        import AsyncResult.Implicits._
        import RoutingDSL._
        import java.util.UUID

        get(string / "test" / long / uuid) ~> { (x: String, l: Long, u: UUID) => "done" }

      """.stripMargin)
  }

  // based on https://stackoverflow.com/a/15323481/13751202
  def compile(code: String): Unit = {
    try {
      tryCompile(code)
      assert(cond = true)
    } catch {
      case _: Throwable =>
        assert(cond = false)
    }
  }

  def doesntCompile(code: String): Unit = {
    try {
      tryCompile(code)
      assert(cond = false)
    } catch {
      case _: ToolBoxError =>
        assert(cond = true)
    }
  }

  def doesntCompile(code: String, expectedErrorMessage: String): Unit = {
    try {
      tryCompile(code)
      assert(cond = false)
    } catch {
      case ex: ToolBoxError =>
        assert(ex.message.contains(expectedErrorMessage))
    }
  }

  def tryCompile(code: String): Any = {
    eval(code)
  }

  private def eval(code: String, compileOptions: String = "-cp target/classes -Ydelambdafy:inline"): Any = {
    val tb = mkToolbox(compileOptions)
    tb.eval(tb.parse(code))
  }

  private def mkToolbox(compileOptions: String = ""): ToolBox[_ <: scala.reflect.api.Universe] = {
    val m = scala.reflect.runtime.currentMirror
    m.mkToolBox(options = compileOptions)
  }

}
