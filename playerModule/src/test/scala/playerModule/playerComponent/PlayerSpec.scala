package playerModule.playerComponent

import playerModule.fieldComponent.fieldBaseImpl.{ActionField, Street}
import org.scalatest._
import play.api.libs.json.Json
import playerModule.playerComponent.playerBaseImpl.Player
import playerModule.util.FieldIterator

class PlayerSpec extends WordSpec with Matchers {
    "A Player" when {
        "new" should {
            val go = ActionField("Go")
            val street1 = Street("Street1", 50, Array(1,2,3,4,5), 25)
            val street2 = Street("Street2", 100, Array(2,4,6,8,10), 50)
            val fields = List(go, street1, street2)
            val player = Player("name", 1500, fields.head, Set(), new FieldIterator(fields))
            "have a name" in {
                player.getName should be("name")
            }
            "have money" in {
                player.getMoney should be(1500)
            }
            "have a String representation" in {
                player.toString should be("name, money: 1500")
            }
            "have a String representation with details" in {
                val testPlayer = Player("player", 1500, fields.head, Set(street1), new FieldIterator(fields))
                testPlayer.getDetails should include(player.getMoney.toString)
                testPlayer.getDetails should include(street1.getName)
                testPlayer.getDetails should include(fields.head.getName)
            }

            "have a JSON representation" in {
                val json = Json.parse("""{ "name" : "name", "money" : 1500, "current_field" : "Go", "bought_fields" : [ ]}""")
                player.getJSON shouldEqual(json)
            }

            "have bought fields included in JSON representation" in {
                val s1 = street1.copy(isBought = true, numHouses = 3)
                val s2 = street2.copy(isBought = true, numHouses = 4)
                val player2 = player.copy(bought = Set(s1, s2))
                val json = Json.parse(
                    """{ "name" : "name", "money" : 1500, "current_field" : "Go",
                      |"bought_fields" : [ { "name" : "Street1", "houses" : 3, "houseCost": 25}, { "name" : "Street2", "houses" : 4, "houseCost" : 50 } ]}""".stripMargin)
                player2.getJSON shouldEqual(json)
            }

            "stand on the first field" in {
                player.getCurrentField should be(fields.head)
            }
            "have no bought streets" in {
                player.getBought.size should be (0)
                player.listStreets shouldEqual("")
            }
            "only be equal to a player with the same name" in {
                player.equals(Player(player.getName, 0, fields.head, Set(), new FieldIterator(fields))) should be(true)
                player.equals(Player("Maddin", 0, fields.head, Set(), new FieldIterator(fields))) should be(false)
            }
        }
        "walking" should {
            val go = ActionField("Go")
            val street1 = Street("Street1", 50, Array(1,2,3,4,5), 25)
            var street2 = Street("Street2", 100, Array(2,4,6,8,10), 50)
            val fields = List(go, street1, street2)
            val player = Player("name", 1500, fields.head, Set(), new FieldIterator(fields))
            "stand on the correct field" in {
                val (movedPlayer, _) = player.walk(1)
                movedPlayer.getCurrentField should be(fields(1))
            }
            "earn 200 when passing Go" in {
                val (movedPlayer, passedGo) = player.walk(4)
                passedGo should be(true)
                movedPlayer.getMoney should be(1700)
            }

        }
    }
}
