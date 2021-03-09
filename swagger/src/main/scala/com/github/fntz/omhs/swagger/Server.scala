package com.github.fntz.omhs.swagger

/**
 * An object representing a Server.
 * @param url - A URL to the target host. This URL supports Server Variables and MAY be relative,
 *              to indicate that the host location is relative to the location
 *              where the OpenAPI document is being served. Variable substitutions
 *              will be made when a variable is named in {brackets}.
 * @param description - An optional string describing the host designated by the URL.
 *                    CommonMark syntax MAY be used for rich text representation.
 * @param variables - A map between a variable name and its value.
 *                  The value is used for substitution in the server's URL template.
 *
 */
case class Server(
                          url: String,
                          description: Option[String],
                          variables: Map[String, Variable]
                        )
