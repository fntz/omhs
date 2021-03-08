package com.github.fntz.omhs.swagger

case class Response(
                   description: String,
                   headers: Map[String, Header],
                   content: Map[String, String] // todo value should be generated
                   )
