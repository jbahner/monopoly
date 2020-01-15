package de.htwg.se.monopoly.controller

import de.htwg.se.monopoly.model.boardComponent.boardBaseImpl.{ActionField, Street}
import de.htwg.se.monopoly.model.boardComponent.{Field, IBoard}
import de.htwg.se.monopoly.model.playerComponent.IPlayer
import de.htwg.se.monopoly.util.{Command, FieldIterator, PlayerIterator}

class SetupCommand(playerNames: Set[String], controller: IController) extends Command {
    override def doStep(): Unit = {

        val go = ActionField("Go")
        var fields = List[Field](go)

        for (i <- 1 to 9)
            fields = fields :+ Street(name = "Street" + i,
                price = 50 * i,
                rentCosts = getRentArray(50 * i),
                houseCost = 25 * i,
                isBought = true)

        val players = playerNames.map(p => controller.injector.getInstance(classOf[IPlayer]).copy(p, 1500, fields.head, Set(), FieldIterator(fields))).toArray

        // For paying rent testing purposes
        for (i <- 1 to 6) {
            players(0) = players(0).copy(bought = players(0).getBought + fields(i).asInstanceOf[Street])
        }
        for (i <- 7 to 9)
            players(1) = players(1).copy(bought = players(1).getBought + fields(i).asInstanceOf[Street])
        controller.setBoard(controller.injector.getInstance(classOf[IBoard]).copy(fields, players(0), new PlayerIterator(players.asInstanceOf[Array[IPlayer]])))
    }

    // This is only for testing purposes
    private def getRentArray(cost: Int): Array[Int] = {
        val step = cost / 10
        Array(step, step * 2, step * 3, step * 4, step * 5, step * 6)
    }

    override def undoStep(): Unit = controller.setBoard(null)

    override def redoStep(): Unit = doStep()
}
