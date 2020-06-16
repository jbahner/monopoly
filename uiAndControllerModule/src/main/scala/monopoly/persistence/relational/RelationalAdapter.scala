package monopoly.persistence.relational

import monopoly.controller.IController
import monopoly.controller.gamestate.GameStatus.BuildStatus.BuildStatus
import monopoly.controller.gamestate.GameStatus.GameStatus
import monopoly.persistence.IDaoController

object RelationalAdapter extends IDaoController {

    private val controllerMapping = ControllerMapping

    override def saveController(controller: IController): Boolean = {
        controllerMapping.saveController(controller)
    }

    override def loadController(): (GameStatus, BuildStatus, (Int, Int), String) = {
        controllerMapping.loadController() match {
            case None => println("Loading Controller Failed")
                throw new RuntimeException("Loading Controller Failed")
            case Some(controller) => controller
        }
    }
}
