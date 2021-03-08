package com.github.fntz.omhs.swagger

/**
 * The object provides metadata about the API. The metadata MAY be used by the clients if needed, and MAY be presented in editing or documentation generation tools for convenience.
 * @param version - The version of the OpenAPI document (which is distinct from the OpenAPI Specification version or the API implementation version).
 * @param title - The title of the API.
 * @param description - A short description of the API. CommonMark syntax MAY be used for rich text representation.
 * @param termsOfService - A URL to the Terms of Service for the API. MUST be in the format of a URL.
 * @param contact - The contact information for the exposed API.
 * @param license - The license information for the exposed API.
 */
case class InfoObject(
                              version: String,
                              title: String,
                              description: Option[String] = None,
                              termsOfService: Option[String] = None,
                              contact: Option[Contact] = None,
                              license: Option[License] = None
                            )

object InfoObject {
  val empty: InfoObject = InfoObject(
    version = "0.0.1",
    title = "auto-generated api",
    description = None,
    termsOfService = None,
    contact = None,
    license = None
  )
}
