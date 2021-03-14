package com.github.fntz.omhs

trait BodyWriter[W] {
  def write(w: W): CommonResponse
}
