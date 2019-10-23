package de.htwg.se.monopoly.controller

import de.htwg.se.monopoly.controller.GameStatus.BuildStatus
import de.htwg.se.monopoly.model.boardComponent.{ActionField, Board, Building, Buyable, Street}
import de.htwg.se.monopoly.model.playerComponent.Player
import de.htwg.se.monopoly.util.{FieldIterator, PlayerIterator}
import org.scalatest.{Matchers, WordSpec}

class ControllerSpec extends WordSpec with Matchers {
    "A controller" should {
        val controller = new Controller()
        val fields = List(ActionField("Go"), Street("Street1", 50, Array(1,2,3,4,5), houseCost = 25), Street("Street2", 50, Array(1,2,3,4,5), houseCost = 25), Street("Street3", 50, Array(2,4,6,8,10), houseCost = 25))
        val player1 = Player("player1", 1500, fields.head, Set(), new FieldIterator(fields))
        val player2 = Player("player2", 1500, fields.head, Set(), new FieldIterator(fields))
        controller.board = Board(fields, player1, new PlayerIterator(Array(player1, player2)))
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
        "get the correct buyer" in {
            controller.board = Board(fields, player1, new PlayerIterator(Array(player1, player2)))
            val noneBuyer = controller.getBuyer(fields(1).asInstanceOf[Buyable])
            noneBuyer should be(None)
            val newPlayer2 = player2.copy(bought = player2.bought + fields(1).asInstanceOf[Buyable])
            controller.board = controller.board.replacePlayer(player2, newPlayer2)
            val buyer = controller.getBuyer(fields(1).asInstanceOf[Buyable])
            buyer.get should be(newPlayer2)
        }
        "walk correctly when processing the roll" in {
            controller.board = Board(fields, player1, new PlayerIterator(Array(player1, player2)))
            controller.processRoll(1,1)
            controller.board.playerIt.list.head.currentField should be(fields(2))
        }
        "buy a street correctly" in {
            val buyer = Player("buyer", 1500, fields(1), Set(), new FieldIterator(fields.drop(1)))
            controller.board = Board(fields.drop(1), buyer, new PlayerIterator(Array(buyer)))
            controller.buy()
            controller.getCurrentPlayer.bought should contain(controller.getCurrentField)
            controller.getCurrentPlayer.money should be(buyer.money - fields(1).asInstanceOf[Street].getPrice)
        }
        "buy a building correctly" in {
            val buildings = List(Building("building", 100))
            val buyer = Player("buyer", 1500, buildings.head, Set(), new FieldIterator(buildings))
            controller.board = Board(buildings, buyer, new PlayerIterator(Array(buyer)))
            controller.buy()
            controller.getCurrentPlayer.bought should contain(controller.getCurrentField)
            controller.getCurrentPlayer.money should be(buyer.money - buildings.head.getPrice)
        }
        "not buy a field" when {
            "the player does not have enough money" in {
                val buyer = Player("buyer", 1, fields(1), Set(), new FieldIterator(fields.drop(1)))
                controller.board = Board(fields.drop(1), buyer, new PlayerIterator(Array(buyer)))
                controller.buy()
                buyer.bought should not contain(controller.board.fields.head)
            }
        }
        "pay rent correctly" in {
            val rentFields = List(fields(1).asInstanceOf[Street].setBought(), fields(2).asInstanceOf[Street])
            val player = Player("player", 1500, rentFields.head, Set(), new FieldIterator(rentFields))
            val buyer = Player("buyer", 1500, rentFields.head, Set(rentFields.head), new FieldIterator(rentFields))
            controller.board = Board(rentFields, player, new PlayerIterator(Array(player, buyer)))
            controller.payRent(player, rentFields.head, buyer)

            controller.board.playerIt.list(0).money should be(player.money - rentFields.head.getRent())
            controller.board.playerIt.list(1).money should be(buyer.money + rentFields.head.getRent())
        }
        "build houses correctly" in {
            val groupFields = List(fields(1).asInstanceOf[Street].setBought(), fields(2).asInstanceOf[Street], fields(3).asInstanceOf[Street])
            val builder = Player("builder", 1500, fields(1),  groupFields.toSet, new FieldIterator(groupFields))
            controller.board = Board(groupFields, builder, new PlayerIterator(Array(builder)))
            controller.buildHouses(groupFields(0).getName, 2) should be(BuildStatus.BUILT)
            controller.board.fields(0).asInstanceOf[Street].numHouses should be(2)
        }
        "not build houses" when {
            "field is not a street" in {
                val builder = Player("builder", 1500, fields.head,  Set(), new FieldIterator(fields))
                controller.board = Board(fields, builder, new PlayerIterator(Array(builder)))
                controller.buildHouses(fields.head.getName, 1) should be(BuildStatus.INVALID_ARGS)
            }
            "the player does not own the street" in {
                val streets = List(fields(1).asInstanceOf[Street].setBought(), fields(2).asInstanceOf[Street].setBought(), fields(3).asInstanceOf[Street].setBought())
                val builder = Player("builder", 1500, streets(1),  Set(), new FieldIterator(streets))
                controller.board = Board(streets, builder, new PlayerIterator(Array(builder)))
                controller.buildHouses(streets.head.getName, 1) should be(BuildStatus.NOT_OWN)
            }
            "the amount of houses cannot be built" in {
                val streets = List(fields(1).asInstanceOf[Street].setBought(), fields(2).asInstanceOf[Street].setBought(), fields(3).asInstanceOf[Street].setBought())
                val builder = Player("builder", 1500, streets(1),  streets.toSet, new FieldIterator(streets))
                controller.board = Board(streets, builder, new PlayerIterator(Array(builder)))
                controller.buildHouses(streets.head.getName, 6) should be(BuildStatus.TOO_MANY_HOUSES)
            }
            "the player does not have enough money" in {
                val streets = List(fields(1).asInstanceOf[Street].setBought(), fields(2).asInstanceOf[Street].setBought(), fields(3).asInstanceOf[Street].setBought())
                val builder = Player("builder", 1, streets(1),  streets.toSet, new FieldIterator(streets))
                controller.board = Board(streets, builder, new PlayerIterator(Array(builder)))
                controller.buildHouses(streets.head.getName, 3) should be(BuildStatus.MISSING_MONEY)
            }
        }
    }
}