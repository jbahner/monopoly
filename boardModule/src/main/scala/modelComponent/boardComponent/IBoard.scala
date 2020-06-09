package modelComponent.boardComponent

import play.api.libs.json.JsObject
import modelComponent.fieldComponent.{Field, IBuyable}
import modelComponent.playerComponent.IPlayer
import modelComponent.util.{GeneralUtil, PlayerIterator}

import scala.xml.Elem

trait IBoard {
    var currentDice: Int

    def nextPlayer(): IPlayer

    def nextPlayerTurn(): IBoard

    def replacePlayer(player: IPlayer, newPlayer: IPlayer): IBoard

    def replaceField(field: IBuyable, newField: IBuyable): IBoard

    def getPlayerIt: PlayerIterator

    def getFields: List[Field]

    def getCurrentPlayer: Option[IPlayer]

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

    def buyCurrentField(): IBoard

    def buildHouses(streetName: String, amount: Int): IBoard

    def getAmountOfHousesOnStreet(streetName: String): Int

    def canCurrentPlayerBuildOnStreet(streetName: String) : Boolean

    def getCurrentPlayerMoney(): Int

    def getStreetsHouseCost(streetName: String): Int

    def rollDice(): (Int, Int)

    def currentPlayerWalk(): IBoard

    def getDidPlayerPassGo(): Boolean

    def getNewGameStateAfterWalk(): String

    def canCurrentPlayerBuyHouses(): Boolean
}
