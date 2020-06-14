import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse, _}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import modelComponent.boardComponent.boardBaseImpl.Board
import modelComponent.fieldComponent.IBuyable
import modelComponent.persistence.IDaoBoard
import modelComponent.persistence.relational.RelationalAdapter
import modelComponent.util.RentContext
import play.api.libs.json.{JsObject, Json}

object BoardComponentServer {

    // Akka Inits
    private implicit val system: ActorSystem = ActorSystem("my-system")
    private implicit val materializer: ActorMaterializer = ActorMaterializer()
    private val database: IDaoBoard = RelationalAdapter

    private val PATH_ROOT =                                 "/"
    private val PATH_NEXT_PLAYER =                          "/board/next-player"
    private val PATH_ROLL_DICE =                            "/board/roll-dice"
    private val PATH_PLAYER_WALK =                          "/board/player-walk"
    private val PATH_PAY_RENT =                             "/board/pay-rent"
    private val PATH_CAN_CURRENT_PLAYER_BUILT =             "/board/can-current-player-build"
    private val PATH_AMOUNT_OF_HOUSES =                     "/board/amount-of-houses"
    private val PATH_CURRENT_PLAYER_MONEY =                 "/board/current-player-money"
    private val PATH_GET_HOUSE_COST =                       "/board/get-house-cost"
    private val PATH_BUILD_HOUSES =                         "/board/build-houses"
    private val PATH_GET_OWNERS_NAME =                      "/board/get-owners-name"
    private val PATH_CURRENT_FIELD =                        "/board/current-field"
    private val PATH_CURRENT_PLAYER_BOUGHT_STREET_COUNT =   "/board/current-player-bought-streets-count"
    private val PATH_CURRENT_PLAYER_NAME =                  "/board/current-player-name"
    private val PATH_CURRENT_FIELD_RENT =                   "/board/current-field-rent"
    private val PATH_CURRENT_PLAYER_BUY_HOUSES =            "/board/can-buy-houses"
    private val PATH_POSSIBLE_BUILD_PLACES =                "/board/possible-build-places"
    private val PATH_GET_FIELD_GAMESTATE =                  "/board/get-field-game-state"
    private val PATH_CURRENT_PLAYER_DETAILS =               "/board/current-player-details"
    private val PATH_CURRENT_PLAYER_BOUGHT_FIELDNAMES =     "/board/current-player-bought-fieldnames"
    private val PATH_SAVE_CURRENT_BOARD =                   "/board/save-current-board"
    private val PATH_LOAD_CURRENT_BOARD =                   "/board/load-current-board"

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
                println("Called Route: " + PATH_GET_HOUSE_COST)

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


            case HttpRequest(GET, Uri.Path(PATH_GET_OWNERS_NAME), _, entity, _) =>
                println("Called Route: " + PATH_GET_OWNERS_NAME)


                val requestJsonBoardAsString = entityToJson(entity)

                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val board = Board.fromSimplefiedJson(json)
                val streetName = (json \ "streetNameParam").get.as[String]
                val ownersName = board.getBuyer(board.getFieldByName(streetName).get.asInstanceOf[IBuyable]).get.getName

                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    ownersName))


            case HttpRequest(GET, Uri.Path(PATH_CURRENT_FIELD), _, entity, _) =>
                println("Called Route: " + PATH_CURRENT_FIELD)

                val requestJsonBoardAsString = entityToJson(entity)

                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val board = Board.fromSimplefiedJson(json)

                val currentField = board.getCurrentField().toJson()

                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    currentField.toString()))


            case HttpRequest(GET, Uri.Path(PATH_CURRENT_PLAYER_BOUGHT_STREET_COUNT), _, entity, _) =>
                println("Called Route: " + PATH_CURRENT_PLAYER_BOUGHT_STREET_COUNT)

                val requestJsonBoardAsString = entityToJson(entity)

                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val board = Board.fromSimplefiedJson(json)

                val boughtSize = board.getCurrentPlayer().get.getBought.size

                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    boughtSize.toString))


            case HttpRequest(GET, Uri.Path(PATH_CURRENT_PLAYER_NAME), _, entity, _) =>
                println("Called Route: " + PATH_CURRENT_PLAYER_NAME)

                val requestJsonBoardAsString = entityToJson(entity)

                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val board = Board.fromSimplefiedJson(json)

                val playerName = board.getCurrentPlayer().get.getName

                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    playerName))


            case HttpRequest(GET, Uri.Path(PATH_CURRENT_FIELD_RENT), _, entity, _) =>
                println("Called Route: " + PATH_CURRENT_FIELD_RENT)

                val requestJsonBoardAsString = entityToJson(entity)

                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val board = Board.fromSimplefiedJson(json)

                val currentRent = RentContext.rentStrategy.executeStrategy(board, board.getCurrentField().asInstanceOf[IBuyable])

                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    currentRent.toString))

            case HttpRequest(GET, Uri.Path(PATH_CURRENT_PLAYER_BUY_HOUSES), _, entity, _) =>
                println("Called Route: " + PATH_CURRENT_PLAYER_BUY_HOUSES)

                val requestJsonBoardAsString = entityToJson(entity)

                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val board = Board.fromSimplefiedJson(json)

                val boolFlag = board.canCurrentPlayerBuyHouses()

                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    boolFlag.toString))

            case HttpRequest(GET, Uri.Path(PATH_POSSIBLE_BUILD_PLACES), _, entity, _) =>
                println("Called Route: " + PATH_POSSIBLE_BUILD_PLACES)

                val requestJsonBoardAsString = entityToJson(entity)

                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val board = Board.fromSimplefiedJson(json)

                val placesToString = board.getPossibleBuildPlacesToString()

                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    placesToString))


            case HttpRequest(GET, Uri.Path(PATH_GET_FIELD_GAMESTATE), _, entity, _) =>
                println("Called Route: " + PATH_GET_FIELD_GAMESTATE)

                val requestJsonBoardAsString = entityToJson(entity)

                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val board = Board.fromSimplefiedJson(json)

                val newGamestateString = board.getCurrentField().action(board.getCurrentPlayer().get)

                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    newGamestateString))


            case HttpRequest(GET, Uri.Path(PATH_CURRENT_PLAYER_DETAILS), _, entity, _) =>
                println("Called Route: " + PATH_CURRENT_PLAYER_DETAILS)

                val requestJsonBoardAsString = entityToJson(entity)

                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val board = Board.fromSimplefiedJson(json)

                val details = board.getCurrentPlayer().get.getDetails

                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    details))

            case HttpRequest(GET, Uri.Path(PATH_CURRENT_PLAYER_BOUGHT_FIELDNAMES), _, entity, _) =>
                println("Called Route: " + PATH_CURRENT_PLAYER_BOUGHT_FIELDNAMES)


                val requestJsonBoardAsString = entityToJson(entity)

                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val board = Board.fromSimplefiedJson(json)

                var answer = ""

                board.getCurrentPlayer().get.getBought.toSeq.sortBy(_.getName)
                    .foreach(bought => answer = answer.concat(bought.getName + ";"))

                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    answer))


            case HttpRequest(POST, Uri.Path(PATH_SAVE_CURRENT_BOARD), _, entity, _) =>
                println("Called Route: " + PATH_SAVE_CURRENT_BOARD)


                val requestJsonBoardAsString = entityToJson(entity)

                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val board = Board.fromSimplefiedJson(json)

                val saveAnser = database.saveBoard(board)


                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    saveAnser.toString))


            case HttpRequest(GET, Uri.Path(PATH_LOAD_CURRENT_BOARD), _, _, _) =>
                println("Called Route: " + PATH_LOAD_CURRENT_BOARD)


                val board = database.loadBoard()


                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    board.toJson().toString()))



            //            case HttpRequest(GET, Uri.Path("/crash"), _, _, _) =>
            //                sys.error("BOOM!")
            //
            //            case r: HttpRequest =>
            //                r.discardEntityBytes() // important to drain incoming HTTP Entity stream
            //                HttpResponse(404, entity = "Unknown resource!")



        }

//        val bindingFuture = Http().bindAndHandleSync(requestHandler, "0.0.0.0", 8082)
        val bindingFuture = Http().bindAndHandleSync(requestHandler, "localhost", 8082)

        println("BoardComponentServer is running and waiting for requests.")


    }


    def entityToJson(entity: RequestEntity): String = {
        val entityString = Unmarshal(entity).to[String].toString
        entityString.replace("FulfilledFuture(", "").replace(")", "")
    }

}
