package com.github.fntz.omhs.internal

import com.github.fntz.omhs.CurrentHttpRequest
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.codec.http.multipart.{FileUpload, MixedFileUpload}

import java.util.UUID

sealed trait ParamDef[T] {
  def value: T
  // property does not used for call user-defined function
  val skip: Boolean = false
  // I use it for sorting:
  // user defined params as: string << header <<< body
  // after decoding we have: srringDef, bodyDef, headerDef
  // but correct function is string + header + body
  // we need to update order of the params according to the property
  val sortProp: Int
}
object ParamDef {
  val empty = 0
  val long = 1
  val string = 2
  val uuid = 3
  val regex = 4
  val tail = 5
  val body = 6
  val header = 7
  val request = 8
  val cookie = 9
  val file = 10
  val query = 11
}
sealed trait PathParamDef[T] extends ParamDef[T]
case class EmptyDef(value: String) extends PathParamDef[String] {
  override val skip: Boolean = true
  override val sortProp: Int = ParamDef.empty
}

case class LongDef(value: Long) extends PathParamDef[Long] {
  override val sortProp: Int = ParamDef.long
}

case class StringDef(value: String) extends PathParamDef[String] {
  override val sortProp: Int = ParamDef.string
}

case class UUIDDef(value: UUID) extends PathParamDef[UUID] {
  override val sortProp: Int = ParamDef.uuid
}

case class RegexDef(value: String) extends PathParamDef[String] {
  override val sortProp: Int = ParamDef.regex
}

case class TailDef(values: List[String]) extends PathParamDef[List[String]] {
  override val value: List[String] = values
  override val sortProp: Int = ParamDef.tail
}

case class BodyDef[T](value: T) extends ParamDef[T] {
  override val sortProp: Int = ParamDef.body
}

case class HeaderDef(value: String) extends ParamDef[String] {
  override val sortProp: Int = ParamDef.header
}

case class CurrentHttpRequestDef(value: CurrentHttpRequest) extends ParamDef[CurrentHttpRequest] {
  override val sortProp: Int = ParamDef.request
}

case class FileDef(value: List[FileUpload]) extends ParamDef[List[FileUpload]] {
  override val sortProp: Int = ParamDef.file

  override def toString: String = s"files: ${value.map(_.getFilename).mkString(", ")}"
}

case class CookieDef(value: Cookie) extends ParamDef[Cookie] {
  override val sortProp: Int = ParamDef.cookie
}

case class QueryDef[T](value: T) extends ParamDef[T] {
  override val sortProp: Int = ParamDef.query
}

