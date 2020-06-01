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

    def requestCurrentPlayer(board: String): Option[String] = {
        val response: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = BOARD_COMPONENT_URL + "/board/current-player",
                        entity = board)),
                1 seconds)

        Option.apply(getStringFromResponse(response))
    }

    def requestGivePlayerMoney(board: String, playerName: String, moneyToGive: Int): Option[String] = {
        val response: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(POST,
                        uri = BOARD_COMPONENT_URL + "/board/give-player-money",
                        entity = board
                            .+("recievingPlayer", Json.toJson(playerName))
                                .+("moneyToGive", Json.toJson(moneyToGive.toString))
                    )),
                1 seconds)

        // TODO atm
        Option.apply(getStringFromResponse(response))
    }

    def requestCurrentFieldName(board: String): String = {
        val response: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = BOARD_COMPONENT_URL + "/board/current-field-name",
                        entity = board)),
                1 seconds)

        getStringFromResponse(response)
    }

    // TODO to be tested
    def requestCurrentPlayerName(board: String): String = {
        val currentPlayer =  requestCurrentPlayer(board).get
        (Json.parse(currentPlayer).as[JsObject] \ "name").toString
    }

    // TODO to be tested
    def requestCurrentPlayerMoney(board: String): String = {
        val currentPlayer =  requestCurrentPlayer(board).get
        (Json.parse(currentPlayer).as[JsObject] \ "money").toString
    }

    def requestCurrentFieldPrice(board: String): String = {
        val response: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = BOARD_COMPONENT_URL + "/board/current-field-price",
                        entity = board)),
                1 seconds)

        getStringFromResponse(response)
    }

    def requestParsingBoardFromJson(json: String): String = {
        val response: HttpResponse =
        Await.result(
            Http().singleRequest(
                HttpRequest(GET,
                    uri = BOARD_COMPONENT_URL + "/board/parse-from-json",
                    entity = json)),
            1 seconds)

        getStringFromResponse(response)
    }


    def getStringFromResponse(input: HttpResponse): String = {
        Unmarshal(input).to[String].toString.replace("FulfilledFuture(", "").replace(")", "")
    }


}
