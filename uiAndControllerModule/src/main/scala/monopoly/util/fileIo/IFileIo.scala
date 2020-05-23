package monopoly.util.fileIo

import boardComponent.IBoard
import gamestate.GameStatus.BuildStatus.BuildStatus
import gamestate.GameStatus.GameStatus
import monopoly.controller.IController

trait IFileIo {

    def load(path: String): (IBoard, GameStatus, BuildStatus)

    def save(controller: IController): Unit

}
