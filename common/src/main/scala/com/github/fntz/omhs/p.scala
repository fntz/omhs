package com.github.fntz.omhs

import com.github.fntz.omhs.HttpMethod.HttpMethod

case class p(xs: Vector[Param], method: HttpMethod)

object p {
  def get(xs: Vector[Param]): p = p(xs, HttpMethod.GET)
  def post(xs: Vector[Param]): p = p(xs, HttpMethod.POST)
  def delete(xs: Vector[Param]): p = p(xs, HttpMethod.DELETE)
  def put(xs: Vector[Param]): p = p(xs, HttpMethod.PUT)
  def path(xs: Vector[Param]): p = p(xs, HttpMethod.PATH)
  def head(xs: Vector[Param]): p = p(xs, HttpMethod.HEAD)
}