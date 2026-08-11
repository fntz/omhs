package com.github.fntz.omhs.impl

import scala.quoted.*

private[impl] object Shared {
  private val propName = "omhs.logLevel"

  enum LogLevel:
    case verbose, info, none

  class Logger()(using Quotes) {
    import quotes.reflect.*

    private val currentLevel = Option(System.getProperty(propName)) match {
      case Some("verbose") => LogLevel.verbose
      case Some("info")    => LogLevel.info
      case _               => LogLevel.none
    }

    def verbose(str: String): Unit = {
      if currentLevel == LogLevel.verbose then
        report.info(str)
    }

    def info(str: String): Unit = {
      if currentLevel == LogLevel.info || currentLevel == LogLevel.verbose then
        report.info(str)
    }
  }

  def guardRoutes(using q: Quotes)(f: q.reflect.Tree): Unit = {
    import q.reflect.*
    val forbiddenWithoutRoute = Set("contentType", "status", "setCookie", "setHeader")

    def isRouteFun(fun: Term): Boolean =
      fun.symbol.name == "route" || (fun match
        case Ident("route")     => true
        case Select(_, "route") => true
        case TypeApply(inner, _) => isRouteFun(inner)
        case _                  => false
      )

    // `~> route { ... }`: by the time RoutingImpl runs, `route` may already be expanded.
    // Its Inlined node still embeds the original body with moar stubs — ignore those.
    object AlreadyRouted extends TreeAccumulator[Boolean]:
      def foldTree(x: Boolean, tree: Tree)(owner: Symbol): Boolean =
        if x then true
        else tree match
          case Apply(fun, _) if isRouteFun(fun) => true
          case Inlined(Some(call), _, _) if call.symbol.name == "route" ||
              call.toString.contains("route") || call.toString.contains("MoarImpl") => true
          case Select(Ident("MutableState"), "empty") => true
          case Select(_, "empty") if tree.symbol.owner.name.contains("MutableState") => true
          case _ => foldOverTree(false, tree)(owner)

    if AlreadyRouted.foldTree(false, f)(Symbol.spliceOwner) then
      return

    def isForbiddenMoarCall(fun: Term, name: String): Boolean =
      forbiddenWithoutRoute(name) && (
        isMoar(using q)(fun) ||
          fun.symbol.owner.name == "moar" ||
          fun.symbol.owner.fullName.contains(".moar")
      )

    object Traverser extends TreeTraverser:
      override def traverseTree(tree: Tree)(owner: Symbol): Unit = tree match
        case Apply(sel @ Select(qual, name), _) if isForbiddenMoarCall(qual, name) || isForbiddenMoarCall(sel, name) =>
          report.errorAndAbort(s"`$name` must be wrapped in an `route` block")
        case Apply(id @ Ident(name), _) if forbiddenWithoutRoute(name) &&
            (id.symbol.owner.name == "moar" || id.symbol.owner.fullName.contains(".moar")) =>
          report.errorAndAbort(s"`$name` must be wrapped in an `route` block")
        case _ =>
          traverseTreeChildren(tree)(owner)

    Traverser.traverseTree(f)(Symbol.spliceOwner)
  }

  def isMoar(using q: Quotes)(term: q.reflect.Term): Boolean = {
    import q.reflect.*
    def loop(t: Tree): Boolean = t match
      case Ident(name) => name == "moar" || name.endsWith("$package")
      case Select(qual, name) =>
        name == "moar" || name == "package" || loop(qual)
      case _ =>
        val fn = term.tpe.typeSymbol.fullName
        fn.contains(".moar.") || fn.endsWith(".moar") || fn.endsWith(".moar$")
    loop(term) || term.symbol.owner.name == "moar" || term.symbol.owner.fullName.contains(".moar")
  }

  val complex: Vector[String] = Vector("body", "query")

  // because helper methods (query, body, long) in the same package as implicits
  val banned: Vector[String] =
    Vector(
      "StringExt",
      "PathParamExt",
      "ExecutableRuleExtensions",
      "post", "get", "head", "put", "patch", "delete", "trace", "options"
    )

  val ignored = banned ++ complex
}
