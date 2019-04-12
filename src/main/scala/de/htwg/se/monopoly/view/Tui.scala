package de.htwg.se.monopoly.view

import de.htwg.se.monopoly.controller.Controller
import de.htwg.se.monopoly.controller.GameStatus._
import de.htwg.se.monopoly.util.Observer

import scala.io.StdIn.readLine

class Tui(controller: Controller) extends Observer {
    controller.add(this)
    controller.setUp()
    while (true) {
        println("\"r\" to roll, \"q\" to quit!")
        val input = readLine()
        processInput(input)
    }

    def processInput(input: String): Unit = {
        input match {
            case "r" => {
                val (d1, d2) = controller.rollDice()
                println("Rolled: " + d1 + " and " + d2)
                controller.processRoll(d1, d2)
            }
            case "q" => System.exit(0)
            case other => println("Wrong input: " + other)
        }
    }

    override def update(gameStatus: GameStatus): Unit = {
        gameStatus match {
            case NEXTPLAYER => println(message(NEXTPLAYER) + controller.getCurrentPlayer.toString)
            case NEWFIELD => println("New Field: \n" + controller.getCurrentField.getName)
        }
    }

}
