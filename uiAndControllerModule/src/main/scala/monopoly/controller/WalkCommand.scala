package monopoly.controller

import monopoly.MainComponentServer
import monopoly.controller.controllerBaseImpl.{CatGuiMessage, UpdateInfo}
import monopoly.controller.gamestate.GameStatus._
import monopoly.util.Command


case class WalkCommand(dice: (Int, Int), controller: IController) extends Command {
    private val backupBoard: String = controller.getBoard()
    private val backupGameString: String = controller.getCurrentGameMessage

    override def undoStep(): String = {
        controller.setBoard(backupBoard)
        controller.controllerState = START_OF_TURN
        controller.currentGameMessage = backupGameString
        controller.updateCurrentPlayerInfo()
        controller.publish(new UpdateInfo)
        backupBoard
    }

    override def redoStep(): String = {
        val board = doStep()
        controller.publish(new UpdateInfo)
        board
    }

    override def doStep(): String = {
        controller.controllerState = ROLLED
        controller.catCurrentGameMessage()
        controller.publish(new CatGuiMessage)

        // Seems a bit buggy -> Player always walks from "Go" and not from previous field
        val board = controller.currentPlayerWalk()

        val passedGo = controller.getDidPlayerPassGo()
        controller.setBoard(board)

        if (passedGo) {
            controller.controllerState = PASSED_GO
            controller.catCurrentGameMessage()
            controller.publish(new CatGuiMessage)
        }


        controller.controllerState = NEW_FIELD
        controller.catCurrentGameMessage()
        controller.publish(new CatGuiMessage)

        // Action return ALREADY_BOUGHT, CAN_BUY or BOUGHT_BY_OTHER
        controller.controllerState = MainComponentServer.getFieldGameState(controller.getBoard())
        // controller.catCurrentGameMessage()
        controller.publish(new CatGuiMessage)

        controller.controllerState match {
            case BOUGHT_BY_OTHER =>
                // RentPay 3
                controller.payRent()
            case _ =>
        }

        if (MainComponentServer.canCurrentPlayerBuyHouses(controller.getBoard())) {
            controller.controllerState = CAN_BUILD
            controller.buildStatus = BuildStatus.DEFAULT
        }
        board
    }
}
