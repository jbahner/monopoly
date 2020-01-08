package de.htwg.se.monopoly.model.boardComponent.boardBaseImpl

import de.htwg.se.monopoly.model.boardComponent.{Field, IBoard, IBuyable}
import de.htwg.se.monopoly.model.playerComponent.IPlayer
import de.htwg.se.monopoly.util.PlayerIterator

case class Board(fields: List[Field], currentPlayer: IPlayer, playerIt: PlayerIterator) extends IBoard {

    override def nextPlayer(): IPlayer = playerIt.next()

    override def nextPlayerTurn() : IBoard = this.copy(this.getFields, this.nextPlayer(), this.getPlayerIt)

    override def replacePlayer(player: IPlayer, newPlayer: IPlayer): IBoard = {
        playerIt.replace(player, newPlayer)
        this.copy(this.getFields, currentPlayer = if(currentPlayer == player) newPlayer else currentPlayer, this.getPlayerIt)
    }

    def replaceField(field: IBuyable, newField : IBuyable) : IBoard = {
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

    def getPlayerit: PlayerIterator = playerIt

    def getFields: List[Field] = fields

    def getCurrentPlayer: IPlayer = currentPlayer

    def copy (fields: List[Field], currentPlayer: IPlayer, playerIt: PlayerIterator): IBoard = {
        Board(fields, currentPlayer, playerIt)
    }

    def getPlayerIt: PlayerIterator = playerIt

}
