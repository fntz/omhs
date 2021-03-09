package com.github.fntz.omhs

sealed trait UnhandledReason
case class PathNotFound(path: String) extends UnhandledReason
case class QueryIsUnparsable(params: Map[String, List[String]]) extends UnhandledReason
case class CookieIsMissing(cookieName: String) extends UnhandledReason
case class HeaderIsMissing(headerName: String) extends UnhandledReason
case object BodyIsUnparsable extends UnhandledReason
case class FilesIsUnparsable(t: Throwable) extends UnhandledReason
case class UnhandledException(t:  Throwable) extends UnhandledReason
