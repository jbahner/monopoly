package de.htwg.se.monopoly

import de.htwg.se.monopoly.controller.{Controller, UpdateInfo}
import de.htwg.se.monopoly.controller.GameStatus.START_OF_TURN
import de.htwg.se.monopoly.view.Tui

import scala.io.StdIn.readLine

object Monopoly {


    val controller = new Controller()
    val tui = new Tui(controller)

    def main(args: Array[String]): Unit = {

        controller.controllerState = START_OF_TURN

        while (true) {
            controller.publish(new UpdateInfo)
            val input = readLine()
            tui.processInput(input)
        }
    }
}
