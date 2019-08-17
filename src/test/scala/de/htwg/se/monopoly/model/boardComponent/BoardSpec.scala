package de.htwg.se.monopoly.model.boardComponent

import de.htwg.se.monopoly.model.playerComponent.Player
import de.htwg.se.monopoly.util.{FieldIterator, PlayerIterator}
import org.scalatest.{Matchers, WordSpec}

class BoardSpec extends WordSpec with Matchers {
    "A Board" should {
        val fields = List(ActionField("Go"), Street("street1", 50, Array(1, 2, 3, 4, 5), houseCost = 50))
        val player1 = Player("player1", 1500, fields.head, Set(), new FieldIterator(fields))
        val player2 = Player("player2", 1500, fields.head, Set(), new FieldIterator(fields))
        "have a list of fields" in {
            val board = Board(fields, player1, new PlayerIterator(Array(player1, player2)))
            board.fields should be(fields)
        }
        "have a player iterator" in {
            val playerIterator = new PlayerIterator(Array(player1, player2))
            val board = Board(fields, player1, playerIterator)
            board.playerIt should be(playerIterator)
        }
        "have a current player" in {
            val board = Board(fields, player1, new PlayerIterator(Array(player1, player2)))
            board.currentPlayer should be(player1)
        }
        "switch to the next player" in {
            var board = Board(fields, player1, new PlayerIterator(Array(player1, player2)))
            board = board.copy(currentPlayer = board.nextPlayer)
            board.currentPlayer should be(player2)
        }
        "be able to replace a player" in {
            var board = Board(fields, player1, new PlayerIterator(Array(player1, player2)))
            val newPlayer = Player("newPlayer", 50, fields.head, Set(), new FieldIterator(fields))
            board = board.replacePlayer(board.currentPlayer, newPlayer)
            board.currentPlayer should be(newPlayer)
            board = Board(fields, player1, new PlayerIterator(Array(player1, player2)))
        }
        "be able to replace a field" in {
            var board = Board(fields, player1, new PlayerIterator(Array(player1, player2)))
            val newField = Street("NewField", 1000, Array(0,0,0,0,0), 1000)
            board = board.replaceField(fields(1), newField)
            board.fields(1) should be(newField)
        }
        "be able to replace a field that belongs to a player" in {
            var board = Board(fields, player1, new PlayerIterator(Array(player1, player2)))
            val newField = Street("street1", 1000, Array(0,0,0,0,0), 1000)
            board = board.replacePlayer(player1, player1.copy(bought = player1.bought + fields(1).asInstanceOf[Buyable]))
            board = board.replaceField(board.fields(1), newField)
            board.currentPlayer.bought.contains(newField) should be(true)
        }
    }
}
