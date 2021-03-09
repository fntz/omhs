package com.github.fntz.omhs

import com.github.fntz.omhs.internal._
import io.netty.handler.codec.http.HttpMethod

import scala.collection.mutable.{ArrayBuffer => AB}

case class Rule(method: HttpMethod) {

  private val paths: AB[PathParam] = new AB[PathParam]()
  private val headers: AB[HeaderParam] = new AB[HeaderParam]()
  private val cookies: AB[CookieParam] = new AB[CookieParam]()

  def currentUrl: String = {
    val tmp = params.map {
      case HardCodedParam(v) => v
      case TailParam => "*"
      case p: PathParam => s"{${p.name}}"
    }.mkString("/")
    if (tmp.startsWith("/")) {
      tmp
    } else {
      s"/$tmp"
    }
  }

  private var reader: BodyReader[_] = _

  private var queryReader: QueryReader[_] = _

  private var fileParam: FileParam = _

  private var isBodyNeeded = false

  private var isQueryNeeded = false

  private var isFileNeeded = false

  private var isCurrentRequestNeeded: Boolean = false

  def params: Vector[PathParam] = paths.toVector

  def currentHeaders: Vector[HeaderParam] = headers.toVector

  def currentFileParam: FileParam = fileParam

  def isParseBody: Boolean = isBodyNeeded

  def isFetchQuery: Boolean = isQueryNeeded

  def isRunWithRequest: Boolean = isCurrentRequestNeeded

  def isFilePassed: Boolean = isFileNeeded

  def currentReader: BodyReader[_] = reader

  def currentQueryReader: QueryReader[_] = queryReader

  def currentCookies: Vector[CookieParam] = cookies.toVector

  def withRequest(): Rule = {
    isCurrentRequestNeeded = true
    this
  }

  def withFiles(fileParam: FileParam): Rule = {
    this.fileParam = fileParam
    isFileNeeded = true
    this
  }

  // : Reader
  def body[T]()(implicit reader: BodyReader[T]): Rule = {
    this.reader = reader
    this.isBodyNeeded = true
    this
  }

  def query[T]()(implicit queryReader: QueryReader[T]): Rule = {
    this.queryReader = queryReader
    this.isQueryNeeded = true
    this
  }

  def path(x: String): Rule = {
    paths += HardCodedParam(x)
    this
  }

  def path(x: PathParam): Rule = {
    paths += x
    this
  }

  def cookie(cookie: CookieParam): Rule = {
    cookies += cookie
    this
  }

  def header(header: HeaderParam): Rule = {
    headers += header
    this
  }

  override def toString: String = {
    val tmpHeaders = s"\n headers: ${headers.mkString(", ")}"
    val tmpReq = s"\n useRequest: $isRunWithRequest"
    val tmpIsBodyNeeded = s"\n isBodyNeeded: $isBodyNeeded"
    val tmpMethod = s"\n ---- $method"
    val tmpPath = s"\n path: ${paths.mkString("/")}"
    s"$tmpMethod $tmpPath $tmpHeaders $tmpReq $tmpIsBodyNeeded"
  }

}

