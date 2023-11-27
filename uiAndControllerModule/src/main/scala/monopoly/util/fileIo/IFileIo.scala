package monopoly.util.fileIo

import java.io.InputStream

import monopoly.controller.IController
import monopoly.controller.gamestate.GameStatus.BuildStatus.BuildStatus
import monopoly.controller.gamestate.GameStatus.GameStatus

trait IFileIo {

    def load(is: InputStream, loadFullGame: Boolean): (String, GameStatus, BuildStatus)

    def save(controller: IController): Unit

}
