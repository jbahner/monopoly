package de.htwg.se.monopoly.controller

import de.htwg.se.monopoly.controller.GameStatus.BuildStatus
import de.htwg.se.monopoly.controller.GameStatus.BuildStatus.BuildStatus
import de.htwg.se.monopoly.controller.GameStatus._
import de.htwg.se.monopoly.model.boardComponent.{ActionField, Board, Building, Buyable, Field, Street}
import de.htwg.se.monopoly.model.playerComponent.Player
import de.htwg.se.monopoly.util.{FieldIterator, Observable, PlayerIterator, GeneralUtil}

class Controller extends Observable {

    var controllerState: GameStatus = _
    var buildStatus: BuildStatus = BuildStatus.DEFAULT

    var board: Board = _
    // TODO check if 2nd variable needed
    var currentDice: Int = _
    var currentGameMessageString : String = _

    def setUp() = {

        val go = ActionField("Go")
        var fields = List[Field](go)

        for (i <- 1 to 3)
            fields = fields :+ Street(name = "Street" + i,
                price = 50 * i,
                rentCosts = getRentArray(50 * i),
                houseCost = 25 * i,
                isBought = true)

        for (i <- 4 to 6)
            fields = fields :+ Street(name = "Street" + i,
                price = 50 * i,
                rentCosts = getRentArray(50 * i),
                houseCost = 25 * i,
                isBought = true)

        for (i <- 7 to 9)
            fields = fields :+ Street(name = "Street" + i,
                price = 50 * i,
                rentCosts = getRentArray(50 * i),
                houseCost = 25 * i,
                isBought = true)

        var player1 = Player(name = "Player1",
            money = 1500,
            currentField = fields.head,
            bought = Set(),
            fieldIt = new FieldIterator(fields))

        var player2 = Player(name = "Player2",
            money = 1500,
            currentField = fields.head,
            bought = Set(),
            fieldIt = new FieldIterator(fields))

        // For paying rent testing purposes
        for (i <- 1 to 6) {
            player1 = player1.copy(bought = player1.bought + fields(i).asInstanceOf[Street])
        }
        for (i <- 7 to 9)
            player2 = player2.copy(bought = player2.bought + fields(i).asInstanceOf[Street])
        board = Board(fields, player1, new PlayerIterator(Array(player1, player2)))
    }

    def rollDice(): (Int, Int) = {
        val r = scala.util.Random
        val (d1, d2) = (r.nextInt(6) + 1, r.nextInt(6) + 1)
        currentDice = d1 + d2
        (d1, d2)
    }

    // This is only for testing purposes
    private def getRentArray(cost: Int): Array[Int] = {
        val step = cost / 10
        Array(step, step * 2, step * 3, step * 4, step * 5)
    }

    def getBuyer(buyable: Buyable): Option[Player] = {
        val players = board.playerIt.list
        players.find(p => p.bought.contains(buyable))
    }

    def processRoll(firstDice: Int, secondDice: Int): Unit =
    {
        val player = board.currentPlayer
        val (newPlayer, passedGo) = player.walk(firstDice + secondDice)

        if (passedGo) {
            controllerState = PASSED_GO
            catCurrentGameMessage()
        }

        board = board.replacePlayer(player, newPlayer)
        controllerState = NEW_FIELD
        catCurrentGameMessage()

        val newField = getCurrentField
        // Action return ALREADY_BOUGHT, CAN_BUY or BOUGHT_BY_OTHER
        controllerState = newField.action(newPlayer)
        catCurrentGameMessage()

        controllerState match {
            case BOUGHT_BY_OTHER =>
                payRent(getCurrentPlayer, getCurrentField.asInstanceOf[Buyable], getBuyer(getCurrentField.asInstanceOf[Buyable]).get)
            case _ =>
        }

        if (GeneralUtil.getWholeGroups(newPlayer) != Nil) {
            controllerState = CAN_BUILD
            buildStatus = BuildStatus.DEFAULT
        }
    }

    def nextPlayer(): Unit = {
        board = board.nextPlayerTurn()
        controllerState = NEXT_PLAYER
        notifyObservers()
        controllerState = START_OF_TURN
        buildStatus = BuildStatus.DEFAULT
    }

    def payRent(currentPlayer: Player, field: Buyable, receiver: Player) = {
        var payAmount = 0
        field match {
            case street: Street =>
                if (street.numHouses == 0 && GeneralUtil.hasWholeGroup(receiver, field.getName))
                    payAmount = field.getRent() * 2
                else
                    payAmount = field.getRent()
            case building: Building =>
                // TODO check this is correct
                if (GeneralUtil.hasWholeGroup(receiver, field.getName))
                    payAmount = currentDice * 10
                else
                    payAmount = currentDice * 4
        }
        if (currentPlayer.money < payAmount) {
            controllerState = MISSING_MONEY
            notifyObservers()
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
            notifyObservers()
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
        notifyObservers()
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
                    "You must pay " + field.getPrice + " rent!\n"
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

    def getCurrentGameMessage() : String = {
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
}
