package bootstrap

import play.api.libs.json._
import scalaj.http._

object Main {

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
        val name = (Json.parse(body) \ "name").get
        val responseJson = Json.obj("message" -> s"Hello, $name!").toString

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
}
