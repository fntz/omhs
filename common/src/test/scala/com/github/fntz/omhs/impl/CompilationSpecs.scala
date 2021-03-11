package com.github.fntz.omhs.impl

import org.specs2.mutable.Specification

class CompilationSpecs extends Specification with CompilationSpecsUtils {

  "dsl compilation" should {
    "doesnt compile: CurrentRequest should be the last argument" in {
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

    "doesnt compile when parameters count is not the same as function arguments length" in {
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

    "doesnt compile when parameters in incorrect sequence" in {
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
