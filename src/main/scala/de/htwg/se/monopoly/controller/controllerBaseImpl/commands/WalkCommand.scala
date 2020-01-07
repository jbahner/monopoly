package de.htwg.se.monopoly.controller.controllerBaseImpl.commands

import de.htwg.se.monopoly.controller.GameStatus._
import de.htwg.se.monopoly.controller.IController
import de.htwg.se.monopoly.controller.controllerBaseImpl.UpdateInfo
import de.htwg.se.monopoly.model.boardComponent.boardBaseImpl.{Board, Buyable}
import de.htwg.se.monopoly.util.{Command, GeneralUtil}

case class WalkCommand(dice: (Int, Int), controller: IController) extends Command{
    private val backupBoard: Board = controller.board.copy(fields = controller.board.fields, playerIt = controller.board.playerIt.copy)
    private val backupGameString: String = controller.getCurrentGameMessage
    override def doStep(): Unit = {
        controller.controllerState = ROLLED
        controller.catCurrentGameMessage
        val player = controller.board.currentPlayer
        val (newPlayer, passedGo) = player.walk(dice._1 + dice._2)

        if (passedGo) {
            controller.controllerState = PASSED_GO
            controller.catCurrentGameMessage
        }

        controller.board = controller.board.replacePlayer(player, newPlayer)
        controller.controllerState = NEW_FIELD
        controller.catCurrentGameMessage

        val newField = controller.getCurrentField
        // Action return ALREADY_BOUGHT, CAN_BUY or BOUGHT_BY_OTHER
        controller.controllerState = newField.action(newPlayer)
        controller.catCurrentGameMessage

        controller.controllerState match {
            case BOUGHT_BY_OTHER =>
                controller.payRent(controller.getCurrentPlayer.get, controller.getCurrentField.asInstanceOf[Buyable], controller.getBuyer(controller.getCurrentField.asInstanceOf[Buyable]).get)
            case _ =>
        }

        if (GeneralUtil.getWholeGroups(newPlayer) != Nil) {
            controller.controllerState = CAN_BUILD
            controller.buildStatus = BuildStatus.DEFAULT
        }
    }

    override def undoStep(): Unit = {
        controller.board = backupBoard
        controller.controllerState = START_OF_TURN
        controller.currentGameMessage = backupGameString
        controller.updateCurrentPlayerInfo
        controller.publish(new UpdateInfo)
    }

    override def redoStep(): Unit = {
        doStep()
        controller.publish(new UpdateInfo)
    }
}
