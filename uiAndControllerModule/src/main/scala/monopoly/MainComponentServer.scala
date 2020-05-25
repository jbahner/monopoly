package monopoly

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.google.inject.{Guice, Injector}
import monopoly.controller.IController
import monopoly.controller.controllerBaseImpl.UpdateInfo
import monopoly.view.{Gui, IUi, Tui}

import scala.concurrent.Future
import scala.io.StdIn.readLine

object MainComponentServer {

    // Akka Inits
    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val injector: Injector = Guice.createInjector(new MonopolyModule)
    val controller: IController = injector.getInstance(classOf[IController])
    controller.setUp
    val tui: IUi = new Tui(controller)
    val gui: IUi = new Gui(controller)



    def main(args: Array[String]): Unit = {

        val requestHandler: HttpRequest => HttpResponse = {

            case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/html(UTF-8)`,
                    "<html><body>Hello world!</body></html>"))

            case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
                HttpResponse(entity = "PONG!")
        }

        val bindingFuture = Http().bindAndHandleSync(requestHandler, "localhost", 8081)

        controller.publish(new UpdateInfo)

        var input = readLine()
        while (input != "q") {
            input = readLine()
            tui.processInput(input)
        }


        // Server Shutdown
        println("Server shutting down")
        controller.shutdown()
    }

    def requestNextPlayer(): Unit = {
        val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "http://localhost:8082/board/next-player"))
        responseFuture.onComplete(
            response => println(Unmarshal(response.get).to[String])
        )
    }


}
