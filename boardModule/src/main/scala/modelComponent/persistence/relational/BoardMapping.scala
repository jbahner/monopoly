package modelComponent.persistence.relational

import modelComponent.boardComponent.IBoard
import modelComponent.boardComponent.boardBaseImpl.Board
import play.api.libs.json.{JsObject, Json}
import slick.driver.H2Driver.api._

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


object BoardMapping {

    // the base query for the Users table
    val boards = TableQuery[Board]
    val db = Database.forConfig("h2mem1")

    Await.result(db.run(DBIO.seq(
        // create the schema
        boards.schema.create,
    )), Duration.Inf)

    def saveBoard(board: IBoard): Boolean = {
        println("Saving in H2 Database")
        println(board.toJson().toString())

        try {
//            Await.result(
                db.run(DBIO.seq(
                boards += (DbBoard(board.toJson().toString())),
            ))
//            , Duration.Inf)
            true
        } catch {
            case e:Exception => false
        }
    }

    def loadBoard(): Future[IBoard] = {
        var board: Future[IBoard] = Future.never
        println("Loading from H2 Database")

        Await.result(db.run(DBIO.seq(
            boards.result.map(pl => {
                println(pl)
                board = Future(Board.fromSimplefiedJson(Json.parse(pl.head.contents).as[JsObject]))
            }))), Duration.Inf)
        board
    }

}

case class DbBoard(contents: String, id: Option[Int] = None)


class Board(tag: Tag) extends Table[DbBoard](tag, "BOARD") {
    // Auto Increment the id primary key column
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

    // The name can't be null
    def contents = column[String]("CONTENTS")

    // the * projection (e.g. select * ...) auto-transforms the tupled
    // column values to / from a User
    def * = (contents, id.?) <> (DbBoard.tupled, DbBoard.unapply)
}
