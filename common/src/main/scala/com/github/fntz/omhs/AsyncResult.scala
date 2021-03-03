package com.github.fntz.omhs

import scala.concurrent.{ExecutionContext, Future}

class AsyncResult {

  var value: CommonResponse = null.asInstanceOf[CommonResponse]
  protected var completeWith: CommonResponse => Unit = null

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

  def complete(x: CommonResponse) = {
    val result = new AsyncResult()
    result.complete(x)
    result
  }

  object Implicits {
    implicit def string2AsyncResult(value: String): AsyncResult = {
      AsyncResult.complete(CommonResponse.plain(value))
    }

    implicit def future2Async[T](f: Future[T])(
      implicit writer: BodyWriter[T],
      ec: ExecutionContext
    ): AsyncResult = {
      AsyncResult.fromFuture[T](f)
    }
  }


  implicit class Future2Async[T](fu: Future[T])
                                (implicit ec: ExecutionContext,
                                 writer: BodyWriter[T]
                                ) {
    def toAsync: AsyncResult = {
      fromFuture(fu)
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
