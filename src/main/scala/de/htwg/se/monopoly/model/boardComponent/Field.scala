package de.htwg.se.monopoly.model.boardComponent

import de.htwg.se.monopoly.model.playerComponent.Player

abstract class Field(name: String) {
    def action(player: Player)

    def getName: String = name
}
