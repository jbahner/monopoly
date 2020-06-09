import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse, _}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import modelComponent.boardComponent.boardBaseImpl.Board
import play.api.libs.json.{JsObject, Json}

object BoardComponentServer {

    // Akka Inits
    private implicit val system: ActorSystem = ActorSystem("my-system")
    private implicit val materializer: ActorMaterializer = ActorMaterializer()

    private val PATH_ROOT = "/"
    private val PATH_NEXT_PLAYER = "/board/next-player"
    private val PATH_ROLL_DICE = "/board/roll-dice"
    private val PATH_PLAYER_WALK = "/board/player-walk"
    private val PATH_PAY_RENT = "/board/pay-rent"
    private val PATH_CAN_CURRENT_PLAYER_BUILT = "/board/can-current-player-build"
    private val PATH_AMOUNT_OF_HOUSES = "/board/amount-of-houses"
    private val PATH_CURRENT_PLAYER_MONEY = "/board/current-player-money"
    private val PATH_GET_HOUSE_COST = "/board/get-house-cost"
    private val PATH_BUILD_HOUSES = "/board/build-houses"
    //    private val PATH_ROOT = "/"
    //    private val PATH_ROOT = "/"
    //    private val PATH_ROOT = "/"

    def main(args: Array[String]): Unit = {

        val requestHandler: HttpRequest => HttpResponse = {


            case HttpRequest(GET, Uri.Path(PATH_ROOT), _, _, _) =>
                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/html(UTF-8)`,
                    "<html><body>Hello world!</body></html>"))

            case HttpRequest(POST, Uri.Path(PATH_NEXT_PLAYER), _, entity, _) =>
                println("Called Route: " + PATH_NEXT_PLAYER)

                val requestJsonBoardAsString = entityToJson(entity)

                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val board = Board.fromSimplefiedJson(json)

                val retBoard = board.nextPlayerTurn()

                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    retBoard.toJson().toString()))

            case HttpRequest(POST, Uri.Path(PATH_ROLL_DICE), _, entity, _) =>
                println("Called Route: " + PATH_ROLL_DICE)

                val requestJsonBoardAsString = entityToJson(entity)

                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val board = Board.fromSimplefiedJson(json)
                val (d1, d2) = board.rollDice()

                val returnBoardJson = board.toJson()
                    .+("d1", Json.toJson(d1))
                    .+("d2", Json.toJson(d2))


                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    returnBoardJson.toString()))

            case HttpRequest(POST, Uri.Path(PATH_PLAYER_WALK), _, entity, _) =>
                println("Called Route: " + PATH_PLAYER_WALK)
                try {

                    val requestJsonBoardAsString = entityToJson(entity)

                    val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                    val board = Board.fromSimplefiedJson(json)
                    val newBoard = board.currentPlayerWalk()

                    val returnBoardJson = newBoard.toJson()

                    HttpResponse(entity = HttpEntity(
                        ContentTypes.`text/plain(UTF-8)`,
                        returnBoardJson.toString()))
                }
                catch {
                    case e: Exception => e.printStackTrace()
                        throw e
                }

            case HttpRequest(POST, Uri.Path(PATH_PAY_RENT), _, entity, _) =>
                println("Called Route: " + PATH_PAY_RENT)

                val requestJsonBoardAsString = entityToJson(entity)

                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val board = Board.fromSimplefiedJson(json)
                val newBoard = board.currentPlayerPaysRent()

                val returnBoardJson = newBoard.toJson()

                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    returnBoardJson.toString()))


            case HttpRequest(GET, Uri.Path(PATH_CAN_CURRENT_PLAYER_BUILT), _, entity, _) =>
                println("Called Route: " + PATH_CAN_CURRENT_PLAYER_BUILT)

                val requestJsonBoardAsString = entityToJson(entity)

                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val board = Board.fromSimplefiedJson(json)
                val steetName = (json \ "streetNameParam").get.as[String]
                val returnBoolean = board.canCurrentPlayerBuildOnStreet(steetName)

                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    returnBoolean.toString))


            case HttpRequest(GET, Uri.Path(PATH_AMOUNT_OF_HOUSES), _, entity, _) =>
                println("Called Route: " + PATH_AMOUNT_OF_HOUSES)

                val requestJsonBoardAsString = entityToJson(entity)

                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val board = Board.fromSimplefiedJson(json)
                val steetName = (json \ "streetNameParam").get.as[String]
                val returnNumber = board.getAmountOfHousesOnStreet(steetName)

                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    returnNumber.toString))


            case HttpRequest(GET, Uri.Path(PATH_CURRENT_PLAYER_MONEY), _, entity, _) =>
                println("Called Route: " + PATH_CURRENT_PLAYER_MONEY)

                val requestJsonBoardAsString = entityToJson(entity)

                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val board = Board.fromSimplefiedJson(json)
                val returnNumber = board.getCurrentPlayerMoney()

                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    returnNumber.toString))

            case HttpRequest(GET, Uri.Path(PATH_GET_HOUSE_COST), _, entity, _) =>
                println("Called Route: " + PATH_CURRENT_PLAYER_MONEY)

                val requestJsonBoardAsString = entityToJson(entity)

                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val board = Board.fromSimplefiedJson(json)
                val steetName = (json \ "streetNameParam").get.as[String]
                val returnNumber = board.getHouseCost(steetName)

                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    returnNumber.toString))


            case HttpRequest(POST, Uri.Path(PATH_BUILD_HOUSES), _, entity, _) =>
                println("Called Route: " + PATH_BUILD_HOUSES)

                val requestJsonBoardAsString = entityToJson(entity)

                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val board = Board.fromSimplefiedJson(json)
                val steetName = (json \ "streetNameParam").get.as[String]
                val amount = (json \ "houseAmount").get.as[Int]
                val returnBoard = board.buildHouses(steetName, amount)

                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    returnBoard.toJson().toString))




            //            case HttpRequest(GET, Uri.Path("/crash"), _, _, _) =>
            //                sys.error("BOOM!")
            //
            //            case r: HttpRequest =>
            //                r.discardEntityBytes() // important to drain incoming HTTP Entity stream
            //                HttpResponse(404, entity = "Unknown resource!")
        }

        val bindingFuture = Http().bindAndHandleSync(requestHandler, "localhost", 8082)

        println("BoardComponentServer is running and waiting for requests.")


    }


    def entityToJson(entity: RequestEntity): String = {
        val entityString = Unmarshal(entity).to[String].toString
        entityString.replace("FulfilledFuture(", "").replace(")", "")
    }

}
