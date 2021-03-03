package com.github.fntz.omhs

import scala.concurrent.{ExecutionContext, Future}

class AsyncResult {
// todo atomic: set in one thread, read in another
  private var value: CommonResponse = null.asInstanceOf[CommonResponse]
  private var completeWith: CommonResponse => Unit = null

  def onComplete(f: CommonResponse => Unit): Unit = {
    completeWith = f
    if (value != null) {
      completeWith.apply(value)
    }
  }

  def complete(x: CommonResponse): Unit = {
    value = x
    if (completeWith != null) {
      completeWith.apply(x)
    }
  }
}

object AsyncResult {

  def completed(response: CommonResponse): AsyncResult = {
    val result = new AsyncResult()
    result.complete(response)
    result
  }

  object Implicits {

    implicit class Future2Async[T](fu: Future[T])
                                  (implicit ec: ExecutionContext,
                                   writer: BodyWriter[T]
                                  ) {
      def toAsync: AsyncResult = {
        fromFuture(fu)
      }
    }

    implicit def string2AsyncResult(value: String): AsyncResult = {
      AsyncResult.completed(CommonResponse.plain(value))
    }

    implicit def future2Async[T](f: Future[T])(
      implicit writer: BodyWriter[T],
      ec: ExecutionContext
    ): AsyncResult = {
      AsyncResult.fromFuture[T](f)
    }
  }

  def fromFuture[T](fu: Future[T])
                   (implicit ec: ExecutionContext,
                    writer: BodyWriter[T]
                   ): AsyncResult = {
    new AsyncResult() {
      fu.map { x =>
        complete(writer.write(x))
      }
    }
  }

}
