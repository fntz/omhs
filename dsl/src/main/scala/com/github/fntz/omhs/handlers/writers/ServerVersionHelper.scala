package com.github.fntz.omhs.handlers.writers

import com.github.fntz.omhs.util.CollectionsConverters
import io.netty.util.Version

object ServerVersionHelper {
  import CollectionsConverters._

  private val currentProject = "omhs"
  val ServerVersion = s"$currentProject on " + Version.identify().values.toScala.headOption
    .map { v => s"netty-${v.artifactVersion()}"}
    .getOrElse("unknown")

}
