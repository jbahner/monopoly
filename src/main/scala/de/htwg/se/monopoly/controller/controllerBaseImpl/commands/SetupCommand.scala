package de.htwg.se.monopoly.controller.controllerBaseImpl.commands

import de.htwg.se.monopoly.controller.controllerBaseImpl.Controller
import de.htwg.se.monopoly.model.boardComponent.Field
import de.htwg.se.monopoly.model.boardComponent.boardBaseImpl.{ActionField, Board, Street}
import de.htwg.se.monopoly.model.playerComponent.playerBaseImpl.Player
import de.htwg.se.monopoly.model.playerComponent.{IPlayer, playerBaseImpl}
import de.htwg.se.monopoly.util.{Command, FieldIterator, PlayerIterator}

class SetupCommand(playerNames: Set[String], controller: Controller) extends Command {
    override def doStep(): Unit = {
        val go = ActionField("Go")
        var fields = List[Field](go)

        for (i <- 1 to 9)
            fields = fields :+ Street(name = "Street" + i,
                price = 50 * i,
                rentCosts = getRentArray(50 * i),
                houseCost = 25 * i,
                isBought = true)

        val players = playerNames.map(p => Player(p, 1500, fields.head, Set(), FieldIterator(fields))).toArray

        // For paying rent testing purposes
        for (i <- 1 to 6) {
            players(0) = players(0).copy(bought = players(0).getBought + fields(i).asInstanceOf[Street])
        }
        for (i <- 7 to 9)
            players(1) = players(1).copy(bought = players(1).getBought + fields(i).asInstanceOf[Street])
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
