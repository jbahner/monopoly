package de.htwg.se.monopoly.view

import de.htwg.se.monopoly.controller.Controller
import de.htwg.se.monopoly.controller.GameStatus._
import de.htwg.se.monopoly.model.boardComponent.{Buyable, Street}
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
            case ALREADY_BOUGHT => info("You already own this street")
            case CAN_BUY => askForBuy(controller.getCurrentField.asInstanceOf[Buyable])
            case BOUGHT_BY_OTHER =>  {
                val field = controller.getCurrentField.asInstanceOf[Buyable]
                info("Field already bought by " + controller.getBuyer(field).get.name + ".")
                info("You must pay " + field.getPrice + " rent!")
                controller.payRent()
            }
            case MISSING_MONEY => info("You do not have enough money!") // TODO: mortgage/sell houses/lose
            case BOUGHT => info("Successfully bought " + controller.getCurrentField.getName)
            case PASSED_GO => info("Received 200€ by passing Go")
            case CAN_BUILD => askForBuild()
            case NOTHING =>
        }
    }

    def askForBuild() : Unit = {
        val wholeGroups = controller.getWholeGroups(controller.getCurrentPlayer)
        info("You can build on: \n" + buildablesToString(wholeGroups))
        var finished = false
        while(!finished) {
            userInput("Type the name of the street and the amount of houses you want to build. Press 'q' to quit.")
            val input = readLine()
            val args = input.split(" ")
            if(args(0).equals("q"))
                finished = true
            else {
                if(args.length != 2)
                    error("Invalid number of arguments: " + args.length)
                else {
                    val street = args(0)
                    val amount = args(1)
                    // TODO must build equally

                    controller.buildHouses(street, amount.toInt) match {
                        case BuildStatus.BUILT => info("Successfully built " + args(1) + " houses!")
                        case BuildStatus.INVALID_ARGS => error("Invalid argument for street or amount of houses!")
                        case BuildStatus.NOT_OWN => error("You don't own this street!")
                        case BuildStatus.TOO_MANY_HOUSES => error("There can only be 5 houses on a street")
                        case BuildStatus.MISSING_MONEY => error("You don't have enough money!")
                    }
                }
            }
        }
    }

    def buildablesToString(buildables : List[Set[String]]) : String = {
        val sb = new StringBuilder()
        buildables.foreach(set => {
            sb.append("\t")
            set.foreach {
                street => sb.append(street).append(" (" + controller.getFieldByName(street).get.asInstanceOf[Street].houseCost +"€)").append("   ")
            }
            sb.append("\n")
        })
        sb.toString()
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
