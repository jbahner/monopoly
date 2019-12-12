package de.htwg.se.monopoly.controller

import de.htwg.se.monopoly.Monopoly.controller
import de.htwg.se.monopoly.controller.GameStatus.BuildStatus.BuildStatus
import de.htwg.se.monopoly.controller.GameStatus.{BuildStatus, _}
import de.htwg.se.monopoly.controller.commands.{BuildCommand, BuyCommand, SetupCommand, WalkCommand}
import de.htwg.se.monopoly.model.boardComponent._
import de.htwg.se.monopoly.model.playerComponent.Player
import de.htwg.se.monopoly.util.{FieldIterator, GeneralUtil, PlayerIterator, UndoManager}
import play.api.libs.json.{JsValue, Json}

import scala.swing.Publisher

class Controller extends Publisher {
    RentContext.controller = this
    val undoManager = new UndoManager
    var controllerState: GameStatus = START_OF_TURN
    var buildStatus: BuildStatus = BuildStatus.DEFAULT

    var board: Board = _
    var currentDice: (Int, Int) = _
    var currentGameMessageString: String = _

    def setUp() = {
        undoManager.doStep(new SetupCommand(Set("Player1", "Player2"),this))
        controllerState = START_OF_TURN
        publish(new UpdateInfo)
    }

    def getBuyer(buyable: Buyable): Option[Player] = {
        val players = board.playerIt.list
        players.find(p => p.bought.contains(buyable))
    }

    def rollDice(): Unit = {
        val r = scala.util.Random
        currentDice = (r.nextInt(6) + 1, r.nextInt(6) + 1)
        catCurrentGameMessage()
        undoManager.doStep(WalkCommand(currentDice, this))
        publish(new UpdateInfo)
    }

    def nextPlayer(): Unit = {
        board = board.nextPlayerTurn()
        updateCurrentPlayerInfo()
        publish(new UpdateInfo)
    }

    def updateCurrentPlayerInfo() : Unit = {
        controllerState = NEXT_PLAYER
        publish(new UpdateInfo)
        controllerState = START_OF_TURN
        buildStatus = BuildStatus.DEFAULT
        publish(new UpdateGui)
    }

    def payRent(currentPlayer: Player, field: Buyable, receiver: Player) = {
        val payAmount = RentContext.rentStrategy.executeStrategy(field)
        if (currentPlayer.money < payAmount) {
            controllerState = MISSING_MONEY
            publish(new UpdateInfo)
        } else {
            board.replacePlayer(currentPlayer, currentPlayer.copy(money = currentPlayer.money - payAmount))
            board.replacePlayer(receiver, receiver.copy(money = receiver.money + payAmount))
        }
        publish(new UpdateGui)
    }

    def buy(): Unit = {
        val currentPlayer = getCurrentPlayer
        val currentField = getCurrentField.asInstanceOf[Buyable]
        if (currentPlayer.get.money < currentField.getPrice) {
            controllerState = MISSING_MONEY
            publish(new UpdateInfo)
            return
        }
        undoManager.doStep(BuyCommand(currentField, this))

    }

    def getFieldByName(name: String): Option[Field] = {
        board.fields.find(field => field.getName.equals(name))
    }

    def buildHouses(streetName: String, amount: Int): Unit = {
        val field = getFieldByName(streetName)
        if (field.isEmpty || !field.get.isInstanceOf[Street]) {
            buildStatus = BuildStatus.INVALID_ARGS
            return
        }
        val street = field.get.asInstanceOf[Street]
        val buyer = getBuyer(street)
        if (buyer.isEmpty || !buyer.get.equals(getCurrentPlayer.get))
            buildStatus = BuildStatus.NOT_OWN
        else if (street.numHouses + amount > 5)
            buildStatus = BuildStatus.TOO_MANY_HOUSES
        else if (getCurrentPlayer.get.money < street.houseCost * amount)
            buildStatus = BuildStatus.MISSING_MONEY
        else
            undoManager.doStep(BuildCommand(street, amount, this))
        publish(new UpdateInfo)
    }

    def getCurrentField: Field = board.currentPlayer.currentField

    def getCurrentPlayer(): Option[Player] = {
        Option(board.currentPlayer)
    }

    def catCurrentGameMessage(): String = {
        controllerState match {
            case START_OF_TURN => currentGameMessageString = userInputString("\"r\" to roll, \"q\" to quit, \"u\" to undo or \"re\" to redo!")
                currentGameMessageString
            case ROLLED => currentGameMessageString = infoString("Rolled: " + currentDice._1 + " and " + currentDice._2+ "\n")
                currentGameMessageString
            case PASSED_GO => currentGameMessageString += infoString("Received 200€ by passing Go\n")
                currentGameMessageString
            case NEW_FIELD => currentGameMessageString = infoString("New Field: " + getCurrentField.getName + "\n")
                currentGameMessageString
            case ALREADY_BOUGHT => currentGameMessageString += infoString("You already own this street\n")
                currentGameMessageString
            case CAN_BUY =>
                val field: Buyable = getCurrentField.asInstanceOf[Buyable]
                currentGameMessageString += userInputString("Do you want to buy %s for %d€? (Y/N)".format(field.getName, field.getPrice) + "\n")
                currentGameMessageString
            case BOUGHT_BY_OTHER => {
                val field = getCurrentField.asInstanceOf[Buyable]
                currentGameMessageString += infoString("Field already bought by " + getBuyer(field).get.name + ".\n" +
                  "You must pay " + RentContext.rentStrategy.executeStrategy(field) + " rent!\n")
                currentGameMessageString
            }
            case CAN_BUILD =>
                buildStatus match {
                    case BuildStatus.DEFAULT => val wholeGroups = GeneralUtil.getWholeGroups(getCurrentPlayer.get)
                        currentGameMessageString += userInputString("You can build on: \n" + buildablesToString(wholeGroups) +
                          "\nType the name of the street and the amount of houses you want to build. Press \"q\" to quit, \"u\" to undo or \"re\" to redo.\n")
                        currentGameMessageString
                    case BuildStatus.BUILT => currentGameMessageString = infoString("Successfully built!\n")
                        currentGameMessageString
                    case BuildStatus.INVALID_ARGS => currentGameMessageString
                    case BuildStatus.NOT_OWN => currentGameMessageString = errorString("You don't own this street!")
                        currentGameMessageString
                    case BuildStatus.TOO_MANY_HOUSES => currentGameMessageString += errorString("There can only be 5 houses on a street\n")
                        currentGameMessageString
                    case BuildStatus.MISSING_MONEY => currentGameMessageString += errorString("You don't have enough money!\n")
                        currentGameMessageString
                    //TODO delete if not needed
                    //case BuildStatus.DONE => currentGameMessageString = ""
                    //    currentGameMessageString
                }
            case DONE => currentGameMessageString = turnString(getCurrentPlayer.get.name + " ended his turn.\n\n")
                currentGameMessageString
            case NEXT_PLAYER => currentGameMessageString = turnString("Next player: " + getCurrentPlayer.get.name + "\n") + playerInfoString(getCurrentPlayer.get.getDetails)
                currentGameMessageString
            case MISSING_MONEY => currentGameMessageString = "You do not have enough money!"
                currentGameMessageString
            case BOUGHT => currentGameMessageString = infoString("Successfully bought the street")
                currentGameMessageString
            case NOTHING => currentGameMessageString = ""
                currentGameMessageString
        }
    }

    def turnString(message: String): String = Console.BOLD + Console.UNDERLINED + Console.GREEN + message + Console.RESET

    def playerInfoString(message: String): String = Console.BOLD + Console.MAGENTA + message + Console.RESET

    def infoString(message: String): String = Console.BOLD + Console.BLUE + message + Console.RESET

    def userInputString(message: String): String = Console.BOLD + Console.YELLOW + message + Console.RESET

    def errorString(message: String): String = Console.BOLD + Console.RED + message + Console.RESET

    def getCurrentGameMessage(): String = {
        currentGameMessageString
    }

    def buildablesToString(buildables: List[Set[String]]): String = {
        val sb = new StringBuilder()
        buildables.foreach(set => {
            sb.append("\t")
            set.foreach {
                street =>
                    sb.append(street)
                      .append(" (" + getFieldByName(street).get.asInstanceOf[Street].houseCost + "€)")
                      .append("   ")
            }
            sb.append("\n")
        })
        sb.toString()
    }

    def getJSON(): JsValue = {
        Json.obj(
            "board" -> Json.obj(
                "state" -> controllerState.toString,
                "current_player" -> getCurrentPlayer.get.name,
                "players" -> board.playerIt.list.map(p => p.getJSON).toList
            )
        )
    }
}