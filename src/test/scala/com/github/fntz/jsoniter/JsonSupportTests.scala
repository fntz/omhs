package com.github.fntz.jsoniter

import com.github.fntz.omhs.jsoniter.JsonSupport
import com.github.fntz.omhs.{BodyReader, BodyWriter}
import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import munit.FunSuite

import java.nio.charset.StandardCharsets.UTF_8

class JsonSupportTests extends FunSuite {

  case class Book(id: Int, name: String)
  implicit val codec: JsonValueCodec[Book] = JsonCodecMaker.make

  implicit val writer: BodyWriter[Book] = JsonSupport.writer[Book]
  implicit val reader: BodyReader[Book] = JsonSupport.reader[Book]

  test("json write/read with `jsoniter`") {
    val b = Book(1, "foo")
    val writeResult = writer.write(b)
    assertEquals(new String(writeResult.content), new String(writeToArray(b)))

    val readResult = reader.read(new String(writeResult.content, UTF_8))
    assertEquals(readResult, b)
  }

}
