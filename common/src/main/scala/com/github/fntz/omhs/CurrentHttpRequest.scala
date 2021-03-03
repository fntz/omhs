package com.github.fntz.omhs

import io.netty.handler.codec.http.{HttpHeaders, HttpMethod}

abstract class CurrentHttpRequest[T] {
  def uri: String
  def rawBody: String
  def parsedBody[T]: T
  def headers: HttpHeaders
  def query: String // Map[String, String]
  def method: HttpMethod
}
