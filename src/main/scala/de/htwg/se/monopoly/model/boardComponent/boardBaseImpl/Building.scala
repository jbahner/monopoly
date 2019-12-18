package de.htwg.se.monopoly.model.boardComponent.boardBaseImpl

case class Building(name: String, price: Int, isBought :Boolean = false) extends Buyable(name, price, isBought) {
    def setBought() : Building = this.copy(isBought = true)
}
