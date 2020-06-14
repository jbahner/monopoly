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
import monopoly.controller.gamestate.GameStatus
import monopoly.controller.gamestate.GameStatus.GameStatus
import monopoly.view.{Gui, IUi, Tui}
import play.api.libs.json.{JsObject, Json}

import scala.concurrent._
import scala.concurrent.duration._
import scala.io.StdIn.readLine

object MainComponentServer {

    val HTTP_RESPONSE_WAIT_TIME = 1000

    // Akka Inits
    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    private val BOARD_COMPONENT_URL = "http://myboard:8082"
    val injector: Injector = Guice.createInjector(new MonopolyModule)
    val controller: IController = injector.getInstance(classOf[IController])


    val resourcesPath = getClass.getResource("/save-game.json")
    println(resourcesPath.getPath)

    controller.setUp()
    val tui: IUi = new Tui(controller)

    // val gui: IUi = new Gui(controller)


    def main(args: Array[String]): Unit = {


        val requestHandler: HttpRequest => HttpResponse = {

            case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/html(UTF-8)`,
                    "<html><body>Hello world!</body></html>"))

            case HttpRequest(GET, Uri.Path("/health"), _, _, _) =>
                HttpResponse(entity = "Health is feeling good!")
        }

        controller.publish(new UpdateInfo)

        var input = readLine()
        while (input != "quit") {
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
                HTTP_RESPONSE_WAIT_TIME seconds)

        getStringFromResponse(response)
    }

    def rollDice(board: String): (String, Int, Int) = {
        val httpResonse: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(POST,
                        uri = BOARD_COMPONENT_URL + "/board/roll-dice",
                        entity = board)),
                HTTP_RESPONSE_WAIT_TIME seconds)

        val responseString = getStringFromResponse(httpResonse)
        val responseJson = Json.parse(responseString).as[JsObject]

        (responseString,
            (responseJson \ "d1").as[Int],
            (responseJson \ "d2").as[Int])
    }

    def playerWalk(board: String): (String) = {
        val httpResonse: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(POST,
                        uri = BOARD_COMPONENT_URL + "/board/player-walk",
                        entity = board)),
                HTTP_RESPONSE_WAIT_TIME seconds)

        val responseString = getStringFromResponse(httpResonse)

        responseString
    }

    def currentPlayerPaysRent(board: String): (String) = {
        val httpResonse: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(POST,
                        uri = BOARD_COMPONENT_URL + "/board/pay-rent",
                        entity = board)),
                HTTP_RESPONSE_WAIT_TIME seconds)

        val responseString = getStringFromResponse(httpResonse)

        responseString
    }

    def canCurrentPlayerBuildOnStreet(board: String, streetName: String): Boolean = {
        val boardJson = Json.parse(board).as[JsObject]
            .+("streetNameParam", Json.toJson(streetName))

        val httpResonse: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = BOARD_COMPONENT_URL + "/board/can-current-player-build",
                        entity = boardJson.toString())),
                HTTP_RESPONSE_WAIT_TIME seconds)

        val responseString = getStringFromResponse(httpResonse)

        responseString.toBoolean
    }

    def getAmountOfHousesOnStreet(board: String, streetName: String): Int = {
        val boardJson = Json.parse(board).as[JsObject]
            .+("streetNameParam", Json.toJson(streetName))

        val httpResonse: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = BOARD_COMPONENT_URL + "/board/amount-of-houses",
                        entity = boardJson.toString())),
                HTTP_RESPONSE_WAIT_TIME seconds)

        val responseString = getStringFromResponse(httpResonse)

        responseString.toInt
    }

    def getCurrentPlayerMoney(board: String): Int = {
        val boardJson = board

        val httpResonse: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = BOARD_COMPONENT_URL + "/board/current-player-money",
                        entity = boardJson.toString())),
                HTTP_RESPONSE_WAIT_TIME seconds)

        val responseString = getStringFromResponse(httpResonse)

        responseString.toInt
    }

    def getCurrentPlayerName(board: String): String = {
        val boardJson = board

        val httpResonse: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = BOARD_COMPONENT_URL + "/board/current-player-name",
                        entity = boardJson.toString())),
                HTTP_RESPONSE_WAIT_TIME seconds)

        val responseString = getStringFromResponse(httpResonse)

        responseString
    }

    def getHouseCost(board: String, streetName: String): Int = {
        val boardJson = Json.parse(board).as[JsObject]
            .+("streetNameParam", Json.toJson(streetName))

        val httpResonse: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = BOARD_COMPONENT_URL + "/board/get-house-cost",
                        entity = boardJson.toString())),
                HTTP_RESPONSE_WAIT_TIME seconds)

        val responseString = getStringFromResponse(httpResonse)

        responseString.toInt
    }

    def buildHouses(board: String, streetName: String, amount: Int): (String) = {
        val boardJson = Json.parse(board).as[JsObject]
            .+("streetNameParam", Json.toJson(streetName))
            .+("houseAmount", Json.toJson(amount))

        val httpResonse: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(POST,
                        uri = BOARD_COMPONENT_URL + "/board/build-houses",
                        entity = boardJson.toString())),
                HTTP_RESPONSE_WAIT_TIME seconds)

        val responseString = getStringFromResponse(httpResonse)

        responseString
    }

    def getOwnersName(board: String, streetName: String): String = {
        val boardJson = Json.parse(board).as[JsObject]
            .+("streetNameParam", Json.toJson(streetName))

        val httpResonse: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = BOARD_COMPONENT_URL + "/board/get-owners-name",
                        entity = boardJson.toString())),
                HTTP_RESPONSE_WAIT_TIME seconds)

        val responseString = getStringFromResponse(httpResonse)

        responseString
    }

    def getCurrentField(board: String): String = {
        val boardJson = board

        val httpResonse: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = BOARD_COMPONENT_URL + "/board/current-field",
                        entity = boardJson)),
                HTTP_RESPONSE_WAIT_TIME seconds)

        val responseString = getStringFromResponse(httpResonse)

        responseString
    }

    def getCurrentFieldName(board: String): String = {
        val currentField = getCurrentField(board)

        val json = Json.parse(currentField).as[JsObject]

        (json \ "field" \ "name").get.as[String]
    }

    // TODO owner of field is not displayed in Gui
    def getCurrentFieldType(board: String): String = {
        val currentField = getCurrentField(board)

        val json = Json.parse(currentField).as[JsObject]

        (json \ "field" \ "type").get.as[String]
    }

    def getCurrentPlayBoughtStreetsCount(board: String): Int = {
        val boardJson = Json.parse(board).as[JsObject]

        val httpResonse: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = BOARD_COMPONENT_URL + "/board/current-player-bought-streets-count",
                        entity = boardJson.toString())),
                HTTP_RESPONSE_WAIT_TIME seconds)

        val responseString = getStringFromResponse(httpResonse)

        responseString.toInt
    }

    def getCurrentFieldRent(board: String): Int = {
        val boardJson = board

        val httpResonse: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = BOARD_COMPONENT_URL + "/board/current-field-rent",
                        entity = boardJson)),
                HTTP_RESPONSE_WAIT_TIME seconds)

        val responseString = getStringFromResponse(httpResonse)

        responseString.toInt
    }

    def canCurrentPlayerBuyHouses(board: String): Boolean = {
        val boardJson = Json.parse(board).as[JsObject]

        val httpResonse: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = BOARD_COMPONENT_URL + "/board/can-buy-houses",
                        entity = boardJson.toString())),
                HTTP_RESPONSE_WAIT_TIME seconds)

        val responseString = getStringFromResponse(httpResonse)

        responseString.toBoolean
    }

    def getPossibleBuildPlacesToString(board: String): String = {
        val boardJson = Json.parse(board).as[JsObject]

        val httpResonse: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = BOARD_COMPONENT_URL + "/board/possible-build-places",
                        entity = boardJson.toString())),
                HTTP_RESPONSE_WAIT_TIME seconds)

        val responseString = getStringFromResponse(httpResonse)

        responseString
    }

    def getFieldGameState(board: String): GameStatus = {
        val boardJson = Json.parse(board).as[JsObject]

        val httpResonse: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = BOARD_COMPONENT_URL + "/board/get-field-game-state",
                        entity = boardJson.toString())),
                HTTP_RESPONSE_WAIT_TIME seconds)

        val responseString = getStringFromResponse(httpResonse)

        GameStatus.revMap(responseString)
    }

    def getCurrentPlayerDetails(board: String): String = {
        val boardJson = board

        val httpResonse: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = BOARD_COMPONENT_URL + "/board/current-player-details",
                        entity = boardJson)),
                HTTP_RESPONSE_WAIT_TIME seconds)

        val responseString = getStringFromResponse(httpResonse)

        responseString
    }

    def getCurrentPlayerBoughtFieldnames(board: String): List[String] = {
        val boardJson = board

        val httpResonse: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = BOARD_COMPONENT_URL + "/board/current-player-bought-fieldnames",
                        entity = boardJson)),
                HTTP_RESPONSE_WAIT_TIME seconds)

        val responseString = getStringFromResponse(httpResonse)

        val boughtStreets = responseString.split(";")

        boughtStreets.toList
    }

    def saveCurrentBoard(board: String): Unit = {
        val boardJson = board

        val httpResonse: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(POST,
                        uri = BOARD_COMPONENT_URL + "/board/save-current-board",
                        entity = boardJson)),
                HTTP_RESPONSE_WAIT_TIME seconds)

        val responseString = getStringFromResponse(httpResonse)

        println("Saving in Database was -> " + responseString)
    }

    def loadCurrentBoard(): String = {

        val httpResonse: HttpResponse =
            Await.result(
                Http().singleRequest(
                    HttpRequest(GET,
                        uri = BOARD_COMPONENT_URL + "/board/load-current-board")),
                HTTP_RESPONSE_WAIT_TIME seconds)

        val responseString = getStringFromResponse(httpResonse)

        responseString
    }


    def getStringFromResponse(input: HttpResponse): String = {
        Unmarshal(input).to[String].toString.replace("FulfilledFuture(", "").replace(")", "")
    }


}
