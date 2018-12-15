package bootstrap

import play.api.libs.json._
import scalaj.http._

object Main {

  implicit val requestReads: Reads[Request] = Json.reads[Request]

  def main(args: Array[String]): Unit = {
    val runtime  = System.getenv("AWS_LAMBDA_RUNTIME_API")

    try {
      println(s"runtime: $runtime")
    } catch {
      case e: Exception =>
        val message = Json.obj("errorMessage" -> e.getMessage, "errorType" -> e.getClass.getName).toString
        Http(s"http://$runtime/2018-06-01/runtime/init/error").postData(message).asString
    }

    while (true) {
      val HttpResponse(body, _, headers) =
        Http(s"http://$runtime/2018-06-01/runtime/invocation/next").asString
      val requestId = headers("lambda-runtime-aws-request-id").head

      try {
        val request = Json.parse(body).validate[Request].get
        val message = handleRequest(request)
        val responseJson = Json.obj("message" -> message).toString

        Http(s"http://$runtime/2018-06-01/runtime/invocation/$requestId/response").postData(responseJson).asString
      } catch {
        case e: Exception =>
          println(e.getClass.getName)
          println(e.getMessage)
          val message = Json.obj("errorMessage" -> e.getMessage, "errorType" -> e.getClass.getName).toString
          Http(s"http://$runtime/2018-06-01/runtime/invocation/$requestId/error").postData(message).asString
      }
    }
  }

  private def handleRequest(request: Request): String = s"Hello, ${request.name}!"
}

case class Request(name: String)
