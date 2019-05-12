package de.htwg.se.monopoly.model.boardComponent

import de.htwg.se.monopoly.controller.GameStatus.GameStatus
import de.htwg.se.monopoly.model.playerComponent.Player

abstract class Field(name: String) {
    def action(player: Player) : GameStatus

    def getName: String = name
}
