package de.htwg.se.monopoly.view

import de.htwg.se.monopoly.controller.GameStatus._
import de.htwg.se.monopoly.controller.{Controller, GameStatus, UpdateInfo}

import scala.swing.Reactor

class Tui(controller: Controller) extends Reactor {
    listenTo(controller);
    controller.setUp()
    playerInfo(message(NEXT_PLAYER) + controller.getCurrentPlayer.getDetails)


    def processInput(input: String) = {

        controller.controllerState match {

            case START_OF_TURN => {
                input match {

                    case "r" => {
                        //val (d1, d2) = controller.rollDice()
                        //info("Rolled: " + d1 + " and " + d2)
                        controller.rollDice()
                        //controller.processRoll(d1, d2)
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
                controller.buildStatus = GameStatus.BuildStatus.DEFAULT
                if (!input.equals("q")) {
                    val args = input.split("_")
                    if (args.length != 2) {
                        userInput("Invalid argument for street or amount of houses!\n" +
                          "<street name>_<amount of houses>")
                        controller.buildStatus = GameStatus.BuildStatus.INVALID_ARGS
                    }
                    else {
                        controller.tryToBuildHouses(args(0), args(1).toInt)
                    }
                }
                else {
                    controller.buildStatus = GameStatus.BuildStatus.DONE
                    controller.controllerState = GameStatus.DONE
                    controller.publish(new UpdateInfo)
                    controller.nextPlayer()
                }
            case DONE => controller.nextPlayer()

        }
    }

    reactions += {
        case event: UpdateInfo => info(controller.catCurrentGameMessage())
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
