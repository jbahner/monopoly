package de.htwg.se.monopoly.model.boardComponent.boardBaseImpl.cards

import de.htwg.se.monopoly.model.boardComponent.Card
import de.htwg.se.monopoly.model.playerComponent.IPlayer

class MoveCard(override val message: String, fieldName: String, overGo: Boolean) extends Card {
    override def action(player: IPlayer): IPlayer = {
        player.walk(player.stepsUntil(fieldName), overGo)._1
    }
}
