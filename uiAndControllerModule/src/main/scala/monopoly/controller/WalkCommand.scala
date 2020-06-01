package monopoly.controller

import com.sun.tools.javadoc.Main
import model.fieldComponent.IBuyable
import model.gamestate.GameStatus._
import monopoly.MainComponentServer
import monopoly.controller.controllerBaseImpl.{CatGuiMessage, UpdateInfo}
import monopoly.util.{Command, GeneralUtil}


case class WalkCommand(dice: (Int, Int), controller: IController) extends Command {
    private val backupBoard: String = controller.board
    private val backupGameString: String = controller.getCurrentGameMessage

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

    override def doStep(): Unit = {
        controller.controllerState = ROLLED
        controller.catCurrentGameMessage
        controller.publish(new CatGuiMessage)


        val (newBoard, passedGo, newGameState) = MainComponentServer.requestCurrentPlayerWalk(controller.board, dice._1 + dice._2)
        controller.board = newBoard

        if (passedGo) {
            controller.controllerState = PASSED_GO
            controller.catCurrentGameMessage
            controller.publish(new CatGuiMessage)
        }

        controller.controllerState = NEW_FIELD
        controller.catCurrentGameMessage
        controller.publish(new CatGuiMessage)

        // Action return ALREADY_BOUGHT, CAN_BUY or BOUGHT_BY_OTHER
        controller.controllerState = newGameState
        controller.catCurrentGameMessage
        controller.publish(new CatGuiMessage)

        controller.controllerState match {
            case BOUGHT_BY_OTHER =>

                println("PAYING RENT")
//                controller.payRent(controller.getCurrentPlayer.get, controller.getCurrentField.asInstanceOf[IBuyable],
//                    controller.getBuyer(controller.getCurrentField.asInstanceOf[IBuyable]).get)
            case _ =>
        }

//        if (GeneralUtil.getWholeGroups(newPlayer) != Nil) {
            controller.controllerState = CAN_BUILD
            controller.buildStatus = BuildStatus.DEFAULT
//        }
    }
}
