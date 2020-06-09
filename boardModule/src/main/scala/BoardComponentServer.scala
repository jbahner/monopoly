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
//                println("Input:")
//                println("\t" + requestJsonBoardAsString)

                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val board = Board.fromSimplefiedJson(json)

                val retBoard = board.nextPlayerTurn()
//                println("Output")
//                println("\t" + retBoard.toJson().toString())


                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    retBoard.toJson().toString()))

            case HttpRequest(POST, Uri.Path(PATH_ROLL_DICE), _, entity, _) =>

                println("Called Route: " + PATH_NEXT_PLAYER)

                val requestJsonBoardAsString = entityToJson(entity)
//                println("Input:")
//                println("\t" + requestJsonBoardAsString)

                val json = Json.parse(requestJsonBoardAsString).as[JsObject]
                val board = Board.fromSimplefiedJson(json)
                val (d1, d2) = board.rollDice()

                println("Rolled: " + d1 + " " + d2)

                val returnBoardJson = board.toJson()
                    .+("d1", Json.toJson(d1))
                    .+("d2", Json.toJson(d2))

//                println("Output")
//                println("\t" + retBoard.toJson().toString())


                HttpResponse(entity = HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    returnBoardJson.toString()))

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
