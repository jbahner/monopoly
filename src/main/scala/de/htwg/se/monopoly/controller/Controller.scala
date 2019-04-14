package de.htwg.se.monopoly.controller

import de.htwg.se.monopoly.controller.GameStatus._
import de.htwg.se.monopoly.model.boardComponent.{ActionField, Board, Field, Street}
import de.htwg.se.monopoly.model.playerComponent.Player
import de.htwg.se.monopoly.util.{FieldIterator, Observable, PlayerIterator}

class Controller extends Observable {
    var board: Board = _

    def setUp() = {
        val go = ActionField("Go")
        var fields = List[Field](go)
        for (i <- 1 to 9)
            fields = fields :+ Street("Street" + i, 50 * i)
        val player1 = Player("Player1", 1500, fields.head, new FieldIterator(fields))
        val player2 = Player("Player2", 1500, fields.head, new FieldIterator(fields))
        board = Board(fields, player1,new PlayerIterator(Array(player1, player2)))
    }

    def rollDice(): (Int, Int) = {
        val r = scala.util.Random
        (r.nextInt(6) + 1, r.nextInt(6) + 1)
    }

    def processRoll(firstDice: Int, secondDice: Int): Unit = {
        val player = board.currentPlayer
        val newPlayer = player.walk(firstDice + secondDice)
        board = board.replacePlayer(player, newPlayer)
        notifyObservers(NEWFIELD)
        board = board.copy(currentPlayer = board.nextPlayer)
        notifyObservers(NEXTPLAYER)
    }

    def getCurrentField: Field = board.currentPlayer.currentField

    def getCurrentPlayer: Player = board.currentPlayer
}
