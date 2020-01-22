package de.htwg.se.monopoly.util.fileIo.fileIoXml

import com.google.inject.Guice
import de.htwg.se.monopoly.MonopolyModule
import de.htwg.se.monopoly.controller.GameStatus.BuildStatus.BuildStatus
import de.htwg.se.monopoly.controller.GameStatus.{BuildStatus, GameStatus}
import de.htwg.se.monopoly.controller.{GameStatus, IController}
import de.htwg.se.monopoly.model.boardComponent.{Field, IBoard, IBuyable}
import de.htwg.se.monopoly.model.boardComponent.boardBaseImpl.{ActionField, Board, Buyable, Street}
import de.htwg.se.monopoly.model.playerComponent.playerBaseImpl.Player
import de.htwg.se.monopoly.util.{FieldIterator, PlayerIterator}
import de.htwg.se.monopoly.util.fileIo.IFileIo

import scala.xml.{Node, NodeSeq, PrettyPrinter}

class FileIoXml extends IFileIo {
    def load(path: String): (IBoard, GameStatus, BuildStatus) = {
        val file = scala.xml.XML.loadFile(path.replace(".json", ".xml"))
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
                                fields.find (field => field.getName.equals ((s \ "name").text.trim) ).get.asInstanceOf[IBuyable]
                            case _ => // Ignore #PCData
                        }
                    } ).toSet
                    val player = Player (
                        name = (p \ "name").text.trim,
                        money = (p \ "money").text.trim.toInt,
                        currentField = fields.find(field => field.getName.equals ((p \ "current-field").text.trim)).get,
                        bought = bought.map(f => f.asInstanceOf[IBuyable]),
                        fieldIt = FieldIterator (fields)
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

    def saveString(controller: IController): Unit = {
        import java.io._
        val pw = new PrintWriter(new File("save-game.xml"))
        val prettyPrinter = new PrettyPrinter(120, 4)
        val xml = prettyPrinter.format(controller.toXml)

        pw.write(xml)
        pw.close()
    }


    def save(controller: IController): Unit = saveString(controller)

}