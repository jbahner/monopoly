package de.htwg.se.monopoly.model.boardComponent

import de.htwg.se.monopoly.model.playerComponent.IPlayer

abstract class Card {
    val message : String

    def action(player: IPlayer) : IPlayer
}
