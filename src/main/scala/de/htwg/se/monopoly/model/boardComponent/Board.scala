package de.htwg.se.monopoly.model.boardComponent

import de.htwg.se.monopoly.model.playerComponent.Player
import de.htwg.se.monopoly.util.PlayerIterator

case class Board(fields: List[Field], currentPlayer: Player, playerIt: PlayerIterator) {

    def nextPlayer = playerIt.next()

    def replacePlayer(player: Player, newPlayer: Player): Board = {
        playerIt.replace(player, newPlayer)
        this.copy(currentPlayer = if(currentPlayer == player) newPlayer else currentPlayer)
    }

    def replaceField(field: Field, newField : Field) : Board = {
        val newFields = fields.updated(fields.indexOf(field), newField)
        var newPlayers : List[Player] = List()
        for(player <- playerIt.list) {
            newPlayers = player.copy(fieldIt = player.fieldIt.replace(field, newField)) :: newPlayers
        }
        this.copy(fields = newFields, playerIt = new PlayerIterator(newPlayers.toArray))
    }
}
