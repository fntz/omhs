package com.github.fntz.omhs.swagger

case class PathItem(
                    summary: Option[String],
                    description: Option[String],
                    servers: List[Server],
                    get: List[Operation],
                    post: List[Operation],
                    put: List[Operation],
                    delete: List[Operation],
                    options: List[Operation],
                    head: List[Operation],
                    patch: List[Operation],
                    trace: List[Operation]
                   )
