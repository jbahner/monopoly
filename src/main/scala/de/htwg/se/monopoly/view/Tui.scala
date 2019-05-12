package de.htwg.se.monopoly.view

import de.htwg.se.monopoly.controller.Controller
import de.htwg.se.monopoly.controller.GameStatus._
import de.htwg.se.monopoly.model.boardComponent.Buyable
import de.htwg.se.monopoly.model.playerComponent.Player
import de.htwg.se.monopoly.util.Observer

import scala.io.StdIn.readLine

class Tui(controller: Controller) extends Observer {
    controller.add(this)
    controller.setUp()
    playerInfo(message(NEXT_PLAYER) + controller.getCurrentPlayer.getDetails)
    while (true) {
        userInput("\"r\" to roll, \"q\" to quit!")
        val input = readLine()
        processInput(input)
    }

    def processInput(input: String): Unit = {
        input match {
            case "r" => {
                val (d1, d2) = controller.rollDice()
                info("Rolled: " + d1 + " and " + d2)
                controller.processRoll(d1, d2)
            }
            case "q" => System.exit(0)
            case other => error("Wrong input: " + other)
        }
    }

    override def update(gameStatus: GameStatus): Unit = {
        gameStatus match {
            case NEXT_PLAYER => turn("Next player: " + controller.getCurrentPlayer.name); playerInfo(controller.getCurrentPlayer.getDetails)
            case NEW_FIELD =>  playerInfo("New Field: " + controller.getCurrentField.getName)
            case CAN_BUY => askForBuy(controller.getCurrentField.asInstanceOf[Buyable])
            case BOUGHT_BY_OTHER => info("Field already bought!")//TODO pay rent
            case BOUGHT => info("Successfully bought " + controller.getCurrentField.getName)
            case NOTHING =>
        }
    }

    def turn(message : String): Unit = {
        Console.println(Console.BOLD + Console.UNDERLINED + Console.GREEN + message + Console.RESET)
    }

    def playerInfo(message: String): Unit = {
        Console.println(Console.BOLD + Console.MAGENTA + message + Console.RESET)
    }

    def info(message : String): Unit = {
        Console.println(Console.BOLD + Console.BLUE + message + Console.RESET)
    }

    def userInput(message : String): Unit = {
        Console.println(Console.BOLD + Console.YELLOW + message + Console.RESET)
    }

    def error(message : String): Unit = {
        Console.println(Console.BOLD + Console.RED + message + Console.RESET)
    }

    def askForBuy(field : Buyable) : Unit =  {
        userInput("Do you want to buy %s for %d€? (Y/N)".format(field.getName, field.getPrice))
        var done = false
        while(!done) {
            val input = readLine()
            input.toLowerCase match {
                case "y" => controller.buy(); done = true
                case "n" => done = true
                case _ => userInput("Y / N"); done = false
            }
        }
    }

}
