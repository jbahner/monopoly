package de.htwg.se.monopoly.controller.controllerBaseImpl.commands

import de.htwg.se.monopoly.controller.GameStatus._
import de.htwg.se.monopoly.controller.controllerBaseImpl.{Controller, UpdateInfo}
import de.htwg.se.monopoly.model.boardComponent.{Board, Street}
import de.htwg.se.monopoly.util.Command

case class BuildCommand(street: Street, amount: Int, controller: Controller) extends Command {
  private val backupBoard: Board = controller.board.copy(fields = controller.board.fields, playerIt = controller.board.playerIt.copy)
  private val backupGameString: String = controller.currentGameMessageString

  override def doStep(): Unit = {
    controller.board = controller.board.replaceField(field = street, newField = street.buyHouses(amount))
    controller.board = controller.board.replacePlayer(controller.getCurrentPlayer.get, controller.getCurrentPlayer.get.copy(money = controller.getCurrentPlayer.get.money - street.houseCost * amount))
    controller.buildStatus = BuildStatus.BUILT
  }

  override def undoStep(): Unit = {
    controller.board = backupBoard
    //controller.currentGameMessageString = backupGameString
    controller.controllerState = CAN_BUILD
    controller.buildStatus = BuildStatus.DEFAULT
    controller.publish(new UpdateInfo)
  }

  override def redoStep(): Unit = {
    doStep()
    controller.publish(new UpdateInfo)
  }
}
