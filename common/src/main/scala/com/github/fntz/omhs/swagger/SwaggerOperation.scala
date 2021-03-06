package com.github.fntz.omhs.swagger

case class SwaggerOperation(
                           tags: List[String],
                           summary: Option[String],
                           description: Option[String],
                           externalDocs: Option[SwaggerExternalDocumentation],
                           operationId: Option[String],
                           parameters: List[SwaggerParameter]
                           )
