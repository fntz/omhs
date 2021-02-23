package com.github.fntz.omhs

case class p(xs: Vector[Param])

object p {
  def get(xs: Vector[Param]): p = p(xs)
}