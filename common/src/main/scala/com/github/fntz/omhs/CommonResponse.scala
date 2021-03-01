package com.github.fntz.omhs

// content-type
abstract class CommonResponse(
                         status: Int,
                         contentType: String,
                         content: String
                         )

case class NotFoundResponse(content: String)
  extends CommonResponse(404, "text/plain", content)
