package com.github.fntz.omhs.swagger

/**
 * Allows referencing an external resource for extended documentation.
 * @param url - The URL for the target documentation. Value MUST be in the format of a URL.
 * @param description - A short description of the target documentation.
 *                    CommonMark syntax MAY be used for rich text representation.
 */
case class ExternalDocumentation(
                                         url: String,
                                         description: Option[String]
                                       )
