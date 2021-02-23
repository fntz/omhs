package com.github.fntz.omhs

object HttpMethod extends Enumeration {
  type HttpMethod = Value
  val Get, Post, Delete, Put, Head, Patch = Value
}
