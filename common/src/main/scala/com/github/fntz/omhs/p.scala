package com.github.fntz.omhs

import com.github.fntz.omhs.internal.{HardCodedParam, Param}
import io.netty.handler.codec.http.HttpMethod

case class p(xs: Vector[Param], method: HttpMethod)

object p {
  def get(x: Param): p = p(Vector(x), HttpMethod.GET)
  def get(x: String): p = p(Vector(HardCodedParam(x)), HttpMethod.GET)
  def get(xs: Vector[Param]): p = p(xs, HttpMethod.GET)
  def post(xs: Vector[Param]): p = p(xs, HttpMethod.POST)
  def delete(xs: Vector[Param]): p = p(xs, HttpMethod.DELETE)
  def put(xs: Vector[Param]): p = p(xs, HttpMethod.PUT)
  def patch(xs: Vector[Param]): p = p(xs, HttpMethod.PATCH)
  def head(xs: Vector[Param]): p = p(xs, HttpMethod.HEAD)
}