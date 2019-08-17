package de.htwg.se.monopoly.controller

import de.htwg.se.monopoly.controller.GameStatus.BuildStatus.BuildStatus
import de.htwg.se.monopoly.controller.GameStatus._
import de.htwg.se.monopoly.model.boardComponent.{ActionField, Board, Building, Buyable, Field, Street}
import de.htwg.se.monopoly.model.playerComponent.Player
import de.htwg.se.monopoly.util.{FieldIterator, Observable, PlayerIterator}

class Controller extends Observable {

    val groupList : List[Set[String]] = List(Set("Street1", "Street2", "Street3"), Set("Street4", "Street5", "Street6"), Set("Street7", "Street8", "Street9"))
    var board: Board = _

    def setUp() = {
        val go = ActionField("Go")
        var fields = List[Field](go)
        for (i <- 1 to 3)
            fields = fields :+ Street(name = "Street" + i, price = 50 * i, rentCosts = getRentArray(50*i), houseCost = 25 * i, isBought = true)
        for(i <- 4 to 6)
            fields = fields :+ Street(name = "Street" + i, price = 50 * i, rentCosts = getRentArray(50*i), houseCost = 25 * i, isBought = true)
        for(i <- 7 to 9)
            fields = fields :+ Street(name = "Street" + i, price = 50 * i, rentCosts = getRentArray(50*i), houseCost = 25 * i, isBought = true)

        var player1 = Player(name = "Player1", money = 1500, currentField =fields.head, bought = Set(), fieldIt = new FieldIterator(fields))
        var player2 = Player(name = "Player2", money = 1500, currentField = fields.head, bought = Set(), fieldIt = new FieldIterator(fields))
        // For paying rent testing purposes
        for(i <- 1 to 6) {
            player1 = player1.copy(bought = player1.bought + fields(i).asInstanceOf[Street])
        }
        for(i <- 7 to 9)
            player2 = player2.copy(bought = player2.bought + fields(i).asInstanceOf[Street])
        board = Board(fields, player1,new PlayerIterator(Array(player1, player2)))
    }

    // This is only for testing purposes
    private def getRentArray(cost : Int) : Array[Int] = {
        val step = cost / 10
        Array(step, step*2, step*3, step*4, step*5)
    }

    def hasWholeGroup(player : Player, street : String) : Boolean = {
        val group = groupList.find(g => g.contains(street)).get
        group.subsetOf(player.bought.flatMap(street => street.getName).asInstanceOf[Set[String]])
    }

    def getWholeGroups(player: Player): List[Set[String]] = {
        var list : List[Set[String]] = List()
        groupList.foreach(group => {
            if(group.subsetOf(player.bought.map(street => street.getName))) {
                list = list :+ group
            }
        })
        list
    }

    def rollDice(): (Int, Int) = {
        val r = scala.util.Random
        (r.nextInt(6) + 1, r.nextInt(6) + 1)
    }

    def getBuyer(buyable: Buyable) : Option[Player] = {
        val players= board.playerIt.list
        players.find(p => p.bought.contains(buyable))
    }

    def processRoll(firstDice: Int, secondDice: Int): Unit = {
        val player = board.currentPlayer
        val (newPlayer, passedGo) = player.walk(firstDice + secondDice)
        if(passedGo) notifyObservers(PASSED_GO)
        board = board.replacePlayer(player, newPlayer)
        notifyObservers(NEW_FIELD)
        val newField = getCurrentField
        val action = newField.action(newPlayer)
        notifyObservers(action)
        if(getWholeGroups(newPlayer) != Nil) {
            notifyObservers(CAN_BUILD)
        }
        board = board.copy(currentPlayer = board.nextPlayer)
        notifyObservers(NEXT_PLAYER)
    }

    def payRent() = {
        val payer = getCurrentPlayer
        val field = getCurrentField.asInstanceOf[Buyable]
        val receiver = getBuyer(getCurrentField.asInstanceOf[Buyable]).get
        var payAmount = field.getPrice
        if(hasWholeGroup(receiver, field.getName)) payAmount = payAmount * 2
        if(payer.money < payAmount) {
            notifyObservers(MISSING_MONEY)
        } else {
            board.replacePlayer(payer, payer.copy(money = payer.money - payAmount))
            board.replacePlayer(receiver, receiver.copy(money = receiver.money + payAmount))
        }
    }

    def buy(): Unit = {
        val currentPlayer = getCurrentPlayer
        val currentField = getCurrentField.asInstanceOf[Buyable]
        var newField = currentField
        currentField match {
            case street: Street => newField = street.copy(isBought = true)
            case building: Building => newField = building.copy(isBought = true)
        }
        val newPlayer : Player = currentPlayer.copy(money = currentPlayer.money - currentField.getPrice,
            bought = currentPlayer.bought + currentField)
        board = board.replacePlayer(currentPlayer, newPlayer)
        currentField match {
            case street: Street=>
                board = board.replaceField(field = street, newField = street.copy(isBought = true))
            case building: Building =>
                board = board.replaceField(field = building, newField = building.copy(isBought = true))
        }
        board = board.copy(currentPlayer = newPlayer)
        notifyObservers(BOUGHT)
    }

    def getFieldByName(name : String) : Option[Field] = {
        board.fields.find(field => field.getName.equals(name))
    }

    def buildHouses(streetName : String, amount : Int) : BuildStatus = {
        val field = getFieldByName(streetName)
        if(field.isEmpty || !field.get.isInstanceOf[Street])
            return BuildStatus.INVALID_ARGS
        val street = field.get.asInstanceOf[Street]
        val buyer = getBuyer(street)
        if(!street.isBought || !buyer.get.equals(getCurrentPlayer))
            return BuildStatus.NOT_OWN
        if(street.numHouses + amount > 5)
            return BuildStatus.TOO_MANY_HOUSES
        if(getCurrentPlayer.money < street.houseCost * amount)
            return BuildStatus.MISSING_MONEY
        board = board.replaceField(field = field.get, newField = street.buyHouses(amount))
        board = board.replacePlayer(getCurrentPlayer, getCurrentPlayer.copy(money = getCurrentPlayer.money - street.houseCost * amount))
        BuildStatus.BUILT
    }

    def getCurrentField: Field = board.currentPlayer.currentField

    def getCurrentPlayer: Player = board.currentPlayer
}
