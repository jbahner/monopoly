package de.htwg.se.monopoly.controller.controllerBaseImpl.commands

import de.htwg.se.monopoly.controller.controllerBaseImpl.Controller
import de.htwg.se.monopoly.model.boardComponent.Field
import de.htwg.se.monopoly.model.boardComponent.boardBaseImpl.{ActionField, Board, Building, Street}
import de.htwg.se.monopoly.model.playerComponent.{IPlayer, playerBaseImpl}
import de.htwg.se.monopoly.util.{Command, FieldIterator, GeneralUtil, PlayerIterator}
import play.api.libs.json.Json

import scala.io.Source

class SetupCommand(fieldFile: String, playerNames: Set[String], controller: Controller) extends Command {
    override def doStep(): Unit = {
        val file = getClass.getClassLoader.getResource(fieldFile).getPath
        val source = Source.fromFile(file)
        val json = Json.parse(source.mkString)
        source.close
        var fields = List[Field]()
        for(i <- 0 until (json \ "numFields").get.toString().toInt) {
            val f = (json \ "fields") (i)
            val name = (f \ "name").as[String]
            val fieldType = (f \ "type").as[String]
            fieldType match {
                case "ActionField" => fields = fields :+ ActionField(name)
                case "Street" =>
                    val group = (f \ "group").as[Int]
                    if(GeneralUtil.groupList.length < group)
                        GeneralUtil.groupList = GeneralUtil.groupList :+ Set(name)
                    else
                        GeneralUtil.groupList = GeneralUtil.groupList.updated(group - 1, GeneralUtil.groupList(group - 1) + name)
                    val price = (f \ "price").as[Int]
                    val rent = (f \ "rent").as[Array[Int]]
                    val buildcost = (f \ "buildcost").as[Int]
                    fields = fields :+ Street(name, price, rent, buildcost)
                case "Building" =>
                    val price = (f \ "price").as[Int]
                    fields = fields :+ Building(name, price)
            }
        }
        println(fields)



        /*val go = ActionField("Go")
       // var fields = List[Field](go)

        for (i <- 1 to 9)
            fields = fields :+ Street(name = "Street" + i,
                price = 50 * i,
                rentCosts = getRentArray(50 * i),
                houseCost = 25 * i,
                isBought = true)
        */
        val players = playerNames.map(p => playerBaseImpl.Player(name = p, money = 1500, currentField = fields.head, bought = Set(), fieldIt = new FieldIterator(fields))).toArray

        // For paying rent testing purposes
        /*for (i <- 1 to 6) {
            players(0) = players(0).copy(bought = players(0).getBought + fields(i).asInstanceOf[Street])
        }
        for (i <- 7 to 9)
            players(1) = players(1).copy(bought = players(1).getBought + fields(i).asInstanceOf[Street])
        */
        controller.board = Board(fields, players(0), new PlayerIterator(players.asInstanceOf[Array[IPlayer]]))
    }

    // This is only for testing purposes
    private def getRentArray(cost: Int): Array[Int] = {
        val step = cost / 10
        Array(step, step * 2, step * 3, step * 4, step * 5, step * 6)
    }

    override def undoStep(): Unit = controller.board = null

    override def redoStep(): Unit = doStep()
}
