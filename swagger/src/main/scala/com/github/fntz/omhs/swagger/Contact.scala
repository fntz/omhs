package com.github.fntz.omhs.swagger

/**
 * Contact information for the exposed API.
 * @param name - The identifying name of the contact person/organization.
 * @param url  - The URL pointing to the contact information. MUST be in the format of a URL.
 * @param email - The email address of the contact person/organization. MUST be in the format of an email address.
 */
case class Contact(
                           name: Option[String] = None,
                           url: Option[String] = None,
                           email: Option[String] = None
                         )