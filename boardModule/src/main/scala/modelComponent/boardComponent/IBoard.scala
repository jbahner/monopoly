package modelComponent.boardComponent

import play.api.libs.json.JsObject
import modelComponent.fieldComponent.{Field, IBuyable}
import modelComponent.playerComponent.IPlayer
import modelComponent.util.{GeneralUtil, PlayerIterator}

import scala.xml.Elem

trait IBoard {
    val currentDice: Int

    def nextPlayer(): IPlayer

    def nextPlayerTurn(): IBoard

    def replacePlayer(player: IPlayer, newPlayer: IPlayer): IBoard

    def replaceField(field: IBuyable, newField: IBuyable): IBoard

    def getPlayerIt: PlayerIterator

    def getFields: List[Field]

    def getCurrentPlayer: IPlayer

    def copy(fields: List[Field], currentPlayer: IPlayer, playerIt: PlayerIterator, currentDice: Int): IBoard

    def toXml(): Elem

    def toJson(): JsObject

    def getHouseCost(streetName: String): Int

    def getHouseCount(streetName: String): Int

    def getFieldByName(fieldName: String): Option[Field]

    def getCurrentField(): Field

    def getCurrentFieldType(): String

    def getCurrentFieldName(): String

    def getCurrentFieldOwnedByString(): String

    def getBuyer(buyable: IBuyable): Option[IPlayer]

    def getCurrentFieldOwnerName(): String

    def buildablesToString(buildables: List[Set[String]]): String

    def getPossibleBuildPlacesToString(): String

    def getCurrentFieldRent(): Int
}
