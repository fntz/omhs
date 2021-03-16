package com.github.fntz.omhs

import com.github.fntz.omhs.handlers.HttpHandler
import com.github.fntz.omhs.internal.ExecutableRule
import com.github.fntz.omhs.util.OverlapDetector
import io.netty.handler.codec.http.{FullHttpResponse, HttpResponse}
import org.slf4j.LoggerFactory

import scala.collection.mutable.{ArrayBuffer => AB}

/**
 * represent Route information (set of rules)
 */
class Route {

  private val logger = LoggerFactory.getLogger(getClass)
  private val rules: AB[ExecutableRule] = new AB[ExecutableRule]()

  private var defaultResponseHandler = (response: HttpResponse) => response

  private var unhandledDefault = (reason: UnhandledReason) => {
    val result = reason match {
      case PathNotFound(value) => (404, value)
      case CookieIsMissing(value) => (400, s"cookie: $value is missing")
      case HeaderIsMissing(value) => (400, s"header: $value is missing")
      case BodyIsUnparsable(ex) => (400, s"body is incorrect: $ex")
      case FilesIsUnparsable(_) => (500, s"files is corrupted")
      case QueryIsUnparsable(params) =>
        val q = params.map(x => s"${x._1}=${x._2.mkString(",")}").mkString(",")
        (400, s"query is unparsable: $q")
      case UnhandledException(ex) => (500, s"$ex")
    }
    CommonResponse(
      status = result._1,
      contentType = "text/plain",
      content = result._2
    )
  }


  def current: Vector[ExecutableRule] = rules.toVector

  /**
   * this method is internal. I use it inside OMHSHttpHandler
   * update response with user-defined function
   * @param response - HttpResponse before send to client
   * @return modified response
   */
  def rewrite(response: HttpResponse): HttpResponse =
    defaultResponseHandler.apply(response)

  /**
   * {{{
   *  val rule = get("test") ~> { "done" }
   *  val route = new Route().addRule(rule)
   *  route.onEveryResponse((r: FullHttpResponse => {
   *     r.headers().set("foo", "bar")
   *     r
   *  })
   * }}}
   * @param rewriter - function for updating response (add headers for example)
   * @return
   */
  def onEveryResponse(rewriter: HttpResponse => HttpResponse): Route = {
    defaultResponseHandler = rewriter
    this
  }

  /**
   *
   * @param exe - additional executable rule in route
   * @return
   */
  def addRule(exe: ExecutableRule): Route = {
    rules += exe
    this
  }

  def ::(other: Route): Route = {
    rules ++= other.rules
    this
  }

  /**
   * alias for `addRule``
   */
  def ::(other: ExecutableRule): Route = {
    addRule(other)
    this
  }

  /**
   * {{{
   *   val rule = get("test") ~> { "done" }
   *   val route = new Route.addRule(rule)
   *   .onUnhandled((reason: UnhandledReason => {
   *      reason match {
   *         case PathNotFound(path) => // redirect to 404.html
   *         case _ => // 500 internal server error
   *      }
   *   })
   * }}}
   * @param handler - function for handling unexpected behaviour (errors inside the app)
   * @return
   */
  def onUnhandled(handler: UnhandledReason => CommonResponse): Route = {
    unhandledDefault = handler
    this
  }

  /**
   * internal method @see OMHSServer
   * @return
   */
  def currentUnhandled: UnhandledReason => CommonResponse = {
    unhandledDefault
  }

  /**
   * Convert to netty Handler
   * @return OMHSHttpHandler
   */
  def toHandler: HttpHandler = toHandler(Setup.default)

  /**
   * Convert to netty Handler
   * @param setup - common application setup
   * @return
   */
  def toHandler(setup: Setup): HttpHandler = {
    current.foreach { rule =>
      logger.debug(s"Define ${rule.method} -> ${rule.rule.currentUrl}")
    }
    OverlapDetector.detect(current.map(_.rule))
    new HttpHandler(this, setup)
  }

  override def toString: String = {
    rules.map(_.toString).mkString("\n")
  }

}
