package com.github.fntz.omhs.swagger

import com.github.fntz.omhs.Rule

// based on https://swagger.io/specification/

case class Swagger(
                    openapi: String,
                    info: InfoObject,
                    servers: List[Server],
                    tags: List[Tag],
                    paths: List[PathItem],
                    externalDocs: Option[ExternalDocumentation]
                  ) {
  require(paths.nonEmpty)
}












