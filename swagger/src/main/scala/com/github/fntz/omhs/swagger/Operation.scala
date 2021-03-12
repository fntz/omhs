package com.github.fntz.omhs.swagger

case class Operation(
                     tags: List[String],
                     summary: Option[String],
                     description: Option[String],
                     externalDocs: Option[ExternalDocumentation],
                     operationId: Option[String],
                     parameters: List[Parameter],
                     requestBody: Option[RequestBody],
                     responses: Map[Int, Response],
                     deprecated: Boolean = false
                     // todo security
                   )
