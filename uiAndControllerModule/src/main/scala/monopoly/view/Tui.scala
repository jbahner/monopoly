package monopoly.view

import monopoly.MainComponentServer
import monopoly.controller.IController
import monopoly.controller.controllerBaseImpl.UpdateInfo
import monopoly.controller.gamestate.GameStatus
import monopoly.controller.gamestate.GameStatus._

import scala.swing.Reactor
import scala.util.{Failure, Success, Try}

class Tui(controller: IController) extends Reactor with IUi {
    listenTo(controller)
    playerInfo(message(NEXT_PLAYER) + MainComponentServer.getCurrentPlayerDetails(controller.getBoard()))


    def processInput(input: String): Unit = {

        controller.getControllerState match {

            case START_OF_TURN =>
                input match {

                    case "r" => controller.rollDice
                    case "q" | null => System.exit(0)
                    case "u" => controller.getUndoManager.undoStep()
                    case "re" => controller.getUndoManager.redoStep()
                    case other => error("Wrong input: " + other)
                }

            case CAN_BUY =>
                input match {
                    case "y" | "Y" => controller.buy
                    case "n" | "N" =>
                    case "u" => controller.getUndoManager.undoStep()
                    case "re" => controller.getUndoManager.redoStep()
                    case _ => userInput("Y / N")
                }

            case CAN_BUILD =>
                controller.buildStatus = GameStatus.BuildStatus.DEFAULT
                input match {
                    case "q" =>
                        controller.buildStatus = GameStatus.BuildStatus.DONE
                        controller.controllerState = GameStatus.DONE
                        controller.publish(new UpdateInfo)
                        controller.nextPlayer
                    case "u" => controller.getUndoManager.undoStep()
                    case "re" => controller.getUndoManager.redoStep()
                    case other =>
                        val args = other.split("_")
                        if (args.length != 2) {
                            userInput("Invalid argument for street or amount of houses!\n" +
                                "<street name>_<amount of houses>")
                            controller.buildStatus = GameStatus.BuildStatus.INVALID_ARGS
                        }
                        else {
                            val arg2 = Try(args(1).toInt)

                            arg2 match {
                                case Success(v: Int) => controller.buildHouses(args(0), v)
                                case Failure(_) =>
                                    userInput("Invalid argument for street or amount of houses!\n" +
                                        "<street name>_<amount of houses>")
                            }
                        }
                }
            case DONE => controller.nextPlayer

        }
    }

    reactions += {
        case event: UpdateInfo => info(controller.catCurrentGameMessage())
    }

    def userInput(message: String): Unit = {
        Console.println(Console.BOLD + Console.YELLOW + message + Console.RESET)
    }

    def error(message: String): Unit = {
        Console.println(Console.BOLD + Console.RED + message + Console.RESET)
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

    override def closeOperation(): Unit = {
        controller.shutdown()
    }
}
