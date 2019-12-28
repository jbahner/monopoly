package de.htwg.se.monopoly.model.boardComponent.boardBaseImpl.cards

import de.htwg.se.monopoly.model.boardComponent.Card
import de.htwg.se.monopoly.model.playerComponent.IPlayer

class PayCard(override val message: String, amount: Int) extends Card {
    override def action(player: IPlayer): IPlayer = {
        // negative value will be subtracted, positive will be added
        player.adjustMoney(amount)
    }
}
