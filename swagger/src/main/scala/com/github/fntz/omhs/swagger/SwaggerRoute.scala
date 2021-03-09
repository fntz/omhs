package com.github.fntz.omhs.swagger


import com.github.fntz.omhs._
import com.github.fntz.omhs.internal.{ExecutableRule, ParamDef}
import io.netty.handler.codec.http.{HttpMethod, HttpResponseStatus}

import scala.collection.mutable.{ArrayBuffer => AB}

case class SwaggerRoute(route: Route) {

  import SwaggerImplicits._

  private var rules: AB[SwaggeredRule] = new AB[SwaggeredRule]()

  def addRule(exe: SwaggeredRule): SwaggerRoute = {
    route.addRule(exe.rule)
    rules += exe
    this
  }

  def addRule(exe: ExecutableRule): SwaggerRoute = {
    addRule(exe.toSwagger)
  }

  def toPathItems: Map[String, PathItem] = {
    def fetch(method: HttpMethod, operations: Map[HttpMethod, Iterable[SwaggeredRule]]): Option[Operation] = {
      operations.get(method).flatMap(_.headOption).map(_.toOperation)
    }
    val tmp = if (rules.isEmpty) {
      route.current.map(_.toSwagger)
    } else {
      rules.toVector
    }
    tmp.groupBy(_.rule.rule.currentUrl).map { case (url, xs) =>
      val operations = xs.groupBy(_.rule.method)
      url -> PathItem(
        get = fetch(HttpMethod.GET, operations),
        post = fetch(HttpMethod.POST, operations),
        put = fetch(HttpMethod.PUT, operations),
        delete = fetch(HttpMethod.DELETE, operations),
        options = fetch(HttpMethod.OPTIONS, operations),
        head = fetch(HttpMethod.HEAD, operations),
        patch = fetch(HttpMethod.PATCH, operations),
        trace = fetch(HttpMethod.TRACE, operations)
      )
    }
  }

  // todo add validator for swagger

  /**
   * generate additional rule to swagger api
   * @param path - path to swagger generated js file
   * @param topSwaggerObject - top Swagger object
   * @return route
   */
  def swagger(path: String,
              topSwaggerObject: OpenApi = OpenApi.empty): SwaggerRoute = {
    // pass implicit generator and build all resources
    val tmp = topSwaggerObject.copy(paths = toPathItems)
    val gen = PlayJsonSupport.toJson(tmp)
    val exe = new ExecutableRule(Rule(HttpMethod.GET).path(path)) {
      override def run(defs: List[ParamDef[_]]): AsyncResult = {
        AsyncResult.completed(
          new CommonResponse(
            status = HttpResponseStatus.OK,
            content = gen.getBytes,
            contentType = "application/json"
          )
        )
      }
    }
    addRule(exe)
    this
  }
}
