package monopoly.util.fileIo

import model.gamestate.GameStatus.BuildStatus.BuildStatus
import model.gamestate.GameStatus.GameStatus
import monopoly.controller.IController

trait IFileIo {

    def load(path: String): (String, GameStatus, BuildStatus)

    def save(controller: IController): Unit

}
