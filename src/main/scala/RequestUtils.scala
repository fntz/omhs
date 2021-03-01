
import io.netty.handler.codec.http._
import io.netty.util.CharsetUtil

import scala.collection.JavaConverters._

object RequestUtils {

  def formatParams(request: HttpRequest): StringBuilder = {
    val responseData = new StringBuilder
    val queryStringDecoder = new QueryStringDecoder(request.uri)
    val params = queryStringDecoder.parameters
    if (!params.isEmpty) {
      params.entrySet().asScala.map(p => (p.getKey, p.getValue))
        .map { case (k, vs) =>
          vs.asScala.map { v =>
            responseData.append("Parameter: ")
              .append(k.toUpperCase)
              .append(" = ")
              .append(v.toUpperCase)
              .append("\r\n")
          }
        }
      responseData.append("\r\n")
    }
    responseData
  }

  def formatBody(httpContent: HttpContent): StringBuilder = {
    val responseData = new StringBuilder
    val content = httpContent.content
    if (content.isReadable) {
      responseData.append(content.toString(CharsetUtil.UTF_8).toUpperCase)
      responseData.append("\r\n")
    }
    responseData
  }

  def evaluateDecoderResult(o: HttpObject): StringBuilder = {
    val responseData = new StringBuilder
    val result = o.decoderResult
    if (!result.isSuccess) {
      responseData.append("..Decoder Failure: ")
      responseData.append(result.cause)
      responseData.append("\r\n")
    }
    responseData
  }

  def prepareLastResponse(request: HttpRequest, trailer: LastHttpContent): StringBuilder = {
    val responseData = new StringBuilder
    responseData.append("Good Bye!\r\n")
    if (!trailer.trailingHeaders.isEmpty) {
      responseData.append("\r\n")
      trailer.trailingHeaders.names.asScala.map { name =>
        trailer.trailingHeaders.getAll(name).asScala.map { value =>
          responseData.append("P.S. Trailing Header: ")
          responseData.append(name).append(" = ").append(value).append("\r\n")
        }
      }
      responseData.append("\r\n")
    }
    responseData
  }
}
