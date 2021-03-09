package com.github.fntz.omhs.swagger

import com.github.fntz.omhs.internal.ExecutableRule
import com.github.fntz.omhs.{DefaultHttpHandler, Route, Setup}

object SwaggerImplicits {

  implicit class RuleExt(val exe: ExecutableRule) extends AnyVal {
    def toSwagger: SwaggeredRule = {
      SwaggeredRule(exe,
        tags = Nil,
        description = None,
        externalDocs = None,
        operationId = None,
        parameters = Nil,
        requestBody = None,
        responses = Map.empty,
        summary = None
      )
    }
  }

  implicit class RouteToSwaggerExt(val route: Route) extends AnyVal {
    def addRule(r: SwaggeredRule): SwaggerRoute = {
      SwaggerRoute(route = route).addRule(r)
    }

    def toSwagger: SwaggerRoute = {
      SwaggerRoute(route = route)
    }
  }

  implicit class SwaggerRouteExt(val s: SwaggerRoute) extends AnyVal {
    def toHandler: DefaultHttpHandler = toHandler(Setup.default)
    def toHandler(setup: Setup): DefaultHttpHandler = new DefaultHttpHandler(s.route, setup)
  }


}
