package de.htwg.se.monopoly.model.boardComponent

import de.htwg.se.monopoly.model.playerComponent.IPlayer
import de.htwg.se.monopoly.util.PlayerIterator
import play.api.libs.json.JsObject

import scala.xml.Elem

trait IBoard {

    def nextPlayer(): IPlayer

    def nextPlayerTurn(): IBoard

    def replacePlayer(player: IPlayer, newPlayer: IPlayer): IBoard

    def replaceField(field: IBuyable, newField: IBuyable): IBoard

    def getPlayerIt: PlayerIterator

    def getFields: List[Field]

    def getCurrentPlayer: IPlayer

    def copy(fields: List[Field], currentPlayer: IPlayer, playerIt: PlayerIterator): IBoard

    def toXml(): Elem

    def toJson(): JsObject
}
