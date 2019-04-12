package de.htwg.se.monopoly.model.playerComponent

import de.htwg.se.monopoly.model.boardComponent.{ActionField, Street}
import de.htwg.se.monopoly.util.FieldIterator
import org.scalatest._

class PlayerSpec extends WordSpec with Matchers {
    "A Player" when {
        "new" should {
            val go = ActionField("Go")
            val street1 = Street("Street1", 50)
            val street2 = Street("Street2", 100)
            val fields = List(go, street1, street2)
            val player = Player("name", 1500, fields.head, new FieldIterator(fields))
            "have a name" in {
                player.name should be("name")
            }
            "have money" in {
                player.money should be(1500)
            }
            "have a String representation" in {
                player.toString should be("name, money: 1500")
            }
            "stand on the first field" in {
                player.currentField should be(fields.head)
            }
        }
        "walking" should {
            val go = ActionField("Go")
            val street1 = Street("Street1", 50)
            val street2 = Street("Street2", 100)
            val fields = List(go, street1, street2)
            val player = Player("name", 1500, fields.head, new FieldIterator(fields))
            "stand on the correct field" in {
                val movedPlayer = player.walk(1)
                movedPlayer.currentField should be(fields(1))
            }
            "earn 200 when passing Go" in {
                val movedPlayer = player.walk(4)
                movedPlayer.money should be(1700)
            }

        }
    }
}
