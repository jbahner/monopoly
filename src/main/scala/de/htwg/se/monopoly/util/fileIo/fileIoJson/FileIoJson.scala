package de.htwg.se.monopoly.util.fileIo.fileIoJson

import de.htwg.se.monopoly.controller.GameStatus.BuildStatus.BuildStatus
import de.htwg.se.monopoly.controller.GameStatus.{BuildStatus, GameStatus}
import de.htwg.se.monopoly.controller.{GameStatus, IController}
import de.htwg.se.monopoly.model.boardComponent.boardBaseImpl.{ActionField, Board, Street}
import de.htwg.se.monopoly.model.boardComponent.{Field, IBoard, IBuyable}
import de.htwg.se.monopoly.model.playerComponent.playerBaseImpl.Player
import de.htwg.se.monopoly.util.fileIo.IFileIo
import de.htwg.se.monopoly.util.{FieldIterator, PlayerIterator}
import play.api.libs.json.Json

import scala.io.Source
import scala.util.{Failure, Success, Try}

class FileIoJson extends IFileIo {
    override def load(path: String): (IBoard, GameStatus, BuildStatus) = {
        val source = Try(Source.fromFile(path.replace(".xml", ".json"))) match {
            case Success(value) => value
            case Failure(_) => Source.fromFile(getClass.getClassLoader.getResource("save-game.json").getPath)
        }
        val json = Json.parse(source.getLines.mkString)
        source.close
        var fields = List[Field]()
        for (i <- 0 until (json \ "controller" \ "board" \ "num-fields").as[Int]) {
            val f = ((json \ "controller" \ "board" \ "fields") (i) \ "field")
            (f \ "type").get.as[String] match {
                case "action-field" =>
                    fields = fields :+ ActionField((f \ "name").get.as[String])
                case "street" =>
                    fields = fields :+ Street(
                        name = (f \ "name").get.as[String],
                        price = (f \ "price").get.as[Int],
                        rentCosts = (f \ "rent-cost").get.as[Array[Int]],
                        houseCost = (f \ "houseCost").get.as[Int],
                        numHouses = (f \ "numHouses").get.as[Int],
                        isBought = (f \ "is-bought").get.as[Boolean]
                    )
                case _ =>
            }
        }
        var players = List[Player]()
        for (i <- 0 until (json \ "controller" \ "board" \ "player-iterator" \ "num-players").as[Int]) {
            val p = ((json \ "controller" \ "board" \ "player-iterator" \ "players") (i) \ "player")
            var bought = Set[IBuyable]()
            for (j <- 0 until (p \ "num-bought").get.as[Int]) {
                val s = ((p \ "bought") (j) \ "field")
                bought = bought + fields.find(field => field.getName.equals((s \ "name").get.as[String])).get.asInstanceOf[IBuyable]
            }
            players = players :+ Player(
                name = (p \ "name").get.as[String],
                money = (p \ "money").get.as[Int],
                currentField = fields.find(field => field.getName.equals((p \ "current-field").get.as[String])).get,
                bought = bought,
                fieldIt = FieldIterator(fields)
            )
        }
        (Board(
            fields,
            currentPlayer = players.find(p => p.name.equals((json \ "controller" \ "board" \ "current-player").get.as[String])).get,
            playerIt = PlayerIterator(players.toArray, (json \ "controller" \ "board" \ "player-iterator" \ "start-idx").get.as[Int])),
          GameStatus.revMap((json \ "controller" \ "game-status").get.as[String]),
          BuildStatus.revMap((json \ "controller" \ "build-status").get.as[String])
        )
    }

    def save(controller: IController): Unit = {
        import java.io._
        val pw = new PrintWriter(new File("save-game.json"))
        pw.write(Json.prettyPrint(controller.toJson()))
        pw.close()
    }

}
