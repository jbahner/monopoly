package de.htwg.se.monopoly

import de.htwg.se.monopoly.controller.IController
import de.htwg.se.monopoly.controller.controllerBaseImpl.{Controller, UpdateInfo}
import de.htwg.se.monopoly.view.{Gui, IUi, Tui}

import scala.io.StdIn.readLine

object Monopoly {

    val controller: IController = new Controller()
    controller.setUp("fields.json")
    val tui: IUi = new Tui(controller)
    val gui: IUi = new Gui(controller)

    def main(args: Array[String]): Unit = {

        controller.publish(new UpdateInfo)

        while (true) {
            val input = readLine()
            tui.processInput(input)
        }
    }
}
