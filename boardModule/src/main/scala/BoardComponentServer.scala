import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse, _}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import boardComponent.IBoard
import boardComponent.boardBaseImpl._
import model.fieldComponent.IStreet
import model.gamestate.GameStatus
import play.api.libs.json.{JsObject, Json}

object BoardComponentServer {

    // Akka Inits
    private implicit val system: ActorSystem = ActorSystem("my-system")
    private implicit val materializer: ActorMaterializer = ActorMaterializer()

    private val PATH_ROOT = "/"
    private val PATH_BOARD_NEXT_PLAYER = "/board/next-player"
    private val PATH_BOARD_CURRENT_PLAYER = "board/current-player"
    private val PATH_BOARD_GIVE_PLAYER_MONEY = "/board/give-player-money"
    private val PATH_BOARD_CURRENT_PLAYER_BOUGHT_FIELDS = "/board/current-player-bought-fields"
    private val PATH_BOARD_CURRENT_FIELD_NAME = "/board/current-field-name"
    private val PATH_BOARD_CURRENT_FIELD_PRICE = "/board/current-field-price"
    private val PATH_BOARD_CURRENT_FIELD_HOUSE_COST = "/board/current-field-house-cost"
    private val PATH_BOARD_PARSE_FROM_JSON = "/board/parse-from-json"
    private val PATH_BOARD_BUY_HOUSES = "/board/buy-houses"
    private val PATH_BOARD_WALK = "/board/walk"
    //    private val PATH_ROOT = "/"
    //    private val PATH_ROOT = "/"
    //    private val PATH_ROOT = "/"

    def main(args: Array[String]): Unit = {

        val requestHandler: HttpRequest => HttpResponse = {


            case HttpRequest(GET, Uri.Path(PATH_ROOT), _, _, _) =>
                println("Route: \t" + PATH_ROOT + "\t\t has been called")

                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/html(UTF-8)`,
                    "<html><body>Hello world!</body></html>"))

            case HttpRequest(POST, Uri.Path(PATH_BOARD_NEXT_PLAYER), _, entity, _) => {
                println("Route: \t" + PATH_BOARD_NEXT_PLAYER + "\t\t has been called")

                val board = entityToBoard(entity)
                val retBoard = board.nextPlayerTurn()

                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    retBoard.toJson().toString()))
            }

            case HttpRequest(GET, Uri.Path(PATH_BOARD_CURRENT_PLAYER), _, entity, _) => {
                println("Route: \t" + PATH_BOARD_CURRENT_PLAYER + "\t\t has been called")

                val board = entityToBoard(entity)
                val currentPlayer = board.getCurrentPlayer


                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    currentPlayer.toJson().toString()))
            }

            case HttpRequest(POST, Uri.Path(PATH_BOARD_GIVE_PLAYER_MONEY), _, entity, _) => {
                println("Route: \t" + PATH_BOARD_GIVE_PLAYER_MONEY + "\t\t has been called")

                val board = entityToBoard(entity)

                val requestJsonBoardAsString = entityToJson(entity)
                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val moneyToGive = (json \ "moneyToGive").as[Int]
                val recievingPlayer = board.getPlayerByName((json \ "recievingPlayer").toString)

                val retBoard = board.givePlayerMoney(recievingPlayer, moneyToGive)

                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    retBoard.toJson().toString()))
            }

            case HttpRequest(GET, Uri.Path(PATH_BOARD_CURRENT_FIELD_NAME), _, entity, _) => {
                println("Route: \t" + PATH_BOARD_CURRENT_FIELD_NAME + "\t\t has been called")

                val board = entityToBoard(entity)
                val currentFieldName = board.getCurrentPlayer.getCurrentField.getName


                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    currentFieldName))
            }

            case HttpRequest(GET, Uri.Path(PATH_BOARD_CURRENT_FIELD_PRICE), _, entity, _) => {
                println("Route: \t" + PATH_BOARD_CURRENT_FIELD_PRICE + "\t\t has been called")

                val board = entityToBoard(entity)
                val currentFieldPrice = board.getCurrentPlayer.getCurrentField.getPrice


                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    currentFieldPrice.toString))
            }

            case HttpRequest(GET, Uri.Path(PATH_BOARD_PARSE_FROM_JSON), _, entity, _) => {
                println("Route: \t" + PATH_BOARD_PARSE_FROM_JSON + "\t\t has been called")

                try {

                    println(entityToJson(entity))
                    val board = entityToBoard(entity)

                    HttpResponse(entity = HttpEntity(
                        ContentTypes.`text/plain(UTF-8)`,
                        board.toJson().toString()))
                } catch {
                    case e: Exception => e.printStackTrace()
                        HttpResponse()
                }

            }

            case HttpRequest(POST, Uri.Path(PATH_BOARD_BUY_HOUSES), _, entity, _) => {
                println("Route: \t" + PATH_BOARD_BUY_HOUSES + "\t\t has been called")

                val board = entityToBoard(entity)

                val requestJsonBoardAsString = entityToJson(entity)
                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val amount = (json \ "amount").as[Int]
                val streetName = (json \ "streetName").toString

                val retBoard = board.buildHouses(streetName, amount)


                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    retBoard.toJson().toString()))
            }

            case HttpRequest(POST, Uri.Path(PATH_BOARD_WALK), _, entity, _) => {
                println("Route: \t" + PATH_BOARD_WALK + "\t\t has been called")

                val board = entityToBoard(entity)

                val requestJsonBoardAsString = entityToJson(entity)
                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val amount = (json \ "amount").as[Int]

                val (newPlayer, passedGo) = board.getCurrentPlayer.walk(amount)

                val newGamestate = board.getCurrentPlayer.getCurrentField.action(board.getCurrentPlayer)

                board.replacePlayer(board.getCurrentPlayer, newPlayer)

                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    board.toJson()
                        .+("passedGo", Json.toJson(passedGo.toString))
                        .+("newGamestate", Json.toJson(GameStatus.map.get(newGamestate).toString))
                        .toString()))
            }


            case HttpRequest(GET, Uri.Path(PATH_BOARD_CURRENT_FIELD_HOUSE_COST), _, entity, _) => {
                println("Route: \t" + PATH_BOARD_CURRENT_FIELD_HOUSE_COST + "\t\t has been called")

                val board = entityToBoard(entity)
                val houseCost = board.getCurrentPlayer.getCurrentField.asInstanceOf[IStreet].getHouseCost


                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    houseCost.toString))
            }


            case HttpRequest(GET, Uri.Path(PATH_BOARD_CURRENT_PLAYER_BOUGHT_FIELDS), _, entity, _) => {
                println("Route: \t" + PATH_BOARD_CURRENT_PLAYER_BOUGHT_FIELDS + "\t\t has been called")

                val board = entityToBoard(entity)
                var boughtFieldNames: String = ""
                board.getCurrentPlayer.getBought.toSeq.sortBy(_.getName)
                    .foreach(bought => boughtFieldNames = boughtFieldNames + bought.getName + "\n")


                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    boughtFieldNames))
            }


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


    def entityToBoard(entity: RequestEntity): IBoard = {
        val requestJsonBoardAsString = entityToJson(entity)
        val json = Json.parse(requestJsonBoardAsString).as[JsObject]
        Board.fromSimplefiedJson(json)
    }

    def entityToJson(entity: RequestEntity): String = {
        val entityString = Unmarshal(entity).to[String].toString
        entityString.replace("FulfilledFuture(", "").replace(")", "")
    }

}
