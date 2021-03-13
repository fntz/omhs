package com.github.fntz.omhs.playjson

import com.github.fntz.omhs.{BodyReader, BodyWriter, CommonResponse}
import io.circe._
import io.circe.parser._
import io.circe.syntax._

object JsonSupport {

  def writer[T]()(implicit encoder: Encoder[T]) : BodyWriter[T] = new BodyWriter[T] {
    override def write(w: T): CommonResponse = {
      CommonResponse.json(w.asJson.noSpaces)
    }
  }

  def reader[T]()(implicit decoder: Decoder[T]): BodyReader[T] = new BodyReader[T] {
    override def read(str: String): T = {
      (for {
        json <- parse(str)
        entity <- json.as[T]
      } yield entity).fold(throw _, identity)
    }
  }

}
