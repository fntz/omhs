package com.github.fntz.omhs.swagger

case class Response(
                   description: String,
                   headers: Map[String, String] = Map.empty, // todo check value should be Header
                   content: Map[String, String] = Map.empty  // todo value should be generated
                   )
