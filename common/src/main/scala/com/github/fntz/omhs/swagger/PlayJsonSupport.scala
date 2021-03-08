package com.github.fntz.omhs.swagger

import com.github.fntz.omhs.swagger.DataType.DateType
import play.api.libs.json._

object PlayJsonSupport {

  implicit class JsObjectExt(val js: JsObject) extends AnyVal {
    def rmNulls: JsObject = {
      val tmp = js.fields.filter {
        case (_, JsNull) => false
        case _ => true
      }
      JsObject(tmp)
    }
  }

  def rmNullOrEmpty[A](ow: OWrites[A]): Writes[A] = {
    ow.transform { js: JsValue =>
      js match {
        case JsObject(obj) =>
          JsObject(
            obj.filter { case (_, v) =>
              v match {
                case JsArray(arr) if arr.isEmpty => false
                case JsNull => false
                case _ => true
              }
            }
          ).as[JsValue]

        case x => x
      }
    }
  }

  implicit lazy val externalDocumentationJson = rmNullOrEmpty(Json.writes[ExternalDocumentation])
  implicit lazy val tagsJson = rmNullOrEmpty(Json.writes[Tag])
  implicit lazy val variableJson = rmNullOrEmpty(Json.writes[Variable])
  implicit lazy val serverJson = rmNullOrEmpty(Json.writes[Server])
  implicit lazy val requestBodyJson = rmNullOrEmpty(Json.writes[RequestBody])
  implicit lazy val securityRequirementJson = rmNullOrEmpty(Json.writes[SecurityRequirement])
  implicit lazy val licenseJson = rmNullOrEmpty(Json.writes[License])
  implicit lazy val contactJson = rmNullOrEmpty(Json.writes[Contact])
  implicit lazy val responseJson = new Writes[Response] {
    override def writes(o: Response): JsValue = {
      // todo rewrite plz
      val c = if (o.content.isEmpty) {
        JsObject(Seq.empty)
      } else {
        JsObject(Seq("content" -> Json.toJson(o.content)))
      }
      val h = if (o.headers.isEmpty) {
        JsObject(Seq.empty)
      } else {
        JsObject(Seq("headers" -> Json.toJson(o.headers)))
      }
      Json.obj(
        "description" -> JsString(o.description)
      ) ++ h ++ c
    }
  }
  implicit lazy val infoObjectJson = rmNullOrEmpty(Json.writes[InfoObject])
  implicit lazy val inJson = new Writes[In.Value] {
    override def writes(o: In.Value): JsValue = {
      JsString(o.toString)
    }
  }
  implicit lazy val parameterJson: Writes[Parameter] = new Writes[Parameter] {
    final val queryJson = Json.writes[Query]
    final val headerJson = Json.writes[Header]
    final val cookieJson = Json.writes[Cookie]
    override def writes(o: Parameter): JsValue = {
      val where = Json.obj("in" -> inJson.writes(o.in))
      val required = Json.obj("required" -> JsBoolean(o.required))
      val tmp = o match {
        case p: Path =>
          JsObject(Seq(
            "name" -> JsString(p.name),
            "description" -> p.description.map(JsString).getOrElse(JsNull)
          )).rmNulls ++ dataTypeJson.writes(p.dataType).as[JsObject]
        case h: Header => headerJson.writes(h) - "dataType" ++ dataTypeJson.writes(h.dataType).as[JsObject]
        case c: Cookie => cookieJson.writes(c)
        case q: Query => queryJson.writes(q)
      }
      tmp ++ where ++ required
    }
  }

  implicit lazy val dataTypeJson = new Writes[DataType.Value] {
    override def writes(o: DataType.Value): JsValue = {
      o match {
        case DataType.int64 =>
          JsObject(Seq("type" -> JsString("integer"), "format" -> JsString(o.toString)))
        case DataType.string =>
          JsObject(Seq("type" -> JsString("string")))
        case DataType.uuid =>
          JsObject(Seq("type" -> JsString("string"), "format" -> JsString(o.toString)))
      }
    }
  }

  implicit lazy val operationJson = new Writes[Operation] {
    override def writes(o: Operation): JsValue = {
      val obj = o.responses.map { case (k, v) =>
        k.toString -> responseJson.writes(v)
      }
      def ns(x: Option[String]): JsValue = x.map(JsString).getOrElse(JsNull)
      JsObject(Seq(
        "tags" -> JsArray(o.tags.map(JsString)),
        "summary" -> ns(o.summary),
        "description" -> ns(o.description),
        "externalDocs" -> o.externalDocs.map(externalDocumentationJson.writes).getOrElse(JsNull),
        "operationId" -> ns(o.operationId),
        "parameters" -> JsArray(o.parameters.map(parameterJson.writes)),
        "requestBody" -> o.requestBody.map(requestBodyJson.writes).getOrElse(JsNull),
        "responses" -> JsObject(obj.toSeq),
        "deprecated" -> JsBoolean(o.deprecated)
      )).rmNulls
    }
  }
  implicit lazy val pathItemJson = rmNullOrEmpty(Json.writes[PathItem])
  implicit lazy val swaggerJson = rmNullOrEmpty(Json.writes[Swagger])

  def toJson(swagger: Swagger): String = {
    Json.toJson(swagger).toString()
  }

}
