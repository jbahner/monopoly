package monopoly.controller

import monopoly.controller.gamestate.GameStatus._
import monopoly.util.Command

case class BuyCommand(controller: IController) extends Command {
    private val backupBoardString: String = controller.getBoard()

    private val backupGameString: String = controller.currentGameMessage

    override def undoStep(): String = {
        val parsedBoard = backupBoardString
        controller.setBoard(parsedBoard)
        controller.currentGameMessage = backupGameString
        controller.controllerState = CAN_BUY
        parsedBoard
    }

    override def redoStep(): String = doStep()

    override def doStep(): String = {

        // Buying things is disabled
        controller.getBoard()
    }
}
