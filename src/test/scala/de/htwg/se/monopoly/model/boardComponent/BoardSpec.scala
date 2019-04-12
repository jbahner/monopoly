package de.htwg.se.monopoly.model.boardComponent

import de.htwg.se.monopoly.model.playerComponent.Player
import de.htwg.se.monopoly.util.{FieldIterator, PlayerIterator}
import org.scalatest.{Matchers, WordSpec}

class BoardSpec extends WordSpec with Matchers {
    "A Board" should {
        "new" should {
            val fields = List(ActionField("Go"), Street("street1", 50))
            val player1 = Player("player1", 1500, fields.head, new FieldIterator(fields))
            val player2 = Player("player2", 1500, fields.head, new FieldIterator(fields))
            val board = Board(fields, player1, new PlayerIterator(Array(player1, player2)))
            "have a current player" in {
                board.currentPlayer should be(player1)
            }
        }
    }
}
