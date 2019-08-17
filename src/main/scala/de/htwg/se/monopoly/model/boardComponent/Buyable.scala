package de.htwg.se.monopoly.model.boardComponent
import de.htwg.se.monopoly.controller.GameStatus._
import de.htwg.se.monopoly.model.playerComponent.Player

abstract class Buyable(name: String, price: Int, isBought : Boolean = false) extends Field(name) {
    override def action(player: Player): GameStatus = {
        if(isBought) {
            if(player.bought.contains(this)) ALREADY_BOUGHT
            else BOUGHT_BY_OTHER
        }
        else CAN_BUY
    }

    def getPrice : Int = price

    def getRent(): Int
}
