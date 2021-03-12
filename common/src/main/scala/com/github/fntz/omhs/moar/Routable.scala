package com.github.fntz.omhs.moar

import com.github.fntz.omhs.{AsyncResult, Rule}
import com.github.fntz.omhs.internal.ExecutableRule

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

object Routable {

  def route(body: => AsyncResult): ExecutableRule = macro RoutableImpl.routeImpl

  implicit class ExecutableRuleExtensionsImpl(val rule: Rule) extends AnyVal {
    def >>(x: ExecutableRule): ExecutableRule = macro RoutableImpl.route1
  }
  def contentType(str: String): Unit = ???

}

private object RoutableImpl {

  def route1(c: whitebox.Context)(x: c.Expr[ExecutableRule]): c.Expr[ExecutableRule] = {
    x
  }

  def routeImpl(c: whitebox.Context)(body: c.Tree): c.Expr[ExecutableRule] = {
    import c.universe._

    val transformer = new Transformer {
      override def transform(tree: c.universe.Tree): c.universe.Tree = {
        tree match {
          case Apply(Select(Select(_, TermName("Routable")),
          TermName("contentType")), List(a)) =>
            q"response.headers.set(HttpHeaderNames.CONTENT_TYPE, ..$a)"

          case _ => super.transform(tree)
        }

      }
    }

    val rewrittenTree = c.untypecheck(transformer.transform(body))

    val gen =
      q"""
        import com.github.fntz.omhs.{AsyncResult, CommonResponse}
        import io.netty.handler.codec.http.DefaultFullHttpResponse
        import io.netty.handler.codec.http.{HttpResponseStatus, HttpMethod, HttpVersion, HttpHeaderNames}
        val response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)

        val rule = _root_.com.github.fntz.omhs.Rule(HttpMethod.GET)
        rule.path("test")

        val f = (x: String) => $rewrittenTree

        val rf = new _root_.com.github.fntz.omhs.internal.ExecutableRule(rule) {
          override val isCallRun: Boolean = false
          override def run2(defs: List[_root_.com.github.fntz.omhs.internal.ParamDef[_]],
            response: DefaultFullHttpResponse): DefaultFullHttpResponse = {
            val k = f("test")
            println("="*100)
            println(k)
            this.run(defs)
            response
          }
        }


        rf
      """
    // (response, result) =>

    println(gen)

    c.Expr[ExecutableRule](gen)
  }
}