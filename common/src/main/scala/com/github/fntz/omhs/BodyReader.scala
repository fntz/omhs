package com.github.fntz.omhs

trait BodyReader[T] {
  def read(str: String): T
}

trait BodyWriter[W] {
  def write(w: W): CommonResponse
}