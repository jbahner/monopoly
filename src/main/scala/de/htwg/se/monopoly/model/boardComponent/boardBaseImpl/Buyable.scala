package de.htwg.se.monopoly.model.boardComponent.boardBaseImpl

import de.htwg.se.monopoly.controller.GameStatus._
import de.htwg.se.monopoly.model.boardComponent.Field
import de.htwg.se.monopoly.model.playerComponent.IPlayer

abstract class Buyable(name: String, price: Int, isBought: Boolean = false) extends Field(name) {
    override def action(player: IPlayer): GameStatus = {
        if (isBought) {
            if (player.getBought.contains(this)) ALREADY_BOUGHT
            else BOUGHT_BY_OTHER
        }
        else CAN_BUY
    }

    def getPrice: Int = price
}
