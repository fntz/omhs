package com.github.fntz.omhs.test

import com.github.fntz.omhs.{AsyncResult, CommonResponse, CurrentHttpRequest, OMHSServer, Route, RoutingDSL, Setup}
import com.github.fntz.omhs.handlers.ServerInitializer
import com.github.fntz.omhs.internal.ExecutableRule
import io.netty.buffer.{ByteBuf, ByteBufUtil}
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.http.HttpConstants.{CR, LF}
import io.netty.handler.codec.http.{DefaultFullHttpRequest, DefaultFullHttpResponse, DefaultHttpContent, DefaultHttpResponse, FullHttpRequest, FullHttpResponse, HttpConstants, HttpContentCompressor, HttpHeaderNames, HttpMethod, HttpObjectAggregator, HttpObjectEncoder, HttpRequestDecoder, HttpResponse, HttpResponseEncoder, HttpResponseStatus, HttpServerCodec, HttpVersion}
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.util.CharsetUtil
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class ServerInitializersProtocolsSpecs extends Specification {

  import RoutingDSL._
  import AsyncResult.Implicits._

  private val path = "test"
  private val rule = get(path) ~> {(x: CurrentHttpRequest) =>
    s"${x.isHttp2}"
  }
  private val route = new Route().addRule(rule)

  "http2" should {
    "switch from http1.1" in new MyTest(Setup.default.h2) {
      response must contain(HttpVersion.HTTP_1_1.toString)
    }

    "works with http2" in {
      success
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
      new ServerInitializer(Some(OMHSServer.getJdkSslContext),
        setup.withoutCompression, handler)
    )

    val request = new DefaultFullHttpRequest(
      HttpVersion.HTTP_1_1, HttpMethod.GET, path
    )

    channel.writeInbound(request)
    val buf: ByteBuf = channel.readOutbound()
    val response = buf.toString(CharsetUtil.UTF_8)
  }



}
