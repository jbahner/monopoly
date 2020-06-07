package modelComponent.boardComponent.boardBaseImpl

import modelComponent.boardComponent.IBoard
import modelComponent.fieldComponent.IBuyable
import modelComponent.fieldComponent.fieldBaseImpl.{ActionField, Street}
import modelComponent.playerComponent.playerBaseImpl.Player
import modelComponent.util.{FieldIterator, PlayerIterator}
import org.scalatest.{Matchers, WordSpec}
import modelComponent.boardComponent

class BoardSpec extends WordSpec with Matchers {
    "A Board" should {
        val fields = List(ActionField("Go"), Street("street1", 50, Array(1, 2, 3, 4, 5), houseCost = 50))
        val player1 = Player("player1", 1500, fields.head, Set(), new FieldIterator(fields))
        val player2 = Player("player2", 1500, fields.head, Set(), new FieldIterator(fields))
        val currentDice = 0
        "have a list of fields" in {
            val board = Board(fields, player1, new PlayerIterator(Array(player1, player2)), currentDice)
            board.fields should be(fields)
        }
        "have a player iterator" in {
            val playerIterator = new PlayerIterator(Array(player1, player2))
            val board = boardComponent.boardBaseImpl.Board(fields, player1, playerIterator, currentDice)
            board.playerIt should be(playerIterator)
        }
        "have a current player" in {
            val board = boardComponent.boardBaseImpl.Board(fields, player1, new PlayerIterator(Array(player1, player2)), currentDice)
            board.currentPlayer should be(player1)
        }
        "switch to the next player" in {
            var board: IBoard = boardComponent.boardBaseImpl.Board(fields, player1, new PlayerIterator(Array(player1, player2)), currentDice)
            board = board.copy(board.getFields, board.nextPlayer(), board.getPlayerIt, currentDice)
            board.getCurrentPlayer should be(player2)
        }
        "be able to replace a player" in {
            var board: IBoard = boardComponent.boardBaseImpl.Board(fields, player1, new PlayerIterator(Array(player1, player2)), currentDice)
            val newPlayer = Player("newPlayer", 50, fields.head, Set(), new FieldIterator(fields))
            board = board.replacePlayer(board.getCurrentPlayer, newPlayer)
            board.getCurrentPlayer should be(newPlayer)
            board = boardComponent.boardBaseImpl.Board(fields, player1, new PlayerIterator(Array(player1, player2)), currentDice)
        }
        "be able to replace a field" in {
            var board: IBoard = boardComponent.boardBaseImpl.Board(fields, player1, new PlayerIterator(Array(player1, player2)), currentDice)
            val newField = Street("NewField", 1000, Array(0, 0, 0, 0, 0), 1000)
            board = board.replaceField(fields(1).asInstanceOf[IBuyable], newField)
            board.getFields(1) should be(newField)
        }
        "be able to replace a field that belongs to a player" in {
            var board: IBoard = boardComponent.boardBaseImpl.Board(fields, player1, new PlayerIterator(Array(player1, player2)), currentDice)
            val newField = Street("street1", 1000, Array(0, 0, 0, 0, 0), 1000)
            board = board.replacePlayer(player1, player1.copy(bought = player1.getBought + fields(1).asInstanceOf[IBuyable]))
            board = board.replaceField(board.getFields(1).asInstanceOf[IBuyable], newField)
            board.getCurrentPlayer.getBought.contains(newField) should be(true)
        }
    }
}
