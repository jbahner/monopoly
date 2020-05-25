import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse, _}
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import boardComponent.boardBaseImpl.Board

object BoardComponentServer {

    def main(args: Array[String]): Unit = {

        // Akka Inits
        implicit val system = ActorSystem("my-system")
        implicit val materializer = ActorMaterializer()


        val requestHandler: HttpRequest => HttpResponse = {

            case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/html(UTF-8)`,
                    "<html><body>Hello world!</body></html>"))

            case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
                HttpResponse(entity = "PONG!")

            case HttpRequest(GET, Uri.Path("/crash"), _, _, _) =>
                sys.error("BOOM!")

            case r: HttpRequest =>
                r.discardEntityBytes() // important to drain incoming HTTP Entity stream
                HttpResponse(404, entity = "Unknown resource!")
        }

        val bindingFuture = Http().bindAndHandleSync(requestHandler, "localhost", 8082)

        println("BoardComponentServer is running and waiting for requests.")


    }

}
