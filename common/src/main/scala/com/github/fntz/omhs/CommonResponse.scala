package com.github.fntz.omhs

// content-type
class CommonResponse(
                         val status: Int,
                         val contentType: String,
                         val content: String // todo should be array of bytes
                         )
case class OkResponse(override val content: String)
  extends CommonResponse(200, "text/plain", content)

case class NotFoundResponse(override val content: String)
  extends CommonResponse(404, "text/plain", content)
