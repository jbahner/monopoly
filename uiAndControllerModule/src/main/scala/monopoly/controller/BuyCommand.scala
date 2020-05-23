package monopoly.controller

import boardComponent.IBoard
import playerModule.fieldComponent.{IBuilding, IBuyable, IStreet}
import gamestate.GameStatus._
import monopoly.controller.controllerBaseImpl.UpdateInfo
import monopoly.util.Command
import playerModule.playerComponent.IPlayer


case class BuyCommand(buyable: IBuyable, controller: IController) extends Command {
    private val backupBoard: IBoard = controller.getBoard.copy(controller.getBoard.getFields, controller.getCurrentPlayer.get, controller.getBoard.getPlayerIt.copy)
    private val backupGameString: String = controller.currentGameMessage

    override def undoStep(): Unit = {
        controller.setBoard(backupBoard)
        controller.currentGameMessage = backupGameString
        controller.controllerState = CAN_BUY
    }

    override def redoStep(): Unit = doStep()

    override def doStep(): Unit = {
        var newField = buyable
        buyable match {
            case street: IStreet => newField = street.copy(isBought = true)
            case building: IBuilding => newField = building.copy(isBought = true)
        }
        val currentPlayer = controller.getCurrentPlayer
        val newPlayer: IPlayer = currentPlayer.get.copy(money = currentPlayer.get.getMoney - newField.getPrice,
            bought = currentPlayer.get.getBought + newField)
        controller.setBoard(controller.getBoard.replacePlayer(currentPlayer.get, newPlayer)
          .copy(controller.getBoard.getFields, newPlayer, controller.getBoard.getPlayerIt))
        controller.setBoard(controller.getBoard.replaceField(buyable, newField))
        controller.controllerState = BOUGHT
        controller.publish(new UpdateInfo)
    }
}
