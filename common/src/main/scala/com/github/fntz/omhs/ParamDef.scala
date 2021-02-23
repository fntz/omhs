package com.github.fntz.omhs

import java.util.UUID

sealed trait ParamDef[T] {
  def value: T
}
case class EmptyDef(value: String) extends ParamDef[String]
case class LongDef(value: Long) extends ParamDef[Long]
case class StringDef(value: String) extends ParamDef[String]
case class UUIDDef(value: UUID) extends ParamDef[UUID]
case class RegexDef(value: String) extends ParamDef[String]
case class TailDef(values: Vector[String]) extends ParamDef[Vector[String]] {
  override val value: Vector[String] = values
}

