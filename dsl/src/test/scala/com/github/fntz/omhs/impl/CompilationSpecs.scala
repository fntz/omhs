package com.github.fntz.omhs.impl

import org.specs2.mutable.Specification

class CompilationSpecs extends Specification with CompilationSpecsUtils {

  "dsl compilation" should {
    "doesn't compile: CurrentRequest should be the last argument" in {
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

    "doesn't compile: Stream should be the last argument" in {
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

    "doesn't compile: Stream or CurrentHttpRequest should be the last argument" in {
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

    "doesn't compile when parameters count is not the same as function arguments length" in {
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

    "doesn't if alternative in not params" in {
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

    "doesn't compile when parameters in incorrect sequence#1" in {
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

    "doesn't compile when parameters in incorrect sequence#2" in {
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

    "doesn't compile status without route" in {
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

    "doesn't compile setHeader without route" in {
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

    "doesn't compile setCookie without route" in {
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

    "doesn't compile contentType without route" in {
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

    "ignore arguments at all" in {
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

    "pass CurrentRequestParam to function" in {
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

    "pass Stream to function" in {
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

    "Stream or CurrentHttpRequest should be the last arguments#1" in {
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

    "Stream or CurrentHttpRequest should be the last arguments#2" in {
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

    "pass Long" in {
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

    "pass String" in {
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

    "pass UUID" in {
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

    "pass Regex" in {
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

    "pass Tail" in {
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

    "alternative check" in {
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

    "pass Header" in {
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

    "pass cookie" in {
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

    "pass query" in {
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

    "pass Body" in {
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

    "pass File" in {
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

    "success compilation" in {
      compile(
        s"""
          import com.github.fntz.omhs._
          import AsyncResult._
          import AsyncResult.Implicits._
          import RoutingDSL._

          get(string) ~> { (x: String) => "done" }

        """.stripMargin)
    }

    "success compilation #1" in {
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
  }

}
