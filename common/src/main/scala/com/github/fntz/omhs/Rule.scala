package com.github.fntz.omhs


import io.netty.handler.codec.http.HttpMethod

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer => AB}

class Rule(val method: HttpMethod) {
  private val paths: AB[PathParam] = new AB[PathParam]()
  private val headers: AB[String] = new AB[String]()

  private var reader: BodyReader[_] = null // todo None instead

  private var isBodyNeeded = false

  private var isFileNeeded = false

  private var isCurrentRequestNeeded: Boolean = false

  def params: Vector[PathParam] = paths.toVector

  def currentHeaders: Vector[String] = headers.toVector

  def isParseBody: Boolean = isBodyNeeded

  def isRunWithRequest: Boolean = isCurrentRequestNeeded

  def isFilePassed: Boolean = isFileNeeded

  def currentReader: BodyReader[_] = reader

  def withRequest(): Rule = {
    isCurrentRequestNeeded = true
    this
  }

  def withFiles(): Rule = {
    isFileNeeded = true
    this
  }

  // : Reader
  def body[T]()(implicit reader: BodyReader[T]): Rule = {
    this.reader = reader
    this.isBodyNeeded = true
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

  def cookie(x: String): Rule = {
    this
  }

  def header(header: String): Rule = {
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

