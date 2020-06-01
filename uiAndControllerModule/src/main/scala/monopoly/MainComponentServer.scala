package monopoly

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.google.inject.{Guice, Injector}
import model.gamestate.GameStatus
import model.gamestate.GameStatus.GameStatus
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
                        uri = "http://localhost:8082/board/next-player",
                        entity = board)),
                1 seconds)

        getStringFromResponse(response)
    }

    def requestCurrentPlayer(board: String): Option[String] = {
        val response: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = "http://localhost:8082/board/current-player",
                        entity = board)),
                1 seconds)

        Option.apply(getStringFromResponse(response))
    }

    def requestGivePlayerMoney(board: String, playerName: String, moneyToGive: Int): String = {
        val response: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(POST,
                        uri = "http://localhost:8082/board/give-player-money",
                        entity = board
                            .+("recievingPlayer", Json.toJson(playerName))
                            .+("moneyToGive", Json.toJson(moneyToGive.toString))
                    )),
                1 seconds)

        getStringFromResponse(response)
    }

    def requestCurrentFieldName(board: String): String = {
        val response: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = "http://localhost:8082/board/current-field-name",
                        entity = board)),
                1 seconds)

        getStringFromResponse(response)
    }

    // TODO to be tested
    def requestCurrentPlayerName(board: String): String = {
        val currentPlayer = requestCurrentPlayer(board).get
        println("CurrentPlayer")
        println(currentPlayer)
        (Json.parse(currentPlayer).as[JsObject] \ "name").toString
    }

    // TODO to be tested
    def requestCurrentPlayerMoney(board: String): Int = {
        val currentPlayer = requestCurrentPlayer(board).get
        (Json.parse(currentPlayer).as[JsObject] \ "money").as[Int]
    }

    def requestCurrentFieldPrice(board: String): String = {
        val response: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = "http://localhost:8082/board/current-field-price",
                        entity = board)),
                1 seconds)

        getStringFromResponse(response)
    }

    def requestParsingBoardFromJson(json: String): String = {
        val response: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = "http://localhost:8082/board/parse-from-json",
                        entity = json)),
                1 seconds)

        getStringFromResponse(response)
    }

    def requestBuyHouses(board: String, streetName: String, amount: Int): String = {

        val response: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(POST,
                        uri = "http://localhost:8082/board/buy-houses",
                        entity = board
                            .+("streetName", Json.toJson(streetName))
                            .+("amount", Json.toJson(amount.toString)))),
                1 seconds)



        getStringFromResponse(response)
    }

    def requestCurrentPlayerWalk(board: String, amount: Int): (String, Boolean, GameStatus) = {

        val response: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(POST,
                        uri = "http://localhost:8082/board/walk",
                        entity = board
                            .+("amount", Json.toJson(amount.toString)))),
                1 seconds)



        val responseString = getStringFromResponse(response)

        val json = Json.parse(responseString).as[JsObject]
        val passedGo = (json \ "passedGo").as[Boolean]
        val newGameState = GameStatus.revMap((json \ "newGamestate").toString)

        (responseString, passedGo, newGameState)
    }

    def requestCurrentFieldHouseCost(board: String): Int = {
        val response: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = "http://localhost:8082/board/current-field-house-cost",
                        entity = board)),
                1 seconds)

        getStringFromResponse(response).toInt
    }

    def requestCurrentPlayerBoughtFieldNames(board: String): List[String] = {
        val response: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = "http://localhost:8082/board/current-player-bought-fields",
                        entity = board)),
                1 seconds)

        var nameList = List[String]()

        getStringFromResponse(response).split("\n").foreach(name =>
        nameList :+ name)
        nameList
    }


    def getStringFromResponse(input: HttpResponse): String = {
        Unmarshal(input).to[String].toString.replace("FulfilledFuture(", "").replace(")", "")
    }

    def entityToJson(entity: RequestEntity): String = {
        val entityString = Unmarshal(entity).to[String].toString
        entityString.replace("FulfilledFuture(", "").replace(")", "")
    }


}
