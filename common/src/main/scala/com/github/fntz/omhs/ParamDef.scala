package com.github.fntz.omhs

import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.codec.http.multipart.MixedFileUpload

import java.util.UUID

sealed trait ParamDef[T] {
  def value: T
  // do not used for call user-defined function
  val skip: Boolean = false
  // I use it for sorting:
  // user defined params as: header / long / string
  // after decoding we have: longDef, strDef, headerDef
  // but correct function is header + long + string
  // we need to sort params according to this property
  val sortProp: Int
}
sealed trait PathParamDef[T] extends ParamDef[T]
case class EmptyDef(value: String) extends PathParamDef[String] {
  override val skip: Boolean = true
  override val sortProp: Int = EmptyDef.sortProp
}
object EmptyDef {
  val sortProp = 0
}
case class LongDef(value: Long) extends PathParamDef[Long] {
  override val sortProp: Int = LongDef.sortProp
}
object LongDef {
  val sortProp = 1
}
case class StringDef(value: String) extends PathParamDef[String] {
  override val sortProp: Int = StringDef.sortProp
}
object StringDef {
  val sortProp = 2
}
case class UUIDDef(value: UUID) extends PathParamDef[UUID] {
  override val sortProp: Int = UUIDDef.sortProp
}
object UUIDDef {
  val sortProp = 3
}
case class RegexDef(value: String) extends PathParamDef[String] {
  override val sortProp: Int = RegexDef.sortProp
}
object RegexDef {
  val sortProp = 4
}
case class TailDef(values: List[String]) extends PathParamDef[List[String]] {
  override val value: List[String] = values
  override val sortProp: Int = TailDef.sortProp
}
object TailDef {
  val sortProp = 5
}
case class BodyDef[T](value: T) extends ParamDef[T] {
  override val sortProp: Int = BodyDef.sortProp
}
object BodyDef {
  val sortProp = 6
}
case class HeaderDef(value: String) extends ParamDef[String] {
  override val sortProp: Int = HeaderDef.sortProp
}
object HeaderDef {
  val sortProp = 7
}
case class CurrentHttpRequestDef(value: CurrentHttpRequest) extends ParamDef[CurrentHttpRequest] {
  override val sortProp: Int = CurrentHttpRequestDef.sortProp
}
object CurrentHttpRequestDef {
  val sortProp = 8
}
case class FileDef(value: List[MixedFileUpload]) extends ParamDef[List[MixedFileUpload]] {
  override val sortProp: Int = FileDef.sortProp

  override def toString: String = s"files: ${value.map(_.getFilename).mkString(", ")}"
}
object FileDef {
  val sortProp = 9
}
case class CookieDef(value: Cookie) extends ParamDef[Cookie] {
  override val sortProp: Int = CookieDef.sortProp
}
object CookieDef {
  val sortProp = 10
}