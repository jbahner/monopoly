package monopoly.controller

import java.io.File

import monopoly.util.Command

import scala.io.Source

class SetupCommand(playerNames: Set[String], controller: IController) extends Command {
    override def undoStep(): String = throw new UnsupportedOperationException

    override def redoStep(): String = doStep()

    override def doStep(): String = {
        controller.loadDefaultGame()
    }

}
