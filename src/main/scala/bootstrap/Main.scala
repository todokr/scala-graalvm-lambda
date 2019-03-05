package bootstrap

import spray.json._
import scalaj.http._

object Main {

  def main(args: Array[String]): Unit = {
    val runtime = System.getenv("AWS_LAMBDA_RUNTIME_API")

    while (true) {
      val HttpResponse(body, _, headers) = Http(s"http://$runtime/2018-06-01/runtime/invocation/next").asString
      val requestId = headers("lambda-runtime-aws-request-id").head

      try {
        val name = body.parseJson.asJsObject.getFields("name").headOption match {
          case Some(JsString(value)) => value
          case _ => throw new Exception("unable to parse json")
        }
        val responseJson = JsObject("message" -> JsString(s"hello, $name!")).compactPrint
        Http(s"http://$runtime/2018-06-01/runtime/invocation/$requestId/response").postData(responseJson).asString
      } catch {
        case e: Exception =>
          Http(s"http://$runtime/2018-06-01/runtime/invocation/$requestId/error").postData(e.getMessage).asString
      }
    }
  }
}
