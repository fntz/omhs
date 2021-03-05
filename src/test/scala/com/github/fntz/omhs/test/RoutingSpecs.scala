package com.github.fntz.omhs.test

import com.github.fntz.omhs._
import com.github.fntz.omhs.methods.Methods._
import io.netty.channel.ChannelFutureListener
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.http._
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.util.CharsetUtil
import org.specs2.mutable.Specification
import org.specs2.specification.{AfterAll, Scope}
import java.util.UUID

class RoutingSpecs extends Specification with AfterAll {

  import DefaultHttpHandler._
  import ParamDSL._
  import com.github.fntz.omhs.p._
  import AsyncResult.Implicits._

  private val channels = scala.collection.mutable.ArrayBuffer[EmbeddedChannel]()

  "routing" in {
    val r1 = get("test" / StringParam) ~> { (x: String) => x }

    "match string" in new RouteTest(r1, "/test/foo") {
      status ==== HttpResponseStatus.OK
      content ==== "foo"
    }

    val r2 = get("test" / LongParam) ~> { (x: Long) => s"$x" }
    "match long" in new RouteTest(r2, "/test/123") {
      status ==== HttpResponseStatus.OK
      content ==== "123"
    }

    val uuid = UUID.randomUUID()
    val r3 = get("test" / UUIDParam) ~> {(x: UUID) => s"$x"}
    "match uuid"  in new RouteTest(r3, s"/test/$uuid") {
      status ==== HttpResponseStatus.OK
      content ==== s"$uuid"
    }

    "match regex" in {
      pending
    }

    "match tail" in {
      pending
    }

    "match header" in {
      pending
    }

    "fetch body" in {
      pending
    }

    "fetch files" in {
      pending
    }

    "unparsed body" in {
      pending
    }

    "unparsed files" in {
      pending
    }

    "header is missing" in {
      pending
    }

    "path is not matched" in {
      pending
    }

    "function throw exception: UnexcpetedReason" in {
      pending
    }
  }

  private def req(path: String) = {
    new DefaultFullHttpRequest(
      HttpVersion.HTTP_1_1, HttpMethod.GET, path
    )
  }

  private class RouteTest(rule: RuleAndF, path: String) extends Scope {
    println(s"------------__> ${path}")
    val ro = (new Route).addRule(rule).toHandler
    val channel = new EmbeddedChannel(new LoggingHandler(LogLevel.DEBUG))
    channel.pipeline()
      .addFirst(new LoggingHandler(LogLevel.DEBUG))
      .addLast("codec", new HttpRequestDecoder())
      .addLast("aggregator", new HttpObjectAggregator(512*1024))
      .addLast(ro)
    channels += channel
    val request = req(path)
    channel.writeInbound(request)
    val response: DefaultFullHttpResponse = channel.readOutbound()
    val status = response.status()
    val content =  response.content().toString(CharsetUtil.UTF_8)
  }

  override def afterAll(): Unit = {
    channels.foreach { ch =>
      ch.close().sync().addListener(ChannelFutureListener.CLOSE)
    }
  }
}
