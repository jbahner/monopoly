package de.htwg.se.monopoly

import de.htwg.se.monopoly.controller.Controller
import de.htwg.se.monopoly.view.Tui

object Monopoly {

    def main(args: Array[String]) {
        val controller = new Controller()
        val tui = new Tui(controller)
    }
}
