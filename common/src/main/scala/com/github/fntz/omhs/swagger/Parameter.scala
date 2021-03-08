package com.github.fntz.omhs.swagger

import com.github.fntz.omhs.swagger.In.In

/**
 * Describes a single operation parameter.
 * A unique parameter is defined by a combination of a name and location.
 *
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
sealed trait Parameter {
  def name: String
  def description: Option[String]
  def required: Boolean = false
  def deprecated: Boolean = false
  def allowEmptyValue: Boolean = false
  def in: In
}
case class Query(name: String,
                 description: Option[String],
                 override val required: Boolean = false,
                 override val deprecated: Boolean = false,
                 override val allowEmptyValue: Boolean = false
                ) extends Parameter {
  override def in: In = In.query
}


case class Header(name: String,
                 description: Option[String],
                 override val required: Boolean = false,
                 override val deprecated: Boolean = false,
                 override val allowEmptyValue: Boolean = false
                ) extends Parameter {
  override def in: In = In.header
}

case class Path(name: String,
                  description: Option[String],
                  override val deprecated: Boolean = false,
                  override val allowEmptyValue: Boolean = false
                 ) extends Parameter {
  override def in: In = In.path
  override def required: Boolean = true
}

case class Cookie(name: String,
                description: Option[String],
                override val deprecated: Boolean = false,
                override val required: Boolean = false,
                override val allowEmptyValue: Boolean = false
               ) extends Parameter {
  override def in: In = In.path
}


object In extends Enumeration {
  type In = Value
  val query, header, path, cookie = Value
}
