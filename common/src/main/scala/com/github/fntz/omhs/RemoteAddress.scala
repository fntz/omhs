package com.github.fntz.omhs

sealed trait RemoteAddress
case class Address(host: String) extends RemoteAddress
case class ForwardProxies(hosts: List[String]) extends RemoteAddress
case object Unknown extends RemoteAddress
