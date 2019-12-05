package de.htwg.se.monopoly.controller

import de.htwg.se.monopoly.controller.GameStatus.BuildStatus.BuildStatus
import de.htwg.se.monopoly.controller.GameStatus.{BuildStatus, _}
import de.htwg.se.monopoly.controller.commands.{WalkCommand, SetupCommand}
import de.htwg.se.monopoly.model.boardComponent._
import de.htwg.se.monopoly.model.playerComponent.Player
import de.htwg.se.monopoly.util.{FieldIterator, GeneralUtil, PlayerIterator, UndoManager}
import play.api.libs.json.{JsValue, Json}

import scala.swing.Publisher
import scala.swing.event.Event

class Controller extends Publisher {
    RentContext.controller = this
    val undoManager = new UndoManager
    var controllerState: GameStatus = START_OF_TURN
    var buildStatus: BuildStatus = BuildStatus.DEFAULT

    var board: Board = _
    // TODO check if 2nd variable needed
    var currentDice: (Int, Int) = _
    var currentGameMessageString: String = _

    def setUp() = undoManager.doStep(new SetupCommand(Set("Player1", "Player2"),this))

    def getBuyer(buyable: Buyable): Option[Player] = {
        val players = board.playerIt.list
        players.find(p => p.bought.contains(buyable))
    }

    def rollDice(): Unit = {
        val r = scala.util.Random
        currentDice = (r.nextInt(6) + 1, r.nextInt(6) + 1)
        catCurrentGameMessage()
        undoManager.doStep(WalkCommand(currentDice, this))
    }

    def nextPlayer(): Unit = {
        board = board.nextPlayerTurn()
        controllerState = NEXT_PLAYER
        publish(new UpdateInfo)
        controllerState = START_OF_TURN
        buildStatus = BuildStatus.DEFAULT
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
    }

    def buy(): Unit = {
        val currentPlayer = getCurrentPlayer
        val currentField = getCurrentField.asInstanceOf[Buyable]
        if (currentPlayer.money < currentField.getPrice) {
            controllerState = MISSING_MONEY
            publish(new UpdateInfo)
            return
        }
        var newField = currentField
        currentField match {
            case street: Street => newField = street.copy(isBought = true)
            case building: Building => newField = building.copy(isBought = true)
        }
        val newPlayer: Player = currentPlayer.copy(money = currentPlayer.money - newField.getPrice,
            bought = currentPlayer.bought + newField)
        board = board.replacePlayer(currentPlayer, newPlayer).copy(currentPlayer = newPlayer)
        board = board.replaceField(currentField, newField)
        controllerState = BOUGHT
        publish(new UpdateInfo)
    }

    def getFieldByName(name: String): Option[Field] = {
        board.fields.find(field => field.getName.equals(name))
    }

    def tryToBuildHouses(streetName: String, amount: Int) = {
        buildStatus = buildHouses(streetName, amount)
    }

    def buildHouses(streetName: String, amount: Int): BuildStatus = {
        val field = getFieldByName(streetName)
        if (field.isEmpty || !field.get.isInstanceOf[Street])
            return BuildStatus.INVALID_ARGS
        val street = field.get.asInstanceOf[Street]
        val buyer = getBuyer(street)
        if (buyer.isEmpty || !buyer.get.equals(getCurrentPlayer))
            return BuildStatus.NOT_OWN
        if (street.numHouses + amount > 5)
            return BuildStatus.TOO_MANY_HOUSES
        if (getCurrentPlayer.money < street.houseCost * amount)
            return BuildStatus.MISSING_MONEY
        board = board.replaceField(field = street, newField = street.buyHouses(amount))
        board = board.replacePlayer(getCurrentPlayer, getCurrentPlayer.copy(money = getCurrentPlayer.money - street.houseCost * amount))
        BuildStatus.BUILT
    }

    def getCurrentField: Field = board.currentPlayer.currentField

    def getCurrentPlayer: Player = board.currentPlayer

    def catCurrentGameMessage(): String = {
        controllerState match {
            case START_OF_TURN => currentGameMessageString = "\"r\" to roll, \"q\" to quit!"
                currentGameMessageString
            case ROLLED => currentGameMessageString += "Rolled: " + currentDice._1 + " and " + currentDice._2+ "\n"
                currentGameMessageString
            case PASSED_GO => currentGameMessageString += "Received 200€ by passing Go\n"
                currentGameMessageString
            case NEW_FIELD => currentGameMessageString = "New Field: " + getCurrentField.getName + "\n"
                currentGameMessageString
            case ALREADY_BOUGHT => currentGameMessageString += "You already own this street\n"
                currentGameMessageString
            case CAN_BUY =>
                val field: Buyable = getCurrentField.asInstanceOf[Buyable]
                currentGameMessageString += "Do you want to buy %s for %d€? (Y/N)".format(field.getName, field.getPrice) + "\n"
                currentGameMessageString
            case BOUGHT_BY_OTHER => {
                val field = getCurrentField.asInstanceOf[Buyable]
                currentGameMessageString += "Field already bought by " + getBuyer(field).get.name + ".\n" +
                  "You must pay " + RentContext.rentStrategy.executeStrategy(field) + " rent!\n"
                currentGameMessageString
            }
            case CAN_BUILD =>
                buildStatus match {
                    case BuildStatus.DEFAULT => val wholeGroups = GeneralUtil.getWholeGroups(getCurrentPlayer)
                        currentGameMessageString += "You can build on: \n" + buildablesToString(wholeGroups) +
                          "\nType the name of the street and the amount of houses you want to build. Press 'q' to quit.\n"
                        currentGameMessageString
                    case BuildStatus.BUILT => currentGameMessageString = "Successfully built!\n"
                        currentGameMessageString
                    case BuildStatus.INVALID_ARGS => currentGameMessageString
                    case BuildStatus.NOT_OWN => currentGameMessageString = "You don't own this street!"
                        currentGameMessageString
                    case BuildStatus.TOO_MANY_HOUSES => currentGameMessageString += "There can only be 5 houses on a street\n"
                        currentGameMessageString
                    case BuildStatus.MISSING_MONEY => currentGameMessageString += "You don't have enough money!\n"
                        currentGameMessageString
                    //TODO delete if not needed
                    //case BuildStatus.DONE => currentGameMessageString = ""
                    //    currentGameMessageString
                }
            case DONE => currentGameMessageString = getCurrentPlayer.name + " ended his turn.\n\n"
                currentGameMessageString
            case NEXT_PLAYER => currentGameMessageString = "Next player: " + getCurrentPlayer.name + "\n" + getCurrentPlayer.getDetails
                currentGameMessageString
            case MISSING_MONEY => currentGameMessageString = "You do not have enough money!"
                currentGameMessageString
            case BOUGHT => currentGameMessageString = "Successfully bought the street"
                currentGameMessageString
            case NOTHING => currentGameMessageString = ""
                currentGameMessageString
        }
    }

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
                "current_player" -> getCurrentPlayer.name,
                "players" -> board.playerIt.list.map(p => p.getJSON).toList
            )
        )
    }
}

class UpdateInfo extends Event
