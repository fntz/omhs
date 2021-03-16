import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import io.netty.handler.codec.http2.{DefaultHttp2DataFrame, DefaultHttp2HeadersFrame, Http2Frame, Http2FrameStream}

import scala.collection.mutable.{ArrayBuffer => AB}
import java.util

class MutableHttp2Message {
  private var headers = AB[DefaultHttp2HeadersFrame]()
  private var datas = AB[DefaultHttp2DataFrame]()

  def isEmpty = headers.isEmpty && datas.isEmpty

  def pushData(x: DefaultHttp2DataFrame) = {
    datas += x
  }

  def pushHeader(x: DefaultHttp2HeadersFrame) = {
    headers += x
  }

  def toAggregated(streamId: Int, stream: Http2FrameStream): AggregatedHttp2Message = {
    AggregatedHttp2Message(
      streamId = streamId,
      stream = stream,
      data = datas.toVector,
      headers = headers.toVector
    )
  }
}

class CustomHttp2MessageDecoder extends MessageToMessageDecoder[Http2Frame] {

  private val keeper = new MutableHttp2Message()

  override def decode(ctx: ChannelHandlerContext, msg: Http2Frame, out: util.List[AnyRef]): Unit = {
    msg match {
      case x: DefaultHttp2HeadersFrame =>
        keeper.pushHeader(x)
        if (x.isEndStream) {
          out.add(keeper.toAggregated(x.stream().id(), x.stream()))
        }

      case x: DefaultHttp2DataFrame =>
        keeper.pushData(x)
        if (x.isEndStream) {
          out.add(keeper.toAggregated(x.stream().id(), x.stream()))
        }

      case _ =>
        out.add(msg)
    }

  }
}
