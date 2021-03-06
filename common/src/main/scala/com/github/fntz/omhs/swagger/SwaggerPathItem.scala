package com.github.fntz.omhs.swagger

case class SwaggerPathItem(
                          `$ref`: Option[String],
                          summary: Option[String],
                          description: Option[String],
                          servers: List[SwaggerServer]
                          )
