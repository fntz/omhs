package com.github.fntz.omhs

import io.netty.handler.ssl.ApplicationProtocolNames

sealed trait WorkMode {
  val weight: Int
  val protocol: String
  def isH2: Boolean = false
}
object WorkModes {
  case object Http11 extends WorkMode {
    override val weight: Int = 1
    override val protocol: String = ApplicationProtocolNames.HTTP_1_1
  }
  case object Http2 extends WorkMode {
    override val weight: Int = 2
    override val protocol: String = ApplicationProtocolNames.HTTP_2
    override def isH2: Boolean = true
  }
}
