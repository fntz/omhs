package com.github.fntz.omhs

import java.util.UUID

sealed trait ParamDef[T] {
  def value: T
  // do not used for call user-defined function
  val skip: Boolean = false
}
sealed trait PathParamDef[T] extends ParamDef[T]
case class EmptyDef(value: String) extends PathParamDef[String] {
  override val skip: Boolean = true
}
case class LongDef(value: Long) extends PathParamDef[Long]
case class StringDef(value: String) extends PathParamDef[String]
case class UUIDDef(value: UUID) extends PathParamDef[UUID]
case class RegexDef(value: String) extends PathParamDef[String]
case class TailDef(values: List[String]) extends PathParamDef[List[String]] {
  override val value: List[String] = values
}
case class BodyDef[T](value: T) extends ParamDef[T]
case class HeaderDef(value: String) extends ParamDef[String]