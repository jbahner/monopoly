package monopoly.controller

import modelComponent.boardComponent.IBoard
import monopoly.controller.controllerBaseImpl.{CatGuiMessage, UpdateInfo}
import monopoly.util.Command
import modelComponent.fieldComponent.IBuyable
import modelComponent.util.GeneralUtil
import monopoly.controller.gamestate.GameStatus
import monopoly.controller.gamestate.GameStatus._



case class WalkCommand(dice: (Int, Int), controller: IController) extends Command {
    private val backupBoard: IBoard = controller.getBoard.copy(controller.getBoard.getFields,
        controller.getBoard.getCurrentPlayer,
        controller.getBoard.getPlayerIt,
        controller.getBoard.currentDice)
    private val backupGameString: String = controller.getCurrentGameMessage

    override def undoStep(): Unit = {
        controller.setBoard(backupBoard)
        controller.controllerState = START_OF_TURN
        controller.currentGameMessage = backupGameString
        controller.updateCurrentPlayerInfo
        controller.publish(new UpdateInfo)
    }

    override def redoStep(): Unit = {
        doStep()
        controller.publish(new UpdateInfo)
    }

    override def doStep(): Unit = {
        controller.controllerState = ROLLED
        controller.catCurrentGameMessage
        controller.publish(new CatGuiMessage)
        val player = controller.getBoard.getCurrentPlayer
        val (newPlayer, passedGo) = player.walk(dice._1 + dice._2)

        if (passedGo) {
            controller.controllerState = PASSED_GO
            controller.catCurrentGameMessage
            controller.publish(new CatGuiMessage)
        }

        controller.setBoard(controller.getBoard.replacePlayer(player, newPlayer))
        controller.controllerState = NEW_FIELD
        controller.catCurrentGameMessage
        controller.publish(new CatGuiMessage)

        val newField = controller.getCurrentField
        // Action return ALREADY_BOUGHT, CAN_BUY or BOUGHT_BY_OTHER
        controller.controllerState = GameStatus.revMap(newField.action(newPlayer))
        controller.catCurrentGameMessage
        controller.publish(new CatGuiMessage)

        controller.controllerState match {
            case BOUGHT_BY_OTHER =>
                // RentPay 3
            controller.payRent(controller.getCurrentPlayer.get, controller.getCurrentField.asInstanceOf[IBuyable],
                    controller.getBuyer(controller.getCurrentField.asInstanceOf[IBuyable]).get)
            case _ =>
        }

        if (GeneralUtil.getWholeGroups(newPlayer) != Nil) {
            controller.controllerState = CAN_BUILD
            controller.buildStatus = BuildStatus.DEFAULT
        }
    }
}
