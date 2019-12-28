package de.htwg.se.monopoly.model.boardComponent

import de.htwg.se.monopoly.model.boardComponent.boardBaseImpl.cards.{MoveCard, PayCard}
import de.htwg.se.monopoly.model.boardComponent.boardBaseImpl.{ActionField, Street}
import de.htwg.se.monopoly.model.playerComponent.playerBaseImpl.Player
import de.htwg.se.monopoly.util.FieldIterator
import org.scalatest.{Matchers, WordSpec}

class CardSpec extends WordSpec with Matchers {
    "A Card" when {
        val go = ActionField("Go")
        val street1 = Street("Street1", 50, Array(1, 2, 3, 4, 5), 25)
        val street2 = Street("Street2", 100, Array(2, 4, 6, 8, 10), 50)
        val fields = List(go, street1, street2)
        val player = Player("name", 1500, fields.head, Set(), new FieldIterator(fields))
        "A MoveCard" should {
            "with passing Go" should {
                val moveCard = new MoveCard("Walk until Go", "Go", true)
                val walkedPlayer = moveCard.action(player)
                "walk to the correct field" in {
                    walkedPlayer.getCurrentField should be(go)
                }
                "receive money when passing Go" in {
                    walkedPlayer.getMoney should be(player.getMoney + 200)
                }
            }
            "without passing Go" should {
                val moveCard = new MoveCard("Walk until Go", "Go", false)
                val walkedPlayer = moveCard.action(player)
                "walk to the correct field" in {
                    walkedPlayer.getCurrentField should be(go)
                }
                "not receive money" in {
                    walkedPlayer.getMoney should be(player.getMoney)
                }
            }
        }
        "A PayCard" should {
            "add the correct money" in {
                val payCard = new PayCard("Receive 100", 100)
                payCard.action(player).getMoney should be(player.getMoney + 100)
            }
            "subtract the correct money" in {
                val payCard = new PayCard("Pay 100", -100)
                payCard.action(player).getMoney should be(player.getMoney - 100)
            }
        }
    }
}
