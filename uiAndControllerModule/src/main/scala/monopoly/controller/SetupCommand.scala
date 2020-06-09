package monopoly.controller

import modelComponent.boardComponent.IBoard
import monopoly.util.Command

class SetupCommand(playerNames: Set[String], controller: IController) extends Command {
    override def doStep(): IBoard = {
        controller.loadGame(getClass.getClassLoader.getResource("save-game.json").getPath)
    }

    override def undoStep(): IBoard = throw new UnsupportedOperationException

    override def redoStep(): IBoard = doStep()

}
