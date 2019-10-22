package de.htwg.se.monopoly.controller

import de.htwg.se.monopoly.controller.GameStatus.BuildStatus
import de.htwg.se.monopoly.controller.GameStatus.BuildStatus.BuildStatus
import de.htwg.se.monopoly.controller.GameStatus._
import de.htwg.se.monopoly.model.boardComponent.{ActionField, Board, Building, Buyable, Field, Street}
import de.htwg.se.monopoly.model.playerComponent.Player
import de.htwg.se.monopoly.util.{FieldIterator, Observable, PlayerIterator}

class Controller extends Observable {

    var controllerState: GameStatus = _
    var buildStatus: BuildStatus = BuildStatus.DEFAULT
    val groupList: List[Set[String]] = List(
        Set("Street1", "Street2", "Street3"),
        Set("Street4", "Street5", "Street6"),
        Set("Street7", "Street8", "Street9"))

    var board: Board = _
    // TODO check if 2nd variable needed
    var currentDice: Int = _

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


    def hasWholeGroup(player: Player, street: String): Boolean = {
        val group = groupList.find(g => g.contains(street)).get
        group.subsetOf(player.bought.flatMap(street => street.getName).asInstanceOf[Set[String]])
    }

    def rollDice(): (Int, Int) = {
        val r = scala.util.Random
        val (d1, d2) = (r.nextInt(6) + 1, r.nextInt(6) + 1)
        currentDice = d1 + d2
        (d1, d2)
    }

    def getWholeGroups(player: Player): List[Set[String]] = {
        var list: List[Set[String]] = List()
        groupList.foreach(group => {
            if (group.subsetOf(player.bought.map(street => street.getName))) {
                list = list :+ group
            }
        })
        list
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

    def playerTurn(firstDice: Int, secondDice: Int): Unit = {
        val player = board.currentPlayer
        val (newPlayer, passedGo) = player.walk(firstDice + secondDice)

        if (passedGo) {
            controllerState = PASSED_GO
            notifyObservers()
        }

        board = board.replacePlayer(player, newPlayer)
        controllerState = NEW_FIELD
        notifyObservers()

        val newField = getCurrentField
        // Action return ALREADY_BOUGHT, CAN_BUY or BOUGHT_BY_OTHER
        controllerState = newField.action(newPlayer)
        notifyObservers()

        controllerState match {
            case BOUGHT_BY_OTHER =>
                payRent(getCurrentPlayer, getCurrentField.asInstanceOf[Buyable], getBuyer(getCurrentField.asInstanceOf[Buyable]).get)
            case _ =>
        }

        if (getWholeGroups(newPlayer) != Nil) {
            controllerState = CAN_BUILD
            //for asking to buy
            notifyObservers()
            //for printing out if you bought
            notifyObservers()
            buildStatus = BuildStatus.DEFAULT
        }

        board = board.nextPlayerTurn()
        controllerState = NEXT_PLAYER
        notifyObservers()
    }

    def payRent(currentPlayer: Player, field: Buyable, receiver: Player) = {
        var payAmount = 0
        field match {
            case street: Street =>
                if (street.numHouses == 0 && hasWholeGroup(receiver, field.getName))
                    payAmount = field.getRent() * 2
                else
                    payAmount = field.getRent()
            case building: Building =>
                // TODO check this is correct
                if (hasWholeGroup(receiver, field.getName))
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
        if (!street.isBought || buyer.isEmpty || !buyer.get.equals(getCurrentPlayer))
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

    def currentGameMessage(): String = {
        controllerState match {
            case PASSED_GO => "Received 200€ by passing Go"
            case NEW_FIELD => "New Field: " + getCurrentField.getName
            case ALREADY_BOUGHT => "You already own this street"
            case CAN_BUY =>
                val field: Buyable = getCurrentField.asInstanceOf[Buyable]
                "Do you want to buy %s for %d€? (Y/N)".format(field.getName, field.getPrice)
            case BOUGHT_BY_OTHER => {
                val field = getCurrentField.asInstanceOf[Buyable]
                "Field already bought by " + getBuyer(field).get.name + ".\n" +
                  "You must pay " + field.getPrice + " rent!"
            }
            case CAN_BUILD =>
                buildStatus match {
                    case BuildStatus.DEFAULT => val wholeGroups = getWholeGroups(getCurrentPlayer)
                        "You can build on: \n" + buildablesToString(wholeGroups) +
                          "\nType the name of the street and the amount of houses you want to build. Press 'q' to quit. Q NEEDS TO BE IMPLEMENTED BACK IN"
                    case BuildStatus.BUILT => "Successfully built houses!"
                    case BuildStatus.INVALID_ARGS => "Invalid argument for street or amount of houses!"
                    case BuildStatus.NOT_OWN => "You don't own this street!"
                    case BuildStatus.TOO_MANY_HOUSES => "There can only be 5 houses on a street"
                    case BuildStatus.MISSING_MONEY => "You don't have enough money!"
                    case BuildStatus.DONE => ""
                }
            case NEXT_PLAYER => "Next player: " + getCurrentPlayer.name + "\n" + getCurrentPlayer.getDetails
            case MISSING_MONEY => "You do not have enough money!"
            case BOUGHT => "Successfully bought the street"
            case NOTHING => ""
        }
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
