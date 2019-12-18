package de.htwg.se.monopoly.model.boardComponent.boardBaseImpl

import de.htwg.se.monopoly.model.boardComponent.{Field, IBoard}
import de.htwg.se.monopoly.model.playerComponent.IPlayer
import de.htwg.se.monopoly.util.PlayerIterator

case class Board(fields: List[Field], currentPlayer: IPlayer, playerIt: PlayerIterator) extends IBoard {

    override def nextPlayer(): IPlayer = playerIt.next()

    override def nextPlayerTurn() : Board = this.copy(currentPlayer = this.nextPlayer())

    override def replacePlayer(player: IPlayer, newPlayer: IPlayer): Board = {
        playerIt.replace(player, newPlayer)
        this.copy(currentPlayer = if(currentPlayer == player) newPlayer else currentPlayer)
    }

    override def replaceField(field: Buyable, newField : Buyable) : Board = {
        val newFields = fields.updated(fields.indexOf(field), newField)
        var newPlayers : List[IPlayer] = List()
        var currentPlayerIdx = 0
        val players = playerIt.list
        for(player <- players) {
            if(player == currentPlayer)
                currentPlayerIdx = playerIt.list.indexOf(player)
            val bought = player.getBought.map(f => if(f == field) newField else f)
            var newPlayerField = player.getCurrentField
            if(player.getCurrentField == field) {
                newPlayerField = newField
            }
            newPlayers = newPlayers :+ player.copy(fieldIt = player.getFieldIt.replace(field, newField),currentField = newPlayerField, bought = bought)
        }
        this.copy(fields = newFields, currentPlayer = newPlayers(currentPlayerIdx), playerIt = new PlayerIterator(newPlayers.toArray, playerIt.currentIdx))
    }
}
