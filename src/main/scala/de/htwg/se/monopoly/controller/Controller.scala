package de.htwg.se.monopoly.controller

import de.htwg.se.monopoly.controller.GameStatus._
import de.htwg.se.monopoly.model.boardComponent.{ActionField, Board, Building, Buyable, Field, Street}
import de.htwg.se.monopoly.model.playerComponent.Player
import de.htwg.se.monopoly.util.{FieldIterator, Observable, PlayerIterator}

class Controller extends Observable {
    var board: Board = _

    def setUp() = {
        val go = ActionField("Go")
        var fields = List[Field](go)
        for (i <- 1 to 9)
            fields = fields :+ Street("Street" + i, 50 * i)
        val player1 = Player("Player1", 1500, fields.head, List(), new FieldIterator(fields))
        val player2 = Player("Player2", 1500, fields.head, List(), new FieldIterator(fields))
        board = Board(fields, player1,new PlayerIterator(Array(player1, player2)))
    }

    def rollDice(): (Int, Int) = {
        val r = scala.util.Random
        (r.nextInt(6) + 1, r.nextInt(6) + 1)
    }

    def processRoll(firstDice: Int, secondDice: Int): Unit = {
        val player = board.currentPlayer
        val (newPlayer, passedGo) = player.walk(firstDice + secondDice)
        if(passedGo) notifyObservers(PASSED_GO)
        board = board.replacePlayer(player, newPlayer)
        notifyObservers(NEW_FIELD)
        val newField = getCurrentField
        notifyObservers(newField.action(newPlayer))
        board = board.copy(currentPlayer = board.nextPlayer)
        notifyObservers(NEXT_PLAYER)
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
            bought = currentPlayer.bought :+ currentField)
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

    def getCurrentField: Field = board.currentPlayer.currentField

    def getCurrentPlayer: Player = board.currentPlayer
}
