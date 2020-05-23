package boardComponent

import play.api.libs.json.JsObject
import playerModule.fieldComponent.{Field, IBuyable}
import playerModule.playerComponent.IPlayer
import playerModule.util.PlayerIterator

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
