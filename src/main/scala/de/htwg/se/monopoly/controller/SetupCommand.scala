package de.htwg.se.monopoly.controller

import de.htwg.se.monopoly.model.boardComponent.boardBaseImpl.{ActionField, Street}
import de.htwg.se.monopoly.model.boardComponent.{Field, IBoard}
import de.htwg.se.monopoly.model.playerComponent.IPlayer
import de.htwg.se.monopoly.util.{Command, FieldIterator, PlayerIterator}

class SetupCommand(playerNames: Set[String], controller: IController) extends Command {
    override def doStep(): Unit = {
        controller.loadGame()
    }

    override def undoStep(): Unit = controller.setBoard(null)

    override def redoStep(): Unit = doStep()
}
