package de.htwg.se.monopoly.model.boardComponent

import de.htwg.se.monopoly.controller.GameStatus
import de.htwg.se.monopoly.model.playerComponent.Player
import de.htwg.se.monopoly.util.FieldIterator
import org.scalatest.{Matchers, WordSpec}

class StreetSpec extends WordSpec with Matchers {
    "A Street" should {
        val street = Street("streetName", 100, Array(10, 20, 30, 40 ,50), 50)
        "should have a name" in {
            street.getName should be("streetName")
        }
        "should have a price" in {
            street.getPrice should be(100)
        }
        "should not be bought" in {
            street.isBought should be(false)
        }
        "should be buyable" in {
            val boughtStreet = street.setBought()
            boughtStreet.isBought should be (true)
        }
        "should get the correct rent" in {
            street.getRent() should be(10)
            val houseStreet = street.copy(numHouses = 2)
            houseStreet.getRent() should be(30)
        }
        "should be able to build houses" in {
            val houseStreet = street.buyHouses(3)
            houseStreet.numHouses should be (3)
        }
        "get the correct action" should {
            "can buy" in {
                val player = Player("player", 1500, street, Set(), new FieldIterator(List(street)))
                street.action(player) should be(GameStatus.CAN_BUY)
            }
            "bought by other" in {
                val boughtStreet = street.setBought()
                val player = Player("player", 1500, boughtStreet, Set(), new FieldIterator(List(boughtStreet)))
                boughtStreet.action(player) should be(GameStatus.BOUGHT_BY_OTHER)
            }
            "bought by same" in {
                val boughtStreet = street.setBought()
                val player = Player("player", 1500, boughtStreet, Set(boughtStreet), new FieldIterator(List(boughtStreet)))
                boughtStreet.action(player) should be(GameStatus.ALREADY_BOUGHT)
            }
        }
    }
}
