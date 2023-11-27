package modelComponent.persistence.documentBased

import modelComponent.boardComponent.IBoard
import modelComponent.boardComponent.boardBaseImpl.Board
import modelComponent.persistence.IDaoBoard
import org.mongodb.scala._
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


class MongoDbAdapter extends IDaoBoard {
    val HTTP_RESPONSE_WAIT_TIME: FiniteDuration = 1000 seconds


    // To directly connect to the default server localhost on port 27017
    val mongoClient: MongoClient = MongoClient()

    val database: MongoDatabase = mongoClient.getDatabase("monopoly")
    val boardCollection: MongoCollection[Document] = database.getCollection("board")

    override def saveBoard(board: IBoard): Boolean = {
        println("Saving board in MongoDb")
//        Await.result(
            boardCollection.insertOne(
            Document("board" -> board.toJson().toString())
        ).toFuture()
//        , HTTP_RESPONSE_WAIT_TIME)
        true
    }

    override def loadBoard(): IBoard = {
        println("Loading board from MongoDb")

        val x = Await.result(boardCollection.find().toFuture(), HTTP_RESPONSE_WAIT_TIME)
        val board: Seq[String] = x
            .map(doc => doc.get("board"))
            .map(desk => desk.get.asString().getValue)

        Board.fromSimplefiedJson(Json.parse(board.last).as[JsObject])

    }
}
