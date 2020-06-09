package monopoly.controller

import modelComponent.boardComponent.IBoard
import monopoly.util.Command

class SetupCommand(playerNames: Set[String], controller: IController) extends Command {
    override def doStep(): String = {
        controller.loadGame(getClass.getClassLoader.getResource("save-game.json").getPath)
    }

    override def undoStep(): String = throw new UnsupportedOperationException

    override def redoStep(): String = doStep()

}
