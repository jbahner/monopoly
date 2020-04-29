package de.htwg.se.monopoly.controller

import de.htwg.se.monopoly.util.Command

class SetupCommand(playerNames: Set[String], controller: IController) extends Command {
    override def doStep(): Unit = {
        controller.loadGame(getClass.getClassLoader.getResource("save-game.json").getPath)
    }

    override def undoStep(): Unit = controller.setBoard(_)

    override def redoStep(): Unit = doStep()

}
