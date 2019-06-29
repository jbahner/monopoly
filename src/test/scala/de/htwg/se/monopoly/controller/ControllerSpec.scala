package de.htwg.se.monopoly.controller

import de.htwg.se.monopoly.model.boardComponent.{ActionField, Board, Street}
import de.htwg.se.monopoly.model.playerComponent.Player
import de.htwg.se.monopoly.util.{FieldIterator, PlayerIterator}
import org.scalatest.{Matchers, WordSpec}

class ControllerSpec extends WordSpec with Matchers {
    "A controller" should {
        val controller = new Controller()
        val fields = List(ActionField("Go"), Street("street1", 50))
        val player1 = Player("player1", 1500, fields.head, List(), new FieldIterator(fields))
        val player2 = Player("player2", 1500, fields.head, List(), new FieldIterator(fields))
        val playerIterator = new PlayerIterator(Array(player1, player2))
        val board = Board(fields, player1, playerIterator)
        controller.board = board
        "roll dice" in {
            val (d1,d2) = controller.rollDice()
            d1 should (be >= 1 and be <= 6)
            d2 should (be >= 1 and be <= 6)
        }
        "get the correct current field" in {
            controller.getCurrentField should be(fields.head)
        }
        "get the correct current player" in {
            controller.getCurrentPlayer should be(player1)
        }
    }
}
