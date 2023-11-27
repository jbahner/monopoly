package monopoly.persistence.relational

import monopoly.controller.IController
import monopoly.controller.gamestate.GameStatus
import monopoly.controller.gamestate.GameStatus.BuildStatus.BuildStatus
import monopoly.controller.gamestate.GameStatus.{BuildStatus, GameStatus}
import play.api.libs.json.{JsObject, Json}
import slick.driver.H2Driver.api._
import play.api.libs.json.{JsObject, Json}


import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


object ControllerMapping {

    // the base query for the Users table
    val controllers = TableQuery[ControllerTable]
    val db = Database.forConfig("h2mem1")

    Await.result(db.run(DBIO.seq(
        // create the schema
        controllers.schema.create,
    )), Duration.Inf)

    def saveController(controller: IController): Boolean = {
        println("Saving in H2 Database")
        println(controller.toJson().toString())

        try {
            Await.result(db.run(DBIO.seq(
                controllers += (DbController(controller.toJson().toString())),
            )), Duration.Inf)
            true
        } catch {
            case e:Exception => false
        }
    }

    def loadController(): Option[(GameStatus, BuildStatus, (Int, Int), String)] = {
        var controllerString = ""
        println("Loading from H2 Database")

        Await.result(db.run(DBIO.seq(
            controllers.result.map(pl => {
                println(pl)
                controllerString = pl.head.contents
            }))), Duration.Inf)

        val controllerJson = Json.parse(controllerString).as[JsObject]

        Option.apply((GameStatus.fromJson(controllerJson),
            BuildStatus.fromJson(controllerJson),
            (0, 0),
            (controllerJson \ "controller" \ "current-game-message").get.toString()))
    }

}

case class DbController(contents: String, id: Option[Int] = None)


class ControllerTable(tag: Tag) extends Table[DbController](tag, "CONTROLLER") {
    // Auto Increment the id primary key column
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

    // The name can't be null
    def contents = column[String]("CONTENTS")

    // the * projection (e.g. select * ...) auto-transforms the tupled
    // column values to / from a User
    def * = (contents, id.?) <> (DbController.tupled, DbController.unapply)
}
