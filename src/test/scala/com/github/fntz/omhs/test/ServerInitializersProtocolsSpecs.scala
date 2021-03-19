package com.github.fntz.omhs.test

import com.github.fntz.omhs.handlers.ServerInitializer
import com.github.fntz.omhs.{CurrentHttpRequest, Route, RoutingDSL, Setup}
import io.netty.buffer.ByteBuf
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.http._
import io.netty.handler.codec.http2._
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.util.CharsetUtil
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class ServerInitializersProtocolsSpecs extends Specification {

  import RoutingDSL._

  private val path = "test"
  private val rule = get(path) ~> {(x: CurrentHttpRequest) =>
    s"${x.isHttp2}"
  }
  private val route = new Route().addRule(rule)

  "http2" should {
    "switch from http1.1" in new MyTest(Setup.default.h2) {
      response must contain(HttpVersion.HTTP_1_1.toString)
    }

    "works with http2" in new MyTest(Setup.default.h2) {
      response must contain("HTTP/2.0")
    }
  }

  "http1.1" should {
    "works with http1.1" in new MyTest(Setup.default.h11) {
      response must contain(HttpVersion.HTTP_1_1.toString)
    }
  }


  class MyTest(setup: Setup) extends Scope {
    val handler = route.toHandler(setup.withoutCompression)
    val channel = new EmbeddedChannel(
      new LoggingHandler(LogLevel.DEBUG),
      new ServerInitializer(None,
        setup.withoutCompression, handler)
    )
    var response: String = ""
    if (setup.isH1) {
      val request = new DefaultFullHttpRequest(
        HttpVersion.HTTP_1_1, HttpMethod.GET, path
      )
      channel.writeInbound(request)
      val buf: ByteBuf = channel.readOutbound()
      response = buf.toString(CharsetUtil.UTF_8)
    } else {
      val http2Headers = new DefaultHttp2Headers()
      val data = new DefaultHttp2HeadersFrame(http2Headers, true).stream(
        new Http2FrameStream {
          override def id(): Int = 1

          override def state(): Http2Stream.State = Http2Stream.State.OPEN
        }
      )
      channel.writeInbound(data)
      val result = channel.readInbound()
      response = s"asd"
    }

    channel.close().sync()
  }

}
