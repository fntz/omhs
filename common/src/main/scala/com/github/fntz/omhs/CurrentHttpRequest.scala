package com.github.fntz.omhs

import io.netty.handler.codec.http.{HttpHeaders, HttpMethod}

case class CurrentHttpRequest(
                             uri: String,
                             path: String,
                             query: String,
                             method: HttpMethod,
                             headers: HttpHeaders,
                             rawBody: String
                      )