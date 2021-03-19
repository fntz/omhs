package com.github.fntz.omhs.impl

import scala.reflect.macros.whitebox

private[impl] object Shared {
  private val propName = "omhs.logLevel"

  object LogLevel extends Enumeration {
    type Level = Value
    val verbose, info, none = Value
  }

  class Logger(c: whitebox.Context) {
    private val currentLevel = Option(System.getProperty(propName)) match {
      case Some("verbose") => LogLevel.verbose
      case Some("info") => LogLevel.info
      case _ =>  LogLevel.none
    }

    def verbose(str: String): Unit = {
      if (currentLevel == LogLevel.verbose) {
        c.info(c.enclosingPosition.focus, str, force = false)
      }
    }
    def info(str: String): Unit = {
      if (currentLevel == LogLevel.info || currentLevel == LogLevel.verbose) {
        c.info(c.enclosingPosition.focus, str, force = false)
      }
    }
  }

  def guardSbtOptions(c: whitebox.Context): Unit = {
    if (!c.compilerSettings.contains("-Ydelambdafy:inline")) {
      c.abort(c.enclosingPosition.focus, "Routing generation requires `-Ydelambdafy:inline` option. " +
        "Change scalacOptions in your build.sbt")
    }
  }

  def guardRoutes(c: whitebox.Context)(f: c.Tree): Unit = {
    import c.universe._
    val forbiddenWithoutRoute = Vector("contentType", "status", "setCookie", "setHeader")
    f.collect {
      case Select(Select(Select(Select(_, TermName("omhs")), TermName("moar")), _),
      TermName(term)) if forbiddenWithoutRoute.contains(term) =>
        c.abort(c.enclosingPosition.focus, s"`$term` must be wrapped in an `route` block")
    }
  }

  val complex: Vector[String] = Vector("body", "query")

  // because helper methods (query, body, long) in the same package as implicits
  val banned: Vector[String] =
    Vector(
      "StringExt",
      "PathParamExt",
      "ExecutableRuleExtensions",
      "post", "get", "head", "put", "patch", "delete", "trace", "options", "connect"
    )

  val ignored = banned ++ complex



}
