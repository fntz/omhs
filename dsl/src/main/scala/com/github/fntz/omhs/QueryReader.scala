package com.github.fntz.omhs

trait QueryReader[T] {
  def read(queries: Map[String, Iterable[String]]): Option[T]
}
