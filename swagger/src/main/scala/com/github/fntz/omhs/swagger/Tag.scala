package com.github.fntz.omhs.swagger

/**
 * Adds metadata to a single tag that is used by the Operation Object.
 * It is not mandatory to have a Tag Object per tag defined in the Operation Object instances.
 * @param name - The name of the tag.
 * @param description - A short description for the tag.
 *                    CommonMark syntax MAY be used for rich text representation.
 * @param externalDocs - Additional external documentation for this tag.
 */
case class Tag(name: String,
               description: Option[String],
               externalDocs: Option[ExternalDocumentation]
              )
