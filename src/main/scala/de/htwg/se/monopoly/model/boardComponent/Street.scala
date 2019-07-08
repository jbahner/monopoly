package de.htwg.se.monopoly.model.boardComponent

import de.htwg.se.monopoly.model.playerComponent.Player

case class Street(name: String, price: Int, rentCosts: Array[Int], isBought:Boolean = false) extends Buyable(name, price, isBought) {
    override def getRent: Int = rentCosts(0)
}
