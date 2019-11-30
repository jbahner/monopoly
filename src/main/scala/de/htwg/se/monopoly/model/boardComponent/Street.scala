package de.htwg.se.monopoly.model.boardComponent

import de.htwg.se.monopoly.model.playerComponent.Player
import play.api.libs.json.{JsObject, Json}

case class Street(name: String, price: Int, rentCosts: Array[Int], houseCost : Int, numHouses : Int = 0, isBought:Boolean = false) extends Buyable(name, price, isBought) {

    def buyHouses(amount : Int) : Street = {
        this.copy(numHouses = numHouses + amount)
    }

    def setBought() : Street = this.copy(isBought = true)

    def getJSON: JsObject = Json.obj("name" -> name, "houses" -> numHouses)
}
