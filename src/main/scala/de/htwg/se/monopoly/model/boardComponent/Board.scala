package de.htwg.se.monopoly.model.boardComponent

import de.htwg.se.monopoly.model.playerComponent.Player
import de.htwg.se.monopoly.util.PlayerIterator

case class Board(fields: List[Field], playerIt : PlayerIterator) {

    def currentPlayer = playerIt.get()

    def nextPlayer = playerIt.next()

    def replacePlayer(player: Player) = playerIt.replace(player)
}
