package de.htwg.se.monopoly.model.playerComponent

import de.htwg.se.monopoly.model.boardComponent.{Field, Street}
import de.htwg.se.monopoly.util.FieldIterator
import org.scalatest._

class PlayerSpec extends WordSpec with Matchers {
  "A Player" when {
    "new" should {
      val player = Player("name", 1500, new FieldIterator(List(Street("Street1", 50), Street("Street2", 100))))
      "have a name" in {
        player.name should be("name")
      }
      "have money" in {
        player.money should be(1500)
      }
      "have a String representation" in {
        player.toString should be("name")
      }
    }
  }
}
