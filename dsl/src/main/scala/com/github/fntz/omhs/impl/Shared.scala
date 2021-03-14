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

}
