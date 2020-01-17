package de.htwg.se.monopoly.util.fileIo

import de.htwg.se.monopoly.controller.IController

trait IFileIo {

    def load: IController
    def save(controller: IController): Unit

}
