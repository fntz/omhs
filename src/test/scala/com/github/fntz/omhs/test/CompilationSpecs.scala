package com.github.fntz.omhs.test

import org.specs2.mutable.Specification

class CompilationSpecs extends Specification with CompilationSpecsUtils {

  "dsl compilation" should {
    "doesnt compile: CurrentRequest should be the last argument" in {
      doesntCompile(
        s"""
           import io.netty.handler.codec.http.multipart.MixedFileUpload
           import com.github.fntz.omhs.methods.Methods._
           import com.github.fntz.omhs._
           import AsyncResult._
           import AsyncResult.Implicits._
           import ParamDSL._

           p.get("file" / StringParam / StringParam) ~> { (a: String, req: CurrentHttpRequest, b: String) =>
              "done"
           }
           """.stripMargin, s"CurrentHttpRequest must be the last argument in the function")
    }
    "doesnot compile when two body param in one rule" in {
      doesntCompile(
        s"""
           import io.netty.handler.codec.http.multipart.MixedFileUpload
           import com.github.fntz.omhs.methods.Methods._
           import com.github.fntz.omhs._
           import AsyncResult._
           import AsyncResult.Implicits._
           import ParamDSL._

           case class Foo(id: Int)
           implicit val fooReader = new BodyReader[Foo] {
             override def read(str: String): Foo = Foo(1)
           }

           p.post("file" / BodyParam[Foo] / BodyParam[Foo]) ~> { (a: Foo, b: Foo) =>
              "done"
           }
           """.stripMargin, s"BodyParam must be one per rule, given: 2")
    }
    "doesnt compile when two file param in one rule" in {
      doesntCompile(
        s"""
           import io.netty.handler.codec.http.multipart.MixedFileUpload
           import com.github.fntz.omhs.methods.Methods._
           import com.github.fntz.omhs._
           import AsyncResult._
           import AsyncResult.Implicits._
           import ParamDSL._

           p.post("file" / FileParam / FileParam) ~> { (a: List[MixedFileUpload], b: List[MixedFileUpload]) =>
              "done"
           }
           """.stripMargin, s"FileParam must be one per rule, given: 2")
    }

    "doesnt compile when body+file in one rule" in {
      doesntCompile(
        s"""
           import io.netty.handler.codec.http.multipart.MixedFileUpload
           import com.github.fntz.omhs.methods.Methods._
           import com.github.fntz.omhs._
           import AsyncResult._
           import AsyncResult.Implicits._
           import ParamDSL._

           case class Foo(id: Int)
           implicit val fooReader = new BodyReader[Foo] {
             override def read(str: String): Foo = Foo(1)
           }

           p.post("file" / FileParam / BodyParam[Foo]) ~> { (file: List[MixedFileUpload], foo: Foo) =>
              "done"
           }
           """.stripMargin, "You can not mix BodyParam with FileParam, choose one")
    }

    "doesnt compile when parameters count is not the same as function arguments length" in {
      doesntCompile(
        s"""
           import io.netty.handler.codec.http.multipart.MixedFileUpload
           import com.github.fntz.omhs.methods.Methods._
           import com.github.fntz.omhs._
           import AsyncResult._
           import AsyncResult.Implicits._
           import ParamDSL._

           p.post("file" / LongParam / StringParam) ~> { (s: String) =>
              "done"
           }
           """.stripMargin, "Args lengths are not the same")
    }

    "doesnt compile when parameters in incorrect sequence" in {
      doesntCompile(
        s"""
           import io.netty.handler.codec.http.multipart.MixedFileUpload
           import com.github.fntz.omhs.methods.Methods._
           import com.github.fntz.omhs._
           import AsyncResult._
           import AsyncResult.Implicits._
           import ParamDSL._

           p.post("file" / LongParam / StringParam) ~> { (s: String, l: Long) =>
              "done"
           }
           """.stripMargin, "Incorrect type for `s`, required: Long, given: String")
    }

    "ignore arguments at all" in {
      compile(
        s"""
           import io.netty.handler.codec.http.multipart.MixedFileUpload
           import com.github.fntz.omhs.methods.Methods._
           import com.github.fntz.omhs._
           import AsyncResult._
           import AsyncResult.Implicits._
           import ParamDSL._

           p.get("file" / StringParam) ~> { () =>
              "done"
           }
           """.stripMargin)
    }

    "pass CurrentRequestParam to function" in {
      compile(
        s"""
           import io.netty.handler.codec.http.multipart.MixedFileUpload
           import com.github.fntz.omhs.methods.Methods._
           import com.github.fntz.omhs._
           import AsyncResult._
           import AsyncResult.Implicits._
           import ParamDSL._

           p.get("file" / StringParam) ~> { (a: String, req: CurrentHttpRequest) =>
              "done"
           }
           """.stripMargin)
    }

    "pass Long" in {
      compile(
        s"""
           import io.netty.handler.codec.http.multipart.MixedFileUpload
           import com.github.fntz.omhs.methods.Methods._
           import com.github.fntz.omhs._
           import AsyncResult._
           import AsyncResult.Implicits._
           import ParamDSL._

           p.get("file" / LongParam) ~> { (l: Long) =>
              "done"
           }
           """.stripMargin)
    }

    "pass String" in {
      compile(
        s"""
           import io.netty.handler.codec.http.multipart.MixedFileUpload
           import com.github.fntz.omhs.methods.Methods._
           import com.github.fntz.omhs._
           import AsyncResult._
           import AsyncResult.Implicits._
           import ParamDSL._

           p.get("file" / StringParam) ~> { (l: String) =>
              "done"
           }
           """.stripMargin)
    }

    "pass UUID" in {
      compile(
        s"""
           import io.netty.handler.codec.http.multipart.MixedFileUpload
           import com.github.fntz.omhs.methods.Methods._
           import com.github.fntz.omhs._
           import AsyncResult._
           import AsyncResult.Implicits._
           import ParamDSL._
           import java.util.UUID

           p.get("file" / UUIDParam) ~> { (l: UUID) =>
              "done"
           }
           """.stripMargin)
    }

    "pass Regex" in {
      compile(
        s"""
           import io.netty.handler.codec.http.multipart.MixedFileUpload
           import com.github.fntz.omhs.methods.Methods._
           import com.github.fntz.omhs._
           import AsyncResult._
           import AsyncResult.Implicits._
           import ParamDSL._

           p.get("file" / RegexParam("".r)) ~> { (l: String) =>
              "done"
           }
           """.stripMargin)
    }

    "pass Tail" in {
      compile(
        s"""
           import io.netty.handler.codec.http.multipart.MixedFileUpload
           import com.github.fntz.omhs.methods.Methods._
           import com.github.fntz.omhs._
           import AsyncResult._
           import AsyncResult.Implicits._
           import ParamDSL._

           p.get("file" / *) ~> { (l: List[String]) =>
              "done"
           }
           """.stripMargin)
    }

    "pass Header" in {
      compile(
        s"""
           import io.netty.handler.codec.http.multipart.MixedFileUpload
           import com.github.fntz.omhs.methods.Methods._
           import com.github.fntz.omhs._
           import AsyncResult._
           import AsyncResult.Implicits._
           import ParamDSL._

           p.get("file" / HeaderParam("User-Agent")) ~> { (l: String) =>
              "done"
           }
           """.stripMargin)
    }

    "pass Body" in {
      compile(
        s"""
           import io.netty.handler.codec.http.multipart.MixedFileUpload
           import com.github.fntz.omhs.methods.Methods._
           import com.github.fntz.omhs._
           import AsyncResult._
           import AsyncResult.Implicits._
           import ParamDSL._

           case class Foo(id: Int)
           implicit val fooReader = new BodyReader[Foo] {
             override def read(str: String): Foo = Foo(1)
           }

           p.get("file" / BodyParam[Foo]) ~> { (l: Foo) =>
              "done"
           }
           """.stripMargin)
    }

    "pass File" in {
      compile(
        s"""
           import io.netty.handler.codec.http.multipart.MixedFileUpload
           import com.github.fntz.omhs.methods.Methods._
           import com.github.fntz.omhs._
           import AsyncResult._
           import AsyncResult.Implicits._
           import ParamDSL._

           p.get("file" / FileParam) ~> { (l: List[MixedFileUpload]) =>
              "done"
           }
           """.stripMargin)
    }

    "success compilation" in {
      compile(
        s"""
          import com.github.fntz.omhs.methods.Methods._
          import com.github.fntz.omhs._
          import AsyncResult._
          import AsyncResult.Implicits._

          p.get(StringParam) ~> { (x: String) => "done" }

        """.stripMargin)
    }
  }

}
