package de.htwg.se.monopoly

import de.htwg.se.monopoly.controller.{Controller, UpdateInfo}
import de.htwg.se.monopoly.controller.GameStatus.START_OF_TURN
import de.htwg.se.monopoly.view.{Gui, Tui}

import scala.io.StdIn.readLine

object Monopoly {


    val controller = new Controller()
    controller.setUp()
    val tui = new Tui(controller)
    val gui = new Gui(controller)

    def main(args: Array[String]): Unit = {


        controller.publish(new UpdateInfo)


        while (true) {
            val input = readLine()
            tui.processInput(input)
        }
    }
}
