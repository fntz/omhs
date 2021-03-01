package com.github.fntz.omhs

trait BodyReader[T] {
  def read(str: String): T
}
