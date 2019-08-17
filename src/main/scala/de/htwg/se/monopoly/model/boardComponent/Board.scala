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
        var currentPlayerIdx = 0
        val players = playerIt.list
        for(player <- players) {
            if(player == currentPlayer)
                currentPlayerIdx = playerIt.list.indexOf(player)
            var bought = player.bought
            if(player.bought.contains(field.asInstanceOf[Buyable]))
                bought = player.bought.map(f => if(f == field.asInstanceOf[Buyable]) newField.asInstanceOf[Buyable] else f)
            newPlayers = newPlayers :+ player.copy(fieldIt = player.fieldIt.replace(field, newField), bought = bought)
        }
        this.copy(fields = newFields, currentPlayer = newPlayers(currentPlayerIdx), playerIt = new PlayerIterator(newPlayers.toArray))
    }
}
