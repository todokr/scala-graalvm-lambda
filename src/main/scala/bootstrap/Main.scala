package bootstrap

import play.api.libs.json._
import scalaj.http._

object Main {
  
  def main(args: Array[String]): Unit = {
    val handler  = System.getenv("_HANDLER")
    val taskRoot = System.getenv("LAMBDA_TASK_ROOT")
    val runtime  = System.getenv("AWS_LAMBDA_RUNTIME_API")

    try {
      println(s"handler: $handler")
      println(s"taskRoot: $taskRoot")
      println(s"runtime: $runtime")
    } catch {
      case e: Exception =>
        val message = Json.obj("errorMessage" -> e.getMessage, "errorType" -> e.getClass.getName).toString
        Http(s"http://$runtime/2018-06-01/runtime/init/error").postData(message).asString
    }

    while (true) {
      val HttpResponse(jsResult, _, headers) =
        Http(s"http://$runtime/2018-06-01/runtime/invocation/next").execute(x => Json.parse(x).validate[Request])

      val requestId = headers.get("lambda-runtime-aws-request-id")
      val traceId = headers.get("lambda-runtime-trace-id")

      println(s"body: $jsResult")
      println(s"headers: $headers")

      try {
        val name = jsResult.get.name
        val result = Json.obj("message" -> s"Hello, $name!").toString
        Http(s"http://$runtime/2018-06-01/runtime/invocation/$requestId/response").postData(result).asString
      } catch {
        case e: Exception =>
          val message = Json.obj("errorMessage" -> e.getMessage, "errorType" -> e.getClass.getName).toString
          Http(s"http://$runtime/2018-06-01/runtime/invocation/$requestId/error").postData(message).asString
      }
    }
  }
}

case class Request(name: String)
