package monopoly.util.fileIo.fileIoXml

import boardComponent.IBoard
import boardComponent.boardBaseImpl.Board
import model.gamestate.GameStatus
import model.gamestate.GameStatus.BuildStatus.BuildStatus
import model.gamestate.GameStatus.GameStatus
import monopoly.controller.IController
import monopoly.util.fileIo.IFileIo
import model.fieldComponent.fieldBaseImpl.{ActionField, Street}
import model.fieldComponent.{Field, IBuyable}
import model.playerComponent.playerBaseImpl.Player
import model.util.{FieldIterator, PlayerIterator}

import scala.util.{Failure, Success, Try}
import scala.xml.PrettyPrinter

class FileIoXml extends IFileIo {
    def load(path: String): (IBoard, GameStatus, BuildStatus) = {
        val f = Try(scala.xml.XML.loadFile(path.replace(".json", ".xml")))
        val file = f match {
            case Success(v) => v
            case Failure(_) => scala.xml.XML.loadFile(getClass.getClassLoader.getResource("save-game.xml").getPath)
        }
        var fields = List[Field]()
        (file \ "board" \ "fields").head.child.foreach(f => {
            f.label match {
                case "action-field" =>
                    fields = fields :+ ActionField((f \ "name").text.trim)
                case "street" =>
                    fields = fields :+ Street(
                        name = (f \ "name").text.trim,
                        price = (f \ "price").text.trim.toInt,
                        rentCosts = (f \ "rent-cost").text.trim.split(" ").map(s => s.toInt),
                        houseCost = (f \ "house-cost").text.trim.toInt,
                        numHouses = (f \ "num-houses").text.trim.toInt,
                        isBought = (f \ "is-bought").text.trim.toBoolean)
                case _ =>
            }
        })
        var players = List[Player]()
        (file \ "board" \ "player-iterator" \ "players").head.child.foreach(p => {
            p.label match {
                case "player" =>
                    val bought = (p \ "bought" \ "street").map(s => {
                        s.label match {
                            case "street" =>
                                fields.find(field => field.getName.equals((s \ "name").text.trim)).get.asInstanceOf[IBuyable]
                            case _ => // Ignore #PCData
                        }
                    }).toSet
                    val player = Player(
                        name = (p \ "name").text.trim,
                        money = (p \ "money").text.trim.toInt,
                        currentField = fields.find(field => field.getName.equals((p \ "current-field").text.trim)).get,
                        bought = bought.map(f => f.asInstanceOf[IBuyable]),
                        fieldIt = FieldIterator(fields)
                    )
                    player.fieldIt.current = (p \ "field-iterator" \ "current-idx").text.trim.toInt
                    players = players :+ player
                case _ =>
            }
        })
        (Board(
            fields,
            players.find(p => p.name.equals((file \ "board" \ "current-player").text.trim)).get,
            playerIt = PlayerIterator(players.toArray, (file \ "board" \ "player-iterator" \ "start-idx").text.trim.toInt)
        ), GameStatus.revMap((file \\ "controller" \ "game-status").text.trim), GameStatus.BuildStatus.revMap((file \\ "controller" \ "build-status").text.trim))
    }

    def save(controller: IController): Unit = saveString(controller)

    def saveString(controller: IController): Unit = {
        import java.io._
        val pw = new PrintWriter(new File("save-game.xml"))
        val prettyPrinter = new PrettyPrinter(120, 4)
        val xml = prettyPrinter.format(controller.toXml)

        pw.write(xml)
        pw.close()
    }

}
