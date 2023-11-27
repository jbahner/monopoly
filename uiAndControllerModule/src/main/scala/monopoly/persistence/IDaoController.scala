package monopoly.persistence

import monopoly.controller.IController
import monopoly.controller.gamestate.GameStatus.BuildStatus.BuildStatus
import monopoly.controller.gamestate.GameStatus.GameStatus

trait IDaoController {

    def saveController(controller: IController): Boolean

    def loadController(): (GameStatus, BuildStatus, (Int, Int), String)

}
