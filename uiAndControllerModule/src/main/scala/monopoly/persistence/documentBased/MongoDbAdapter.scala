package monopoly.persistence.documentBased

import monopoly.controller.IController
import monopoly.controller.gamestate.GameStatus
import monopoly.controller.gamestate.GameStatus.BuildStatus.BuildStatus
import monopoly.controller.gamestate.GameStatus.{BuildStatus, GameStatus}
import monopoly.persistence.IDaoController
import org.mongodb.scala._
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Await
import scala.concurrent.duration._


class MongoDbAdapter extends IDaoController {
    val HTTP_RESPONSE_WAIT_TIME: FiniteDuration = 1000 seconds


    // To directly connect to the default server localhost on port 27017
    val mongoClient: MongoClient = MongoClient()

    val database: MongoDatabase = mongoClient.getDatabase("monopoly")
    val controllerCollection: MongoCollection[Document] = database.getCollection("controller")

    override def saveController(controller: IController): Boolean = {
        println("Saving board in MongoDb")
        Await.result(controllerCollection.insertOne(
            Document("controller" -> controller.toJson().toString())
        ).toFuture(), HTTP_RESPONSE_WAIT_TIME)
        true
    }

    override def loadController(): (GameStatus, BuildStatus, (Int, Int), String) = {
        println("Loading board from MongoDb")

        val x = Await.result(controllerCollection.find().toFuture(), HTTP_RESPONSE_WAIT_TIME)
        val board: Seq[String] = x
            .map(doc => doc.get("controller"))
            .map(desk => desk.get.asString().getValue)

        val controllerJson = Json.parse(board.last).as[JsObject]

        (GameStatus.fromJson(controllerJson),
            BuildStatus.fromJson(controllerJson),
            (0, 0),
            (controllerJson \ "controller" \ "current-game-message").get.toString())

    }

}
