package com.github.fntz.omhs.swagger

/**
 * Describes the operations available on a single path.
 * @param get - A definition of a GET operation on this path.
 * @param post - A definition of a POST operation on this path.
 * @param put - A definition of a PUT operation on this path.
 * @param delete - A definition of a DELETE operation on this path.
 * @param options - A definition of a OPTIONS operation on this path.
 * @param head - A definition of a HEAD operation on this path.
 * @param patch - A definition of a PATCH operation on this path.
 * @param trace - A definition of a TRACE operation on this path.
 */
case class PathItem(
                    get: Option[Operation],
                    post: Option[Operation],
                    put: Option[Operation],
                    delete: Option[Operation],
                    options: Option[Operation],
                    head: Option[Operation],
                    patch: Option[Operation],
                    trace: Option[Operation]
                   )

object PathItem {
  val empty: PathItem = {
    PathItem(
      get = None,
      post = None,
      put = None,
      delete = None,
      options = None,
      head = None,
      patch = None,
      trace = None
    )
  }
}
