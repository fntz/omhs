package com.github.fntz.omhs.swagger

import com.github.fntz.omhs.internal.{ExecutableRule, LongParam, UUIDParam}

case class SwaggeredRule(rule: ExecutableRule,
                         tags: List[String],
                         description: Option[String],
                         summary: Option[String],
                         externalDocs: Option[ExternalDocumentation],
                         operationId: Option[String],
                         parameters: List[Parameter],
                         requestBody: Option[RequestBody],
                         responses: Map[Int, Response],
                         deprecated: Boolean = false
                        ) {

  def withTags(tags: String*): SwaggeredRule =
    copy(tags = tags.toList)

  def withSummary(summary: String): SwaggeredRule =
    copy(summary = Some(summary))

  def withDescription(description: String): SwaggeredRule =
    copy(description = Some(description))

  def withDeprecated(deprecated: Boolean): SwaggeredRule =
    copy(deprecated = deprecated)

  def withResponse(status: Int, response: Response): SwaggeredRule =
    copy(responses = responses ++ Map(status -> response))

  def withResponses(responses: Map[Int, Response]): SwaggeredRule =
    copy(responses = this.responses ++ responses)

  def withExternalDocs(doc: ExternalDocumentation): SwaggeredRule =
    copy(externalDocs = Some(doc))

  def withOperationId(operationId: String): SwaggeredRule =
    copy(operationId = Some(operationId))

  // user-defined params should be ignored ("test" / string) => only string is needed
  def toParameters: List[Parameter] = {
    val currentPaths = rule.rule.params.filterNot(_.isUserDefined).map { p =>
      val dataType = p match {
        case _: LongParam => DataType.int64
        case _: UUIDParam => DataType.uuid
        case _ => DataType.string
      }
      Path(
        name = p.name,
        description = p.description,
        dataType = dataType
      )
    }
    val currentHeaders = rule.rule.currentHeaders.map { h =>
      Header(
        name = h.headerName,
        description = h.description,
        required = true,
        dataType = DataType.string
      )
    }
    val currentCookies = rule.rule.currentCookies.map { c =>
      Cookie(
        name = c.cookieName,
        description = c.description,
        required = true,
        dataType = DataType.string
      )
    }
    val formData = if (rule.rule.isFilePassed) {
      FormData(
        name = rule.rule.currentFileParam.name,
        description = rule.rule.currentFileParam.description
      ) :: Nil
    } else {
      Nil
    }

    (currentPaths ++ currentHeaders ++ currentCookies ++ formData).toList
  }

  def toOperation: Operation = {
    Operation(
      tags = tags,
      summary = summary,
      description = description,
      externalDocs = externalDocs,
      operationId = operationId,
      parameters = toParameters,
      requestBody = requestBody,
      responses = responses,
      deprecated = deprecated
    )
  }
}
