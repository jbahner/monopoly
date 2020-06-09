package monopoly.util.fileIo

import monopoly.controller.IController
import monopoly.controller.gamestate.GameStatus.BuildStatus.BuildStatus
import monopoly.controller.gamestate.GameStatus.GameStatus

trait IFileIo {

    def load(path: String): (String, GameStatus, BuildStatus)

    def save(controller: IController): Unit

}
