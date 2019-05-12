package de.htwg.se.monopoly.model.boardComponent

import de.htwg.se.monopoly.model.playerComponent.Player
import de.htwg.se.monopoly.util.{FieldIterator, PlayerIterator}
import org.scalatest.{Matchers, WordSpec}

class BoardSpec extends WordSpec with Matchers {
    "A Board" should {
        val fields = List(ActionField("Go"), Street("street1", 50))
        val player1 = Player("player1", 1500, fields.head, List(), new FieldIterator(fields))
        val player2 = Player("player2", 1500, fields.head, List(), new FieldIterator(fields))
        val playerIterator = new PlayerIterator(Array(player1, player2))
        var board = Board(fields, player1, playerIterator)
        "have a list of fields" in {
            board.fields should be(fields)
        }
        "have a player iterator" in {
            board.playerIt should be(playerIterator)
        }
        "have a current player" in {
            board.currentPlayer should be(player1)
        }
        "switch to the next player" in {
            board = board.copy(currentPlayer = board.nextPlayer)
            board.currentPlayer should be(player2)
        }
        "be able to replace a player" in {
            val newPlayer = Player("newPlayer", 50, fields.head, List(), new FieldIterator(fields))
            board = board.replacePlayer(board.currentPlayer, newPlayer)
            board.currentPlayer should be(newPlayer)
        }
    }
}
