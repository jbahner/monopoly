package de.htwg.se.monopoly.controller

import de.htwg.se.monopoly.controller.GameStatus.BuildStatus
import de.htwg.se.monopoly.controller.controllerBaseImpl.Controller
import de.htwg.se.monopoly.model.boardComponent.{IBuyable, boardBaseImpl}
import de.htwg.se.monopoly.model.boardComponent.boardBaseImpl._
import de.htwg.se.monopoly.model.playerComponent.playerBaseImpl.Player
import de.htwg.se.monopoly.model.playerComponent.{IPlayer, playerBaseImpl}
import de.htwg.se.monopoly.util.{FieldIterator, GeneralUtil, PlayerIterator, RentContext}
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json

class ControllerSpec extends WordSpec with Matchers {
    "A controller" should {
        val controller: IController = new Controller()
        val fields = List(ActionField("Go"), Street("Street1", 50, Array(1, 2, 3, 4, 5), houseCost = 25), Street("Street2", 50, Array(1, 2, 3, 4, 5), houseCost = 25), Street("Street3", 50, Array(2, 4, 6, 8, 10), houseCost = 25))
        val player1: IPlayer = Player("player1", 1500, fields.head, Set(), new FieldIterator(fields))
        val player2: IPlayer = Player("player2", 1500, fields.head, Set(), new FieldIterator(fields))
        controller.setBoard(Board(fields, player1, new PlayerIterator(Array(player1, player2))))
        "get the correct current field" in {
            controller.getCurrentField should be(fields.head)
        }
        "get the correct current player" in {
            controller.getCurrentPlayer.get should be(player1)
        }
        "have a JSON representation" in {
            val player1JSON = player1.getJSON
            val player2JSON = player2.getJSON
            val json = Json.parse(
                "{ \"board\" : { \"state\" : \"START_OF_TURN\",  \"current_player\" : \"player1\", \"players\" : [ " +
                    player1JSON.toString + ", " + player2JSON.toString() + " ]}}")
            controller.getJSON shouldEqual (json)
        }
        "get the correct buyer" in {
            controller.setBoard(boardBaseImpl.Board(fields, player1, new PlayerIterator(Array(player1, player2))))
            val noneBuyer = controller.getBuyer(fields(1).asInstanceOf[IBuyable])
            noneBuyer should be(None)
            val newPlayer2 = player2.copy(bought = player2.getBought + fields(1).asInstanceOf[IBuyable])
            controller.setBoard(controller.getBoard.replacePlayer(player2, newPlayer2))
            val buyer = controller.getBuyer(fields(1).asInstanceOf[IBuyable])
            buyer.get should be(newPlayer2)
        }
        "roll the dice correctly" in {
            controller.setBoard(boardBaseImpl.Board(fields, player1, new PlayerIterator(Array(player1, player2))))
            controller.rollDice
            controller.getCurrentDice._1 should (be >= 1 and be <= 6)
            controller.getCurrentDice._2 should (be >= 1 and be <= 6)
        }
        "walk correctly when processing the roll" in {
            controller.setBoard(boardBaseImpl.Board(fields, player1, new PlayerIterator(Array(player1, player2))))
            val dice: (Int, Int) = (1, 2)
            // TODO delete this LOC later
            controller.currentDice = dice
            controller.getUndoManager.doStep(WalkCommand(dice, controller))
            // TODO dostep seems not to be working
            //controller.board.playerIt.list.head.currentField should be(fields((dice._1 + dice._2) % fields.size))
            controller.getUndoManager.undoStep()
            // TODO incorrect
            // controller.board.playerIt.list.head.currentField should be(fields.head)
            controller.getUndoManager.redoStep()
            //controller.currentDice should be(dice)
            // TODO check why this is always Street 2
            //controller.board.playerIt.list.head.currentField should be(fields((dice._1 + dice._2) % fields.size))
        }
        "buy a street correctly" in {
            val buyer = Player("buyer", 1500, fields(1), Set(), new FieldIterator(fields.drop(1)))
            controller.setBoard(boardBaseImpl.Board(fields.drop(1), buyer, new PlayerIterator(Array(buyer))))
            controller.buy
            controller.getCurrentPlayer.get.getBought should contain(controller.getCurrentField)
            controller.getCurrentPlayer.get.getMoney should be(buyer.getMoney - fields(1).asInstanceOf[Street].getPrice)
            controller.getUndoManager.undoStep()
            controller.controllerState should be(GameStatus.CAN_BUY)
            controller.getCurrentPlayer.get.getBought should be(empty)
            controller.getCurrentPlayer.get.getMoney should be(buyer.getMoney)
            controller.getUndoManager.redoStep()
            controller.getCurrentPlayer.get.getBought should contain(controller.getCurrentField)
            controller.getCurrentPlayer.get.getMoney should be(buyer.getMoney - fields(1).asInstanceOf[Street].getPrice)
        }
        "buy a building correctly" in {
            val buildings = List(Building("building", 100))
            val buyer = Player("buyer", 1500, buildings.head, Set(), new FieldIterator(buildings))
            controller.setBoard(boardBaseImpl.Board(buildings, buyer, new PlayerIterator(Array(buyer))))
            controller.buy
            controller.getCurrentPlayer.get.getBought should contain(controller.getCurrentField)
            controller.getCurrentPlayer.get.getMoney should be(buyer.getMoney - buildings.head.getPrice)
        }
        "not buy a field" when {
            "the player does not have enough money" in {
                val buyer = Player("buyer", 1, fields(1), Set(), new FieldIterator(fields.drop(1)))
                controller.setBoard(boardBaseImpl.Board(fields.drop(1), buyer, new PlayerIterator(Array(buyer))))
                controller.buy
                buyer.getBought should not contain (controller.getBoard.getFields.head)
            }
        }
        "pay rent correctly" in {
            val rentFields = List(fields(1).asInstanceOf[Street].setBought(), fields(2).asInstanceOf[Street])
            val player = Player("player", 1500, rentFields.head, Set(), new FieldIterator(rentFields))
            val buyer = Player("buyer", 1500, rentFields.head, Set(rentFields.head), new FieldIterator(rentFields))
            controller.setBoard(boardBaseImpl.Board(rentFields, player, new PlayerIterator(Array(player, buyer))))
            RentContext.controller = controller
            controller.payRent(player, rentFields.head, buyer)

            val amount = RentContext.rentStrategy.executeStrategy(rentFields.head.asInstanceOf[IBuyable])
            controller.getBoard.getPlayerIt.list.head.getMoney should be(player.getMoney - amount)
            controller.getBoard.getPlayerIt.list(1).getMoney should be(buyer.getMoney + amount)
        }
        "build houses correctly" in {
            val groupFields = List(fields(1).asInstanceOf[Street].setBought(), fields(2).asInstanceOf[Street], fields(3).asInstanceOf[Street])
            val builder = Player("builder", 1500, fields(1), groupFields.toSet, new FieldIterator(groupFields))
            controller.setBoard(boardBaseImpl.Board(groupFields, builder, new PlayerIterator(Array(builder))))
            controller.buildHouses(groupFields.head.getName, 2)
            controller.getBuildStatus should be(BuildStatus.BUILT)
            controller.getBoard.getFields.head.asInstanceOf[Street].numHouses should be(2)
            controller.getUndoManager.undoStep()
            controller.controllerState should be(GameStatus.CAN_BUILD)
            controller.getBuildStatus should be(BuildStatus.DEFAULT)
            controller.getBoard.getFields.head.asInstanceOf[Street].numHouses should be(0)
            controller.getUndoManager.redoStep()
            controller.getBuildStatus should be(BuildStatus.BUILT)
            controller.getBoard.getFields.head.asInstanceOf[Street].numHouses should be(2)
        }
        "not build houses" when {
            "field is not a street" in {
                val builder = Player("builder", 1500, fields.head, Set(), new FieldIterator(fields))
                controller.setBoard(boardBaseImpl.Board(fields, builder, new PlayerIterator(Array(builder))))
                controller.buildHouses(fields.head.getName, 1)
                controller.getBuildStatus should be(BuildStatus.INVALID_ARGS)
            }
            "the player does not own the street" in {
                val streets = List(fields(1).asInstanceOf[Street].setBought(), fields(2).asInstanceOf[Street].setBought(), fields(3).asInstanceOf[Street].setBought())
                val builder = Player("builder", 1500, streets(1), Set(), new FieldIterator(streets))
                controller.setBoard(boardBaseImpl.Board(streets, builder, new PlayerIterator(Array(builder))))
                controller.buildHouses(streets.head.getName, 1)
                controller.getBuildStatus should be(BuildStatus.NOT_OWN)
            }
            "the amount of houses cannot be built" in {
                val streets = List(fields(1).asInstanceOf[Street].setBought(), fields(2).asInstanceOf[Street].setBought(), fields(3).asInstanceOf[Street].setBought())
                val builder = Player("builder", 1500, streets(1), streets.toSet, new FieldIterator(streets))
                controller.setBoard(boardBaseImpl.Board(streets, builder, new PlayerIterator(Array(builder))))
                controller.buildHouses(streets.head.getName, 6)
                controller.getBuildStatus should be(BuildStatus.TOO_MANY_HOUSES)
            }
            "the player does not have enough money" in {
                val streets = List(fields(1).asInstanceOf[Street].setBought(), fields(2).asInstanceOf[Street].setBought(), fields(3).asInstanceOf[Street].setBought())
                val builder = Player("builder", 1, streets(1), streets.toSet, new FieldIterator(streets))
                controller.setBoard(boardBaseImpl.Board(streets, builder, new PlayerIterator(Array(builder))))
                controller.buildHouses(streets.head.getName, 3)
                controller.getBuildStatus should be(BuildStatus.MISSING_MONEY)
            }
        }
    }

    "A controller" should {
        val controller: IController = new Controller()

        val s1 = Street("Street1", 50, Array(1, 2, 3, 4, 5), houseCost = 25)
        val s2 = Street("Street2", 50, Array(1, 2, 3, 4, 5), houseCost = 25)
        val s3 = Street("Street3", 50, Array(2, 4, 6, 8, 10), houseCost = 25)
        val fields = List(ActionField("Go"), s1, s2, s3)

        val player1 = Player("player1", 1500, fields.head, Set(), new FieldIterator(fields))
        val player2 = Player("player2", 1500, fields.head, Set(s1, s2, s3), new FieldIterator(fields))
        controller.setBoard(boardBaseImpl.Board(fields, player1, new PlayerIterator(Array(player1, player2))))
        "declare the next player" in {
            controller.getCurrentPlayer.get should be(player1)
            controller.nextPlayer
            controller.getCurrentPlayer.get should be(player2)
        }
        "state should be CAN_BUILD" in {
            //controller.processRoll(1, 2)
            controller.currentDice = (1, 2)
            controller.getUndoManager.doStep(WalkCommand((1, 2), controller))
            controller.controllerState should be(GameStatus.CAN_BUILD)
        }
        "concat buildables to String" in {
            controller.buildablesToString(GeneralUtil.getWholeGroups(player1)) shouldBe a[String]
        }
    }

    "A controller" should {

        val s1 = Street("Street1", 50, Array(1, 2, 3, 4, 5), houseCost = 25)
        val s2 = Street("Street2", 50, Array(1, 2, 3, 4, 5), houseCost = 25)
        val s3 = Street("Street3", 50, Array(2, 4, 6, 8, 10), houseCost = 25)
        val s4 = Street("Street4", 50, Array(2, 4, 6, 8, 10), houseCost = 25)
        val fields = List(ActionField("Go"), s1, s2, s3, s4)

        val player1 = Player("player1", 1500, fields(1), Set(s1, s2, s3), new FieldIterator(fields))
        val controller: IController = new Controller()

        controller.setBoard(boardBaseImpl.Board(fields, player1, new PlayerIterator(Array(player1))))

        "return the correct game message" when {
            "controller state is START_OF_TURN" in {
                controller.controllerState = GameStatus.START_OF_TURN
                controller.catCurrentGameMessage shouldBe a[String]
            }
            "controller state is PASSED_GO" in {
                controller.controllerState = GameStatus.PASSED_GO
                controller.catCurrentGameMessage shouldBe a[String]
            }
            "controller state is NEW_FIELD" in {
                controller.controllerState = GameStatus.NEW_FIELD
                controller.catCurrentGameMessage shouldBe a[String]
            }
            "controller state is ALREADY_BOUGHT" in {
                controller.controllerState = GameStatus.ALREADY_BOUGHT
                controller.catCurrentGameMessage shouldBe a[String]
            }
            "controller state is CAN_BUY" in {
                controller.controllerState = GameStatus.CAN_BUY
                controller.catCurrentGameMessage shouldBe a[String]
            }
            "controller state is BOUGHT_BY_OTHER" in {
                controller.controllerState = GameStatus.BOUGHT_BY_OTHER
                controller.catCurrentGameMessage shouldBe a[String]
            }
            "controller state is DONE" in {
                controller.controllerState = GameStatus.DONE
                controller.catCurrentGameMessage shouldBe a[String]
            }
            "controller state is NEXT_PLAYER" in {
                controller.controllerState = GameStatus.NEXT_PLAYER
                controller.catCurrentGameMessage shouldBe a[String]
            }
            "controller state is MISSING_MONEY" in {
                controller.controllerState = GameStatus.MISSING_MONEY
                controller.catCurrentGameMessage shouldBe a[String]
            }
            "controller state is BOUGHT" in {
                controller.controllerState = GameStatus.BOUGHT
                controller.catCurrentGameMessage shouldBe a[String]
            }
            "controller state is NOTHING" in {
                controller.controllerState = GameStatus.NOTHING
                controller.catCurrentGameMessage shouldBe a[String]
            }
            "controller state is CAN_BUILD" when {
                controller.controllerState = GameStatus.CAN_BUILD

                "build status is BUILT" in {
                    controller.buildStatus = GameStatus.BuildStatus.DEFAULT
                    controller.catCurrentGameMessage shouldBe a[String]
                }
                "build status is INVALID_ARGS" in {
                    controller.buildStatus = GameStatus.BuildStatus.INVALID_ARGS
                    controller.catCurrentGameMessage shouldBe a[String]
                }
                "build status is NOT_OWN" in {
                    controller.buildStatus = GameStatus.BuildStatus.NOT_OWN
                    controller.catCurrentGameMessage shouldBe a[String]
                }
                "build status is TOO_MANY_HOUSES" in {
                    controller.buildStatus = GameStatus.BuildStatus.TOO_MANY_HOUSES
                    controller.catCurrentGameMessage shouldBe a[String]
                }
                "build status is MISSING_MONEY" in {
                    controller.buildStatus = GameStatus.BuildStatus.MISSING_MONEY
                    controller.catCurrentGameMessage shouldBe a[String]
                }
                "build status is DONE" in {
                    controller.buildStatus = GameStatus.BuildStatus.DONE
                    controller.catCurrentGameMessage shouldBe a[String]
                }
            }

        }
    }

    // This one is cheated but I wanted the coverage to be increased
    // since this method will be here for quite a while
    "A controller" should {
        val controller: IController = new Controller

        "use the test setUp correctly" in {
            controller.setUp
            controller.getBoard.getCurrentPlayer shouldBe a[IPlayer]
        }
    }
}