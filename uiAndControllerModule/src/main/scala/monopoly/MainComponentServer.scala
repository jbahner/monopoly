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
import play.api.libs.json.{JsObject, Json}

import scala.concurrent._
import scala.concurrent.duration._
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

    private val BOARD_COMPONENT_URL = "http://localhost:8082"


    def main(args: Array[String]): Unit = {

        val requestHandler: HttpRequest => HttpResponse = {

            case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/html(UTF-8)`,
                    "<html><body>Hello world!</body></html>"))

            case HttpRequest(GET, Uri.Path("/health"), _, _, _) =>
                HttpResponse(entity = "Health is feeling good!")
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

    def requestNextPlayer(board: String): String = {
        val response: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(POST,
                        uri = BOARD_COMPONENT_URL + "/board/next-player",
                        entity = board)),
                1 seconds)

        getStringFromResponse(response)
    }

    def rollDice(board: String): (String, Int, Int) = {
        val httpResonse: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(POST,
                        uri = BOARD_COMPONENT_URL + "/board/roll-dice",
                        entity = board)),
                1 seconds)

        val responseString = getStringFromResponse(httpResonse)
        val responseJson = Json.parse(responseString).as[JsObject]

        (responseString,
            (responseJson \ "d1").as[Int],
            (responseJson \ "d2").as[Int])
    }


    def getStringFromResponse(input: HttpResponse): String = {
        Unmarshal(input).to[String].toString.replace("FulfilledFuture(", "").replace(")", "")
    }


}
