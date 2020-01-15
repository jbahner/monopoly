package de.htwg.se.monopoly.model.boardComponent.boardBaseImpl

import de.htwg.se.monopoly.controller.GameStatus.{ALREADY_BOUGHT, BOUGHT_BY_OTHER, CAN_BUY, GameStatus}
import de.htwg.se.monopoly.model.boardComponent.IBuilding
import de.htwg.se.monopoly.model.playerComponent.IPlayer

case class Building (name: String, price: Int, isBought: Boolean = false) extends IBuilding {
    def setBought(): IBuilding = this.copy(isBought = true)

    def getName: String = name

    def copy(name: String, price: Int, isBought: Boolean): IBuilding =
        Building(name, price, isBought)

    def getIsBought: Boolean = isBought

    override def action(player: IPlayer): GameStatus = {
        if (isBought) {
            if (player.getBought.contains(this)) ALREADY_BOUGHT
            else BOUGHT_BY_OTHER
        }
        else CAN_BUY
    }

    def getPrice: Int = price
}