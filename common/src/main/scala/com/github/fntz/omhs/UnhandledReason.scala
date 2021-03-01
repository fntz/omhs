package com.github.fntz.omhs

sealed trait UnhandledReason
case object PathNotFound extends UnhandledReason
case object CookieIsMissing extends UnhandledReason
case object HeaderIsMissing extends UnhandledReason
case object BodyIsUnparsable extends UnhandledReason
case class UnhandledException(t:  Throwable) extends UnhandledReason
