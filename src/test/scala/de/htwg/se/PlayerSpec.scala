package de.htwg.se
import org.scalatest._

class PlayerSpec extends WordSpec with Matchers {
  "A Player" when {
    "new" should {
      val player = new Player("name", 1500)
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
