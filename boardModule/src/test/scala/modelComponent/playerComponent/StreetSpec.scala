package modelComponent.playerComponent

import modelComponent.fieldComponent.fieldBaseImpl.Street
import modelComponent.playerComponent.playerBaseImpl.Player
import modelComponent.util.FieldIterator
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json

class StreetSpec extends WordSpec with Matchers {
    "A Street" should {
        val street = Street("streetName", 100, Array(10, 20, 30, 40, 50), 50)
        "have a name" in {
            street.getName should be("streetName")
        }
        "have a price" in {
            street.getPrice should be(100)
        }
        "not be bought" in {
            street.isBought should be(false)
        }
        "be buyable" in {
            val boughtStreet = street.setBought()
            boughtStreet.getIsBought should be(true)
        }
        "be able to build houses" in {
            val houseStreet = street.buyHouses(3)
            houseStreet.getNumHouses should be(3)
        }
        "have a JSON representation" in {
            val json = Json.parse("""{ "name" : "streetName", "houses" : 0, "houseCost" : 50 }""")
            street.getJSON shouldEqual (json)
        }
        "have a JSON representation with correct houses" in {
            val street2 = street.copy(isBought = true, numHouses = 3)
            val json = Json.parse("""{ "name" : "streetName", "houses" : 3, "houseCost" : 50 }""")
            street2.getJSON shouldEqual (json)
        }
        "get the correct action" should {
            "can buy" in {
                val player = Player("player", 1500, street, Set(), new FieldIterator(List(street)))
                street.action(player) should be("CAN_BUY")
            }
            "bought by other" in {
                val boughtStreet = street.setBought()
                val player = Player("player", 1500, boughtStreet, Set(), new FieldIterator(List(boughtStreet)))
                boughtStreet.action(player) should be("BOUGHT_BY_OTHER")
            }
            "bought by same" in {
                val boughtStreet = street.setBought()
                val player = Player("player", 1500, boughtStreet, Set(boughtStreet), new FieldIterator(List(boughtStreet)))
                boughtStreet.action(player) should be("ALREADY_BOUGHT")
            }
        }
    }
}
