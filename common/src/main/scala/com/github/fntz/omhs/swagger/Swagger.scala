package com.github.fntz.omhs.swagger

// based on https://swagger.io/specification/
/**
 * This is the root document object of the OpenAPI document.
 * @param openapi - This string MUST be the semantic version number of the OpenAPI Specification version that the OpenAPI document uses.
 *                The openapi field SHOULD be used by tooling specifications and clients to interpret the OpenAPI document.
 *                This is not related to the API info.version string.
 * @param info  - Provides metadata about the API. The metadata MAY be used by tooling as required.
 * @param servers - An array of Server Objects, which provide connectivity information to a target server. If the servers property is not provided, or is an empty array,
 *                the default value would be a Server Object with a url value of /.
 * @param tags - A list of tags used by the specification with additional metadata.
 *              The order of the tags can be used to reflect on their order by the parsing tools.
 *              Not all tags that are used by the Operation Object must be declared.
 *              The tags that are not declared MAY be organized randomly or based on the tools' logic.
 *              Each tag name in the list MUST be unique.
 * @param paths - The available paths and operations for the API.
 * @param externalDocs - Additional external documentation.
 */
// TODO securityDefinitions is not reflected into specs, but still used in example !
case class Swagger(
                    swagger: String,
                    info: InfoObject,
                    servers: List[Server],
                    tags: List[Tag],
                    paths: Map[String, PathItem],
                    externalDocs: Option[ExternalDocumentation]
                  ) {
 // require(paths.nonEmpty)
}
object Swagger {
  def empty = Swagger(
    swagger = "2.0",
    info = InfoObject.empty,
    servers = Nil,
    tags = Nil,
    paths = Map.empty,
    externalDocs = None
  )
}












