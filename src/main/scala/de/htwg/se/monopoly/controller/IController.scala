package de.htwg.se.monopoly.controller

import com.google.inject.Injector
import de.htwg.se.monopoly.controller.GameStatus.BuildStatus.BuildStatus
import de.htwg.se.monopoly.controller.GameStatus.GameStatus
import de.htwg.se.monopoly.model.boardComponent.{Field, IBoard, IBuyable}
import de.htwg.se.monopoly.model.playerComponent.IPlayer
import de.htwg.se.monopoly.util.UndoManager
import play.api.libs.json.{JsObject, JsValue}

import scala.swing.Publisher
import scala.xml.Elem

trait IController extends Publisher {

    val injector: Injector
    var controllerState: GameStatus
    var buildStatus: BuildStatus
    var currentGameMessage: String
    var currentDice: (Int, Int)

    def getBoard: IBoard

    def setBoard(board: IBoard): Unit

    def setUp: Unit

    def getBuyer(buyable: IBuyable): Option[IPlayer]

    def rollDice: Unit

    def nextPlayer: Unit

    def updateCurrentPlayerInfo: Unit

    def payRent(currentPlayer: IPlayer, field: IBuyable, receiver: IPlayer)

    def buy: Unit

    def getFieldByName(name: String): Option[Field]

    def buildHouses(streetName: String, amount: Int): Unit

    def getCurrentField: Field

    def getCurrentPlayer: Option[IPlayer]

    def catCurrentGameMessage: String

    def turnString(message: String): String

    def playerInfoString(message: String): String

    def infoString(message: String): String

    def userInputString(message: String): String

    def errorString(message: String): String

    def getCurrentGameMessage: String

    def buildablesToString(buildables: List[Set[String]]): String

    def getJSON: JsValue

    def getControllerState: GameStatus

    def getUndoManager: UndoManager

    def getBuildStatus: BuildStatus

    def getCurrentDice: (Int, Int)

    def unstyleString(input: String): String

    def toXml(): Elem

    def toJson(): JsObject

    def saveGame()

    def loadGame(path: String = "save-game")
}
