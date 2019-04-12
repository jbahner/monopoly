package de.htwg.se.monopoly.model.boardComponent

import org.scalatest.{Matchers, WordSpec}

class BuildingSpec extends WordSpec with Matchers {
    "A Building" when {
        "new" should {
            val building = Building("buildingName", 100)
            "should have a name" in {
                building.name should be("buildingName")
            }
            "should have a price" in {
                building.price should be(100)
            }
        }
    }
}
