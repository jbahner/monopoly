package monopoly.controller

import monopoly.util.Command

class SetupCommand(playerNames: Set[String], controller: IController) extends Command {
    override def doStep(): Unit = {
        controller.loadGame(getClass.getClassLoader.getResource("save-game.json").getPath)
    }

    override def undoStep(): Unit = controller.setBoard(_)

    override def redoStep(): Unit = doStep()

}
