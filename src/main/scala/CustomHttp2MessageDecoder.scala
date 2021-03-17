import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import io.netty.handler.codec.http2.{DefaultHttp2DataFrame, DefaultHttp2HeadersFrame, EmptyHttp2Headers, Http2Frame, Http2FrameStream}
import jdk.internal.net.http.common.Log.headers

import java.util

class MutableHttp2Message {
  private var header: DefaultHttp2HeadersFrame =
    new DefaultHttp2HeadersFrame(EmptyHttp2Headers.INSTANCE)
  private var data: DefaultHttp2DataFrame =
    new DefaultHttp2DataFrame(Unpooled.EMPTY_BUFFER)

  def set(x: DefaultHttp2DataFrame) = {
    data = x
  }

  def set(x: DefaultHttp2HeadersFrame) = {
    header = x
  }

  def toAggregated(streamId: Int, stream: Http2FrameStream): AggregatedHttp2Message = {
    AggregatedHttp2Message(
      streamId = streamId,
      stream = stream,
      data = data,
      headers = header
    )
  }
}

class CustomHttp2MessageDecoder extends MessageToMessageDecoder[Http2Frame] {

  private val keeper = new MutableHttp2Message()

  override def decode(ctx: ChannelHandlerContext, msg: Http2Frame, out: util.List[AnyRef]): Unit = {
    msg match {
      case x: DefaultHttp2HeadersFrame =>
        keeper.set(x)
        if (x.isEndStream) {
          out.add(keeper.toAggregated(x.stream().id(), x.stream()))
        }

      case x: DefaultHttp2DataFrame =>
        keeper.set(x)
        if (x.isEndStream) {
          out.add(keeper.toAggregated(x.stream().id(), x.stream()))
        }

      case _ =>
        println(s"==========> $msg")
        out.add(msg)
    }

  }
}
