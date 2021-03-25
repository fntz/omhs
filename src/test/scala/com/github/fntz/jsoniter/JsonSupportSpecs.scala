package com.github.fntz.jsoniter

import com.github.fntz.omhs.jsoniter.JsonSupport
import com.github.fntz.omhs.{BodyReader, BodyWriter}
import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import org.specs2.mutable.Specification

import java.nio.charset.StandardCharsets.UTF_8

object JsonSupportSpecs extends Specification {

  case class Book(id: Int, name: String)
  implicit val codec: JsonValueCodec[Book] = JsonCodecMaker.make

  implicit val writer: BodyWriter[Book] = JsonSupport.writer[Book]
  implicit val reader: BodyReader[Book] = JsonSupport.reader[Book]

  "json" should {
    "serialize/deserialize" in {
      val b = Book(1, "foo")
      val writeResult = writer.write(b)
      writeResult.content ==== writeToArray(b)

      val readResult = reader.read(new String(writeResult.content, UTF_8))
      readResult === b
    }
  }

}
