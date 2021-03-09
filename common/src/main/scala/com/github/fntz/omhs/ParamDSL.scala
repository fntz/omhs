package com.github.fntz.omhs

import com.github.fntz.omhs.internal._
import io.netty.handler.codec.http.HttpMethod

import scala.util.matching.Regex

case class p(xs: Vector[Param], method: HttpMethod)

object ParamDSL {

  def get(x: Param): p = p(Vector(x), HttpMethod.GET)
  def get(x: String): p = p(Vector(HardCodedParam(x)), HttpMethod.GET)
  def get(xs: Vector[Param]): p = p(xs, HttpMethod.GET)
  def post(xs: Vector[Param]): p = p(xs, HttpMethod.POST)
  def delete(xs: Vector[Param]): p = p(xs, HttpMethod.DELETE)
  def put(xs: Vector[Param]): p = p(xs, HttpMethod.PUT)
  def patch(xs: Vector[Param]): p = p(xs, HttpMethod.PATCH)
  def head(xs: Vector[Param]): p = p(xs, HttpMethod.HEAD)

  def string(name: String, description: Option[String]): StringParam = StringParam(name, description)
  def string(name: String): StringParam = string(name, None)
  def string: StringParam = string("string-param", None)

  def long(name: String, description: Option[String]): LongParam = LongParam(name, description)
  def long(name: String): LongParam = long(name, None)
  def long: LongParam = long("long-param", None)

  def uuid(name: String, description: Option[String]): UUIDParam = UUIDParam(name, description)
  def uuid(name: String): UUIDParam = uuid(name, None)
  def uuid: UUIDParam = uuid("uuid-param", None)

  def regex(re: Regex, name: String, description: Option[String]): RegexParam =
    RegexParam(re, name, description)
  def regex(re: Regex, name: String): RegexParam = RegexParam(re, name, None)
  def regex(re: Regex): RegexParam = RegexParam(re, "regex-param", None)

  def header(headerName: String): HeaderParam =
    header(headerName, None)
  def header(headerName: String, description: Option[String]): HeaderParam =
    HeaderParam(headerName, description)

  def cookie(headerName: String): CookieParam =
    cookie(headerName, None)
  def cookie(headerName: String, description: Option[String]): CookieParam =
    CookieParam(headerName, description)

  def query[T](implicit qr: QueryReader[T]): QueryParam[T] =
    QueryParam[T]()(qr)

  def file: FileParam = FileParam("file", None)
  def file(name: String): FileParam = FileParam(name, None)
  def file(name: String, description: String): FileParam =
    FileParam(name, Some(description))

  def body[T](implicit r: BodyReader[T]): BodyParam[T] =
    BodyParam[T]()(r)

  def * : TailParam.type = TailParam

  implicit class ParamImplicits(val param: Param) extends AnyVal {
    def /(other: Param): Vector[Param] = Vector(param, other)
    def /(other: String): Vector[Param] = Vector(param, HardCodedParam(other))
  }
  implicit class StringToParamImplicits(val str: String) extends AnyVal {
    def /[T <: Param](other: T): Vector[Param] =
      Vector(HardCodedParam(str), other)
    def /(other: String): Vector[Param] =
      Vector(HardCodedParam(str), HardCodedParam(other))
  }
  implicit class VectorParamsImplicits(val xs: Vector[Param]) extends AnyVal {
    def /(o: Param): Vector[Param] = xs :+ o
    def /(o: String): Vector[Param] = xs :+ HardCodedParam(o)
  }
}

