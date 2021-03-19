package com.github.fntz.omhs

import com.github.fntz.omhs.internal._
import com.github.fntz.omhs.impl.RoutingImpl
import io.netty.handler.codec.http.HttpMethod

import scala.language.experimental.macros
import scala.util.matching.Regex

object RoutingDSL {

  /*********** http methods ******************/

  def get(x: String): Rule = Rule(HttpMethod.GET).path(x)
  def get(x: PathParam): Rule = Rule(HttpMethod.GET).path(x)
  def get(x: LikeRule): Rule = x.rule.withMethod(HttpMethod.GET)
  def get(x: Rule): Rule = x.withMethod(HttpMethod.GET)

  def post(x: String): Rule = Rule(HttpMethod.POST).path(x)
  def post(x: PathParam): Rule = Rule(HttpMethod.POST).path(x)
  def post(x: LikeRule): Rule = x.rule.withMethod(HttpMethod.POST)
  def post(x: Rule): Rule = x.withMethod(HttpMethod.POST)

  def delete(x: String): Rule = Rule(HttpMethod.DELETE).path(x)
  def delete(x: PathParam): Rule = Rule(HttpMethod.DELETE).path(x)
  def delete(x: LikeRule): Rule = x.rule.withMethod(HttpMethod.DELETE)
  def delete(x: Rule): Rule = x.withMethod(HttpMethod.DELETE)

  def put(x: String): Rule = Rule(HttpMethod.PUT).path(x)
  def put(x: PathParam): Rule = Rule(HttpMethod.PUT).path(x)
  def put(x: LikeRule): Rule = x.rule.withMethod(HttpMethod.PUT)
  def put(x: Rule): Rule = x.withMethod(HttpMethod.PUT)

  def patch(x: String): Rule = Rule(HttpMethod.PATCH).path(x)
  def patch(x: PathParam): Rule = Rule(HttpMethod.PATCH).path(x)
  def patch(x: LikeRule): Rule = x.rule.withMethod(HttpMethod.PATCH)
  def patch(x: Rule): Rule = x.withMethod(HttpMethod.PATCH)

  def head(x: String): Rule = Rule(HttpMethod.HEAD).path(x)
  def head(x: PathParam): Rule = Rule(HttpMethod.HEAD).path(x)
  def head(x: LikeRule): Rule = x.rule.withMethod(HttpMethod.HEAD)
  def head(x: Rule): Rule = x.withMethod(HttpMethod.HEAD)

  def connect(x: String): Rule = Rule(HttpMethod.CONNECT).path(x)
  def connect(x: PathParam): Rule = Rule(HttpMethod.CONNECT).path(x)
  def connect(x: LikeRule): Rule = x.rule.withMethod(HttpMethod.CONNECT)
  def connect(x: Rule): Rule = x.withMethod(HttpMethod.CONNECT)

  def options(x: String): Rule = Rule(HttpMethod.OPTIONS).path(x)
  def options(x: PathParam): Rule = Rule(HttpMethod.OPTIONS).path(x)
  def options(x: LikeRule): Rule = x.rule.withMethod(HttpMethod.OPTIONS)
  def options(x: Rule): Rule = x.withMethod(HttpMethod.OPTIONS)

  def trace(x: String): Rule = Rule(HttpMethod.TRACE).path(x)
  def trace(x: PathParam): Rule = Rule(HttpMethod.TRACE).path(x)
  def trace(x: LikeRule): Rule = x.rule.withMethod(HttpMethod.TRACE)
  def trace(x: Rule): Rule = x.withMethod(HttpMethod.TRACE)

  /********************** dsl *************************/

  def string(name: String, description: Option[String]): StringParam = StringParam(name, description)
  def string(name: String): StringParam = string(name, None)
  def string: StringParam = string(":string", None)

  def long(name: String, description: Option[String]): LongParam = LongParam(name, description)
  def long(name: String): LongParam = long(name, None)
  def long: LongParam = long(":long", None)

  def uuid(name: String, description: Option[String]): UUIDParam = UUIDParam(name, description)
  def uuid(name: String): UUIDParam = uuid(name, None)
  def uuid: UUIDParam = uuid(":uuid", None)

  def regex(re: Regex, name: String, description: Option[String]): RegexParam =
    RegexParam(re, name, description)
  def regex(re: Regex, name: String): RegexParam = RegexParam(re, name, None)
  def regex(re: Regex): RegexParam = RegexParam(re, ":regex", None)

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

  implicit class StringExt(val s: String) extends AnyVal {
    def /(x: PathLikeParam): PathLikeParam =  {
      val tmp = new Rule().path(s)
      x.rule.currentParams.foreach { p => tmp.path(p) }
      PathLikeParam(tmp)
    }
    def /(x: TailParam.type): NoPathMoreParam =
      NoPathMoreParam(new Rule().path(s).path(x))
    def /(x: PathParamNoTail): PathLikeParam =
      PathLikeParam(new Rule().path(s).path(x))
    def |(x: String): PathLikeParam =
      PathLikeParam(new Rule().path(AlternativeParam(s :: x :: Nil)))
    def /(x: String): PathLikeParam =
      PathLikeParam(new Rule().path(s).path(x))
    def :?[T](q: QueryParam[T]): QueryLikeParam =
      QueryLikeParam(new Rule().path(s).query(q.reader))
    def <<[T](h: HeaderParam): HeaderOrCookieLikeParam =
      HeaderOrCookieLikeParam(new Rule().path(s).header(h))
    def <<[T](c: CookieParam): HeaderOrCookieLikeParam =
      HeaderOrCookieLikeParam(new Rule().path(s).cookie(c))
    def <<<[T](b: BodyParam[T]): FileOrBodyLikeParam =
      FileOrBodyLikeParam(new Rule().path(s).body(b.reader))
    def <<<(f: FileParam): FileOrBodyLikeParam =
      FileOrBodyLikeParam(new Rule().path(s).withFiles(f))
  }

  implicit class PathParamExt(val l: PathParam) extends AnyVal {
    def /(x: String): PathLikeParam =
      PathLikeParam(new Rule().path(l).path(x))
    def /(x: TailParam.type): NoPathMoreParam =
      NoPathMoreParam(new Rule().path(l).path(x))
    def /(x: PathParam): PathLikeParam =
      PathLikeParam(new Rule().path(l).path(x))
    def /(x: PathLikeParam): PathLikeParam = {
      val tmp = new Rule().path(l)
      x.rule.currentParams.foreach { p => tmp.path(p) }
      PathLikeParam(tmp)
    }
    def :?[T](q: QueryParam[T]): QueryLikeParam =
      QueryLikeParam(new Rule().path(l).query(q.reader))
    def <<[T](h: HeaderParam): HeaderOrCookieLikeParam =
      HeaderOrCookieLikeParam(new Rule().path(l).header(h))
    def <<[T](c: CookieParam): HeaderOrCookieLikeParam =
      HeaderOrCookieLikeParam(new Rule().path(l).cookie(c))
    def <<<[T](b: BodyParam[T]): FileOrBodyLikeParam =
      FileOrBodyLikeParam(new Rule().path(l).body(b.reader))
    def <<<(f: FileParam): FileOrBodyLikeParam =
      FileOrBodyLikeParam(new Rule().path(l).withFiles(f))
  }

  implicit class ExecutableRuleExtensions(val rule: Rule) extends AnyVal {
    implicit def ~>[R](f: () => R): ExecutableRule =
      macro RoutingImpl.run0[R]
    implicit def ~>[T1, R](f: (T1) => R): ExecutableRule =
      macro RoutingImpl.run1[T1, R]
    implicit def ~>[T1, T2, R](f: (T1, T2) => R): ExecutableRule =
      macro RoutingImpl.run2[T1, T2, R]
    implicit def ~>[T1, T2, T3, R](f: (T1, T2, T3) => R): ExecutableRule =
      macro RoutingImpl.run3[T1, T2, T3, R]
    implicit def ~>[T1, T2, T3, T4, R](f: (T1, T2, T3, T4) => R): ExecutableRule =
      macro RoutingImpl.run4[T1, T2, T3, T4, R]
    implicit def ~>[T1, T2, T3, T4, T5, R](f: (T1, T2, T3, T4, T5) => R): ExecutableRule =
      macro RoutingImpl.run5[T1, T2, T3, T4, T5, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, R](f: (T1, T2, T3, T4, T5, T6) => R): ExecutableRule =
      macro RoutingImpl.run6[T1, T2, T3, T4, T5, T6, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, T7, R](f: (T1, T2, T3, T4, T5, T6, T7) => R): ExecutableRule =
      macro RoutingImpl.run7[T1, T2, T3, T4, T5, T6, T7, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, T7, T8, R](f: (T1, T2, T3, T4, T5, T6, T7, T8) => R): ExecutableRule =
      macro RoutingImpl.run8[T1, T2, T3, T4, T5, T6, T7, T8, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9) => R): ExecutableRule =
      macro RoutingImpl.run9[T1, T2, T3, T4, T5, T6, T7, T8, T9, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => R): ExecutableRule =
      macro RoutingImpl.run10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) => R): ExecutableRule =
      macro RoutingImpl.run11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) => R): ExecutableRule =
      macro RoutingImpl.run12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) => R): ExecutableRule =
      macro RoutingImpl.run13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) => R): ExecutableRule =
      macro RoutingImpl.run14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) => R): ExecutableRule =
      macro RoutingImpl.run15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) => R): ExecutableRule =
      macro RoutingImpl.run16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) => R): ExecutableRule =
      macro RoutingImpl.run17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18) => R): ExecutableRule =
      macro RoutingImpl.run18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) => R): ExecutableRule =
      macro RoutingImpl.run19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) => R): ExecutableRule =
      macro RoutingImpl.run20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21) => R): ExecutableRule =
      macro RoutingImpl.run21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R]
    implicit def ~>[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R](f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22) => R): ExecutableRule =
      macro RoutingImpl.run22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R]

  }
}
