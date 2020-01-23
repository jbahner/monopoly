package de.htwg.se.monopoly.util.fileIo

import de.htwg.se.monopoly.controller.GameStatus.BuildStatus.BuildStatus
import de.htwg.se.monopoly.controller.GameStatus.GameStatus
import de.htwg.se.monopoly.controller.IController
import de.htwg.se.monopoly.model.boardComponent.IBoard

trait IFileIo {

    def load(): (IBoard, GameStatus, BuildStatus)
    def save(controller: IController): Unit

}
