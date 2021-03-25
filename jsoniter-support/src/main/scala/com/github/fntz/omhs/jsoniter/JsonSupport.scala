package com.github.fntz.omhs.jsoniter

import java.nio.charset.StandardCharsets.UTF_8
import com.github.fntz.omhs.{BodyReader, BodyWriter, CommonResponse}
import com.github.plokhotnyuk.jsoniter_scala.core._

object JsonSupport {

  def writer[T](implicit codec: JsonValueCodec[T]): BodyWriter[T] = new BodyWriter[T] {
    override def write(w: T): CommonResponse = {
      CommonResponse(
        status = 200,
        contentType = "application/json",
        content = writeToArray(w)
      )
    }
  }

  def reader[T](implicit codec: JsonValueCodec[T]): BodyReader[T] = new BodyReader[T] {
    override def read(str: String): T = {
      readFromArray(str.getBytes(UTF_8))
    }
  }

}
