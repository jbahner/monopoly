package de.htwg.se.monopoly.controller.controllerBaseImpl.commands

import de.htwg.se.monopoly.controller.GameStatus.{BOUGHT, _}
import de.htwg.se.monopoly.controller.controllerBaseImpl.{Controller, UpdateInfo}
import de.htwg.se.monopoly.model.boardComponent.boardBaseImpl.{Board, Building, Buyable, Street}
import de.htwg.se.monopoly.model.playerComponent.IPlayer
import de.htwg.se.monopoly.util.Command

case class BuyCommand(buyable: Buyable, controller: Controller) extends Command {
  private val backupBoard: Board = controller.board.copy(fields = controller.board.fields, playerIt = controller.board.playerIt.copy)
  private val backupGameString: String = controller.currentGameMessage

  override def doStep(): Unit = {
    var newField = buyable
    buyable match {
      case street: Street => newField = street.copy(isBought = true)
      case building: Building => newField = building.copy(isBought = true)
    }
    val currentPlayer = controller.getCurrentPlayer
    val newPlayer: IPlayer = currentPlayer.get.copy(money = currentPlayer.get.getMoney - newField.getPrice,
      bought = currentPlayer.get.getBought + newField)
    controller.board = controller.board.replacePlayer(currentPlayer.get, newPlayer).copy(currentPlayer = newPlayer)
    controller.board = controller.board.replaceField(buyable, newField)
    controller.controllerState = BOUGHT
    controller.publish(new UpdateInfo)
  }

  override def undoStep(): Unit = {
    controller.board = backupBoard
    controller.currentGameMessage = backupGameString
    controller.controllerState = CAN_BUY
  }

  override def redoStep(): Unit = doStep()
}
