package de.htwg.se.monopoly.model.boardComponent
import de.htwg.se.monopoly.model.boardComponent.boardBaseImpl.{Board, Buyable}
import de.htwg.se.monopoly.model.playerComponent.IPlayer

trait IBoard {

    def nextPlayer(): IPlayer

    def nextPlayerTurn(): Board

    def replacePlayer(player: IPlayer, newPlayer: IPlayer): Board

    def replaceField(field: Buyable, newField: Buyable): Board
}
