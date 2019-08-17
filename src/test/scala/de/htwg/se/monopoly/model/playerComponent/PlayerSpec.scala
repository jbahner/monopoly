package de.htwg.se.monopoly.model.playerComponent

import de.htwg.se.monopoly.model.boardComponent.{ActionField, Street}
import de.htwg.se.monopoly.util.FieldIterator
import org.scalatest._

class PlayerSpec extends WordSpec with Matchers {
    "A Player" when {
        "new" should {
            val go = ActionField("Go")
            val street1 = Street("Street1", 50, Array(1,2,3,4,5), 25)
            val street2 = Street("Street2", 100, Array(2,4,6,8,10), 50)
            val fields = List(go, street1, street2)
            val player = Player("name", 1500, fields.head, Set(), new FieldIterator(fields))
            "have a name" in {
                player.name should be("name")
            }
            "have money" in {
                player.money should be(1500)
            }
            "have a String representation" in {
                player.toString should be("name, money: 1500")
            }
            "have a String representation with details" in {
                val testPlayer = Player("player", 1500, fields.head, Set(street1), new FieldIterator(fields))
                testPlayer.getDetails should include(player.money.toString)
                testPlayer.getDetails should include(street1.getName)
                testPlayer.getDetails should include(fields.head.getName)
            }
            "stand on the first field" in {
                player.currentField should be(fields.head)
            }
            "have no bought streets" in {
                player.bought.size should be (0)
                player.listStreets shouldEqual("")
            }
            "only be equal to a player with the same name" in {
                player.equals(Player(player.name, 0, fields.head, Set(), new FieldIterator(fields))) should be(true)
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
                movedPlayer.currentField should be(fields(1))
            }
            "earn 200 when passing Go" in {
                val (movedPlayer, passedGo) = player.walk(4)
                passedGo should be(true)
                movedPlayer.money should be(1700)
            }

        }
    }
}
