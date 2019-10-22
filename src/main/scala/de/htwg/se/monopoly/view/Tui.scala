package de.htwg.se.monopoly.view

import de.htwg.se.monopoly.controller.{Controller, GameStatus}
import de.htwg.se.monopoly.controller.GameStatus._
import de.htwg.se.monopoly.model.boardComponent.{Buyable, Street}
import de.htwg.se.monopoly.util.Observer

import scala.io.StdIn.readLine

class Tui(controller: Controller) extends Observer {
    controller.add(this)
    controller.setUp()
    playerInfo(message(NEXT_PLAYER) + controller.getCurrentPlayer.getDetails)

    while (true) {
        controller.controllerState = START_OF_TURN
        userInput("\"r\" to roll, \"q\" to quit!")
        val input = readLine()
        processInput(input)
    }

    def readInput(): String = {
        readLine()
    }


    def processInput(input: String) = {

        controller.controllerState match {

            case START_OF_TURN => {
                input match {

                    case "r" => {
                        val (d1, d2) = controller.rollDice()
                        info("Rolled: " + d1 + " and " + d2)
                        controller.playerTurn(d1, d2)
                    }
                    case "q" => System.exit(0)
                    case other => error("Wrong input: " + other)
                }
            }

            case CAN_BUY =>
                input match {
                    case "y" | "Y" => controller.buy()
                    case "n" | "N" =>
                    case _ => userInput("Y / N")
                }

            case CAN_BUILD =>
                val args = input.split(" ")

                if (!input.equals("q"))  {
                    if (args.length != 2) {
                        userInput("<street> <amout of houses>")
                    }

                    controller.tryToBuildHouses(args(0), args(1).toInt)
                }
                else {
                    controller.buildStatus = GameStatus.BuildStatus.DONE
                }

        }
    }

    override def update(): Unit = {

        val currentMessage: String = controller.currentGameMessage()

        controller.controllerState match {
            case PASSED_GO => info(currentMessage)
            case NEW_FIELD => playerInfo(currentMessage)
            case CAN_BUY => userInput(currentMessage)
                processInput(readInput())
            case ALREADY_BOUGHT => info(currentMessage)
            case BOUGHT_BY_OTHER => info(currentMessage)
            case CAN_BUILD =>
                info(currentMessage)
                //TODO make it possible to buy multiple houses in one turn
                if (controller.buildStatus != GameStatus.BuildStatus.BUILT && controller.buildStatus != GameStatus.BuildStatus.DONE)
                    processInput(readInput())

            case NEXT_PLAYER => turn(currentMessage)
            case MISSING_MONEY => info(currentMessage) // TODO: mortgage/sell houses/lose
            case BOUGHT => info(currentMessage)
            case NOTHING =>
        }

        /*
                controller.controllerState match {
                    case NEXT_PLAYER => turn("Next player: " + controller.getCurrentPlayer.name); playerInfo(controller.getCurrentPlayer.getDetails)
                    case NEW_FIELD =>  playerInfo("New Field: " + controller.getCurrentField.getName)
                    case ALREADY_BOUGHT => info("You already own this street")
                    case CAN_BUY => askForBuy(controller.getCurrentField.asInstanceOf[Buyable])
                    case BOUGHT_BY_OTHER =>  {
                        val field = controller.getCurrentField.asInstanceOf[Buyable]
                        info("Field already bought by " + controller.getBuyer(field).get.name + ".")
                        info("You must pay " + field.getPrice + " rent!")
                    }
                    case MISSING_MONEY => info("You do not have enough money!")
                    case BOUGHT => info("Successfully bought " + controller.getCurrentField.getName)
                    case PASSED_GO => info("Received 200€ by passing Go")
                    case CAN_BUILD => askForBuild()
                    case NOTHING =>
                }
         */
    }

    def turn(message: String): Unit = {
        Console.println(Console.BOLD + Console.UNDERLINED + Console.GREEN + message + Console.RESET)
    }

    def playerInfo(message: String): Unit = {
        Console.println(Console.BOLD + Console.MAGENTA + message + Console.RESET)
    }

    def info(message: String): Unit = {
        Console.println(Console.BOLD + Console.BLUE + message + Console.RESET)
    }

    def userInput(message: String): Unit = {
        Console.println(Console.BOLD + Console.YELLOW + message + Console.RESET)
    }

    def error(message: String): Unit = {
        Console.println(Console.BOLD + Console.RED + message + Console.RESET)
    }


}
