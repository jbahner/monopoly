package de.htwg.se.monopoly.controller

import de.htwg.se.monopoly.controller.GameStatus.{BuildStatus, CAN_BUILD}
import de.htwg.se.monopoly.controller.controllerBaseImpl.{Controller, UpdateInfo}
import de.htwg.se.monopoly.model.boardComponent.{IBoard, IStreet}
import de.htwg.se.monopoly.util.Command

case class BuildCommand(street: IStreet, amount: Int, controller: Controller) extends Command {
    private val backupBoard: IBoard = controller.board.copy(controller.board.getFields, controller.board.getCurrentPlayer, controller.board.getPlayerIt)
    private val backupGameString: String = controller.currentGameMessage

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

    override def doStep(): Unit = {
        controller.board = controller.board.replaceField(field = street, newField = street.buyHouses(amount))
        controller.board = controller.board.replacePlayer(controller.getCurrentPlayer.get,
            controller.getCurrentPlayer.get.copy(money = controller.getCurrentPlayer.get.getMoney - street.getHouseCost * amount))
        controller.buildStatus = BuildStatus.BUILT
    }
}
