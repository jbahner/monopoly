package de.htwg.se.monopoly.model.boardComponent

import de.htwg.se.monopoly.model.playerComponent.Player

case class Street(name: String, price: Int, rentCosts: Array[Int], houseCost : Int, numHouses : Int = 0, isBought:Boolean = false) extends Buyable(name, price, isBought) {

    override def getRent(): Int = {
        rentCosts(numHouses)
    }

    def buyHouses(amount : Int) : Street = {
        this.copy(numHouses = numHouses + amount)
    }

    def setBought() : Street = this.copy(isBought = true)
}
