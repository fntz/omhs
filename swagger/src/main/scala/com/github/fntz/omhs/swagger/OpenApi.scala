package com.github.fntz.omhs.swagger

// based on https://github.com/OAI/OpenAPI-Specification/blob/3.0.1/versions/3.0.1.md
/**
 * This is the root document object of the OpenAPI document.
 * @param openapi - This string MUST be the semantic version number of the OpenAPI Specification version
 *                that the OpenAPI document uses.
 *                The openapi field SHOULD be used by tooling specifications and clients
 *                to interpret the OpenAPI document.
 *                This is not related to the API info.version string.
 * @param info  - Provides metadata about the API. The metadata MAY be used by tooling as required.
 * @param servers - An array of Server Objects, which provide connectivity information to a target server. If the servers property is not provided, or is an empty array,
 *                the default value would be a Server Object with a url value of /.
 * @param tags - A list of tags used by the specification with additional metadata.
 * @param paths - The available paths and operations for the API.
 * @param externalDocs - Additional external documentation.
 */
case class OpenApi(
                    openapi: String,
                    info: InfoObject,
                    servers: List[Server],
                    tags: List[Tag],
                    paths: Map[String, PathItem],
                    externalDocs: Option[ExternalDocumentation]
                  ) {
 // require(paths.nonEmpty)
}
object OpenApi {
  def empty = OpenApi(
    openapi = "3.0.0",
    info = InfoObject.empty,
    servers = Nil,
    tags = Nil,
    paths = Map.empty,
    externalDocs = None
  )
}












