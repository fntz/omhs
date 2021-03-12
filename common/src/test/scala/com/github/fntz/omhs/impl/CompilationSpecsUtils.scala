package com.github.fntz.omhs.impl

import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification

import scala.tools.reflect.{ToolBox, ToolBoxError}

// based on https://stackoverflow.com/a/15323481/13751202
trait CompilationSpecsUtils { self: Specification =>

  def compile(code: String): MatchResult[Any] = {
    tryCompile(code) must not(throwA[ToolBoxError])
  }

  def doesntCompile(code: String): MatchResult[Any] = {
    tryCompile(code) must throwA[ToolBoxError]
  }

  def doesntCompile(code: String, expectedErrorMessage: String): MatchResult[Any] = {
    tryCompile(code) must throwA[ToolBoxError](expectedErrorMessage)
  }

  def tryCompile(code: String): Any = {
    eval(code)
  }

  private def eval(code: String, compileOptions: String = "-cp target/classes -Ydelambdafy:inline"): Any = {
    val tb = mkToolbox(compileOptions)
    tb.eval(tb.parse(code))
  }

  private def mkToolbox(compileOptions: String = ""): ToolBox[_ <: scala.reflect.api.Universe] = {
    val m = scala.reflect.runtime.currentMirror
    m.mkToolBox(options = compileOptions)
  }

}