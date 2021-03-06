package com.github.fntz.omhs.swagger

/**
 * Describes a single operation parameter.
 * A unique parameter is defined by a combination of a name and location.
 * @param name - The name of the parameter.
 * @param in -  The location of the parameter.
 * @param description - A brief description of the parameter. This could contain examples of use.
 *                    CommonMark syntax MAY be used for rich text representation.
 * @param required - Determines whether this parameter is mandatory.
 *                 If the parameter location is "path", this property is REQUIRED and
 *                 its value MUST be true.
 *                 Otherwise, the property MAY be included and its default value is false.
 * @param deprecated - Specifies that a parameter is deprecated and SHOULD be transitioned out of usage.
 *                   Default value is false.
 * @param allowEmptyValue - Sets the ability to pass empty-valued parameters.
 */
case class SwaggerParameter(
                           name: String,
                           in: SwaggerIn.Value,
                           deprecated: Boolean = false,
                           description: Option[String],
                           required: Option[Boolean],
                           allowEmptyValue: Option[Boolean]
                           ) {
  // todo require
}

object SwaggerIn extends Enumeration {
  type SwaggerIn = Value
  val query, header, path, cookie = Value
}