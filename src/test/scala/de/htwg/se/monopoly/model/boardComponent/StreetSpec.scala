package de.htwg.se.monopoly.model.boardComponent

import org.scalatest.{Matchers, WordSpec}

class StreetSpec extends WordSpec with Matchers {
    "A Street" when {
        "new" should {
            val street = Street("streetName", 100)
            "should have a name" in {
                street.name should be("streetName")
            }
            "should have a price" in {
                street.price should be(100)
            }
        }
    }
}
