package com.github.fntz.omhs

import scala.concurrent.{ExecutionContext, Future}

/*
  ChannelHandler is not thread safe by design in netty
 */
class AsyncResult {
  // todo tests multithreading
  @volatile private var value: Response = null.asInstanceOf[Response]
  @volatile private var completeWith: Response => Unit = null

  def onComplete(f: Response => Unit): Unit = {
    completeWith = f
    if (value != null) {
      completeWith.apply(value)
    }
  }

  def complete(x: Response): Unit = {
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

  def chunked(response: StreamResponse): AsyncResult = {
    val result = new AsyncResult()
    result.complete(response)
    result
  }

  object Streaming {
    implicit class IteratorToAsync[T](it: Iterator[Array[Byte]]) {
      def toAsync(contentType: String): AsyncResult = {
        AsyncResult.chunked(StreamResponse(contentType, it))
      }
    }
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

    implicit def commonResponse2ASyncResult(value: CommonResponse): AsyncResult = {
      AsyncResult.completed(value)
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
