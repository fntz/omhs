package com.github.fntz.omhs.playjson

import com.github.fntz.omhs.{BodyReader, BodyWriter, CommonResponse}
import play.api.libs.json.{Json, Reads, Writes}

object JsonSupport {

  def writer[T]()(implicit writes: Writes[T]) : BodyWriter[T] = new BodyWriter[T] {
    override def write(w: T): CommonResponse = {
      CommonResponse.json(Json.toJson(w)(writes).toString())
    }
  }

  def reader[T]()(implicit reads: Reads[T]): BodyReader[T] = new BodyReader[T] {
    override def read(str: String): T = {
      Json.parse(str).as[T](reads)
    }
  }

}
