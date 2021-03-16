import io.netty.handler.codec.http2.{DefaultHttp2DataFrame, DefaultHttp2HeadersFrame, Http2FrameStream}

case class AggregatedHttp2Message(
                                   streamId: Int,
                                   stream: Http2FrameStream,
                                   data: Vector[DefaultHttp2DataFrame],
                                  headers: Vector[DefaultHttp2HeadersFrame]) {

}
