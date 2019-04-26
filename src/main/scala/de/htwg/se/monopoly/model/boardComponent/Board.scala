package de.htwg.se.monopoly.model.boardComponent

import de.htwg.se.monopoly.model.playerComponent.Player
import de.htwg.se.monopoly.util.PlayerIterator

case class Board(fields: List[Field], currentPlayer: Player, playerIt: PlayerIterator) {

    def nextPlayer = playerIt.next()

    def replacePlayer(player: Player, newPlayer: Player): Board = {
        playerIt.replace(player, newPlayer)
        this.copy(currentPlayer = if(currentPlayer == player) newPlayer else currentPlayer)
    }
}
