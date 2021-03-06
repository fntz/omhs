package com.github.fntz.omhs.swagger

/**
 * License information for the exposed API.
 * @param name - The license name used for the API.
 * @param url - A URL to the license used for the API. MUST be in the format of a URL.
 */
case class SwaggerLicense(
                           name: String,
                           url: Option[String]
                         )