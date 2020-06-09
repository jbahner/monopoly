package monopoly.controller

import modelComponent.boardComponent.IBoard
import modelComponent.boardComponent.boardBaseImpl.Board
import monopoly.controller.controllerBaseImpl.UpdateInfo
import monopoly.controller.gamestate.GameStatus._
import monopoly.util.Command
import play.api.libs.json.{JsObject, Json}

case class BuyCommand(controller: IController) extends Command {
    private val backupBoardString: String = controller.getBoard().toJson().toString

    private val backupGameString: String = controller.currentGameMessage

    override def undoStep(): IBoard = {
        val parsedBoard = Board.fromSimplefiedJson(Json.parse(backupBoardString).as[JsObject])
        controller.setBoard(parsedBoard)
        controller.currentGameMessage = backupGameString
        controller.controllerState = CAN_BUY
        parsedBoard
    }

    override def redoStep(): IBoard = doStep()

    override def doStep(): IBoard = {

        val board = controller.buyCurrentField()

        controller.controllerState = BOUGHT

        controller.publish(new UpdateInfo)

        board
    }
}
