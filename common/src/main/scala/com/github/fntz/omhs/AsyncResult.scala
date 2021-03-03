package com.github.fntz.omhs

import scala.concurrent.{ExecutionContext, Future}

class AsyncResult[T](implicit val writer: BodyWriter[T]) {

  private var value: T = null.asInstanceOf[T]
  private var completeWith: T => Unit = null

  def onComplete(f: T => Unit): Unit = {
    completeWith = f
    if (value != null) {
      completeWith.apply(value)
    }
  }

  def complete(x: T): Unit = {
    value = x
    if (completeWith != null) {
      completeWith.apply(x)
    }
  }
}

object AsyncResult {

  implicit class Future2Async[T](fu: Future[T])
                                (implicit writer: BodyWriter[T],
                                                ec: ExecutionContext
  ) {
    def toAsync: AsyncResult[T] = {
      fromFuture(fu)
    }
  }

  def fromFuture[T](fu: Future[T])
                   (implicit writer: BodyWriter[T],
                    ec: ExecutionContext
                   ): AsyncResult[T] = {
    new AsyncResult[T]() {
      fu.map(complete)
    }
  }

}
