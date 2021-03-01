package com.github.fntz.omhs

object HttpMethod extends Enumeration {
  type HttpMethod = Value
  val GET, POST, DELETE, PUT, HEAD, PATH = Value
}
