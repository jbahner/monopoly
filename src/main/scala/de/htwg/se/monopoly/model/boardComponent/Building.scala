package de.htwg.se.monopoly.model.boardComponent

import de.htwg.se.monopoly.model.playerComponent.Player

case class Building(name: String, price: Int, isBought :Boolean = false) extends Buyable(name, price, isBought) {
}
