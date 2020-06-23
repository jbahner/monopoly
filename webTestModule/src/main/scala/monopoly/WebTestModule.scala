package monopoly

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer

import scala.concurrent._
import scala.concurrent.duration._

object WebTestModule {

    val HTTP_RESPONSE_WAIT_TIME = 1000

    // Akka Inits
    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    // TODO Active if using Docker
    //  do this in Board server, too
    //    private val BOARD_COMPONENT_URL = "http://myboard:8082"
    private val BOARD_COMPONENT_URL = "http://localhost:8082"

    private val BOARD_JSON = "{\"num-fields\":10,\"fields\":[{\"field\":{\"type\":\"action-field\",\"name\":\"Go\"}},{\"field\":{\"type\":\"street\",\"name\":\"Street1\",\"price\":50,\"rent-cost\":[5,10,15,20,25,30],\"houseCost\":25,\"numHouses\":0,\"is-bought\":true}},{\"field\":{\"type\":\"street\",\"name\":\"Street2\",\"price\":100,\"rent-cost\":[10,20,30,40,50,60],\"houseCost\":50,\"numHouses\":0,\"is-bought\":true}},{\"field\":{\"type\":\"street\",\"name\":\"Street3\",\"price\":150,\"rent-cost\":[15,30,45,60,75,90],\"houseCost\":75,\"numHouses\":0,\"is-bought\":true}},{\"field\":{\"type\":\"street\",\"name\":\"Street4\",\"price\":200,\"rent-cost\":[20,40,60,80,100,120],\"houseCost\":100,\"numHouses\":0,\"is-bought\":true}},{\"field\":{\"type\":\"street\",\"name\":\"Street5\",\"price\":250,\"rent-cost\":[25,50,75,100,125,150],\"houseCost\":125,\"numHouses\":0,\"is-bought\":true}},{\"field\":{\"type\":\"street\",\"name\":\"Street6\",\"price\":300,\"rent-cost\":[30,60,90,120,150,180],\"houseCost\":150,\"numHouses\":0,\"is-bought\":true}},{\"field\":{\"type\":\"street\",\"name\":\"Street7\",\"price\":350,\"rent-cost\":[35,70,105,140,175,210],\"houseCost\":175,\"numHouses\":0,\"is-bought\":true}},{\"field\":{\"type\":\"street\",\"name\":\"Street8\",\"price\":400,\"rent-cost\":[40,80,120,160,200,240],\"houseCost\":200,\"numHouses\":0,\"is-bought\":true}},{\"field\":{\"type\":\"street\",\"name\":\"Street9\",\"price\":450,\"rent-cost\":[45,90,135,180,225,270],\"houseCost\":225,\"numHouses\":0,\"is-bought\":true}}],\"current-player\":\"Player1\",\"player-iterator\":{\"num-players\":2,\"players\":[{\"player\":{\"name\":\"Player1\",\"money\":1500,\"current-field\":\"Go\",\"num-bought\":6,\"bought\":[{\"field\":{\"type\":\"street\",\"name\":\"Street2\"}},{\"field\":{\"type\":\"street\",\"name\":\"Street1\"}},{\"field\":{\"type\":\"street\",\"name\":\"Street3\"}},{\"field\":{\"type\":\"street\",\"name\":\"Street5\"}},{\"field\":{\"type\":\"street\",\"name\":\"Street6\"}},{\"field\":{\"type\":\"street\",\"name\":\"Street4\"}}],\"field-iterator\":{\"current-idx\":0}}},{\"player\":{\"name\":\"Player2\",\"money\":1500,\"current-field\":\"Go\",\"num-bought\":3,\"bought\":[{\"field\":{\"type\":\"street\",\"name\":\"Street7\"}},{\"field\":{\"type\":\"street\",\"name\":\"Street8\"}},{\"field\":{\"type\":\"street\",\"name\":\"Street9\"}}],\"field-iterator\":{\"current-idx\":0}}}],\"start-idx\":0},\"currentDice\":0,\"passedGo\":false}"


    def main(args: Array[String]): Unit = {


        val requestHandler: HttpRequest => HttpResponse = {


            case HttpRequest(GET, Uri.Path("/save"), _, _, _) =>

                println("SAVE CALLED")
                Await.result(
                    Http().singleRequest(
                        HttpRequest(POST,
                            uri = BOARD_COMPONENT_URL + "/board/save-current-board",
                            entity = BOARD_JSON)),
                    HTTP_RESPONSE_WAIT_TIME seconds).copy(entity="Saved\n" + BOARD_JSON)

            case HttpRequest(GET, Uri.Path("/load"), _, _, _) =>
                println("LOAD CALLED")
                Await.result(
                    Http().singleRequest(
                        HttpRequest(GET,
                            uri = BOARD_COMPONENT_URL + "/board/load-current-board")),
                    HTTP_RESPONSE_WAIT_TIME seconds)


            case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
                HttpResponse(entity = "I am ROOT!")

            case HttpRequest(GET, Uri.Path("/health"), _, _, _) =>
                HttpResponse(entity = "Health is feeling good!")
        }

        val bindingFuture = Http().bindAndHandleSync(requestHandler, "localhost", 8083)

        println("WebUi is running and waiting for requests.")



    }

    def entityToJson(entity: RequestEntity): String = {
        val entityString = Unmarshal(entity).to[String].toString
        entityString.replace("FulfilledFuture(", "").replace(")", "")
    }



    def getStringFromResponse(input: HttpResponse): String = {
        Unmarshal(input).to[String].toString.replace("FulfilledFuture(", "").replace(")", "")
    }


}
