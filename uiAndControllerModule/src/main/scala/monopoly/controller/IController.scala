package monopoly.controller

import com.google.inject.Injector
import model.fieldComponent.{Field, IBuyable}
import model.gamestate.GameStatus.BuildStatus.BuildStatus
import model.gamestate.GameStatus._
import model.playerComponent.IPlayer
import monopoly.util.UndoManager
import play.api.libs.json.{JsObject, JsValue}

import scala.swing.Publisher
import scala.xml.Elem

trait IController extends Publisher {

    val injector: Injector
    var controllerState: GameStatus
    var buildStatus: BuildStatus
    var currentGameMessage: String
    var currentDice: (Int, Int)
    var board:String
    val STATIC_RENT_AMOUNT: Int



    def setUp: Unit

    def rollDice: Unit

    def nextPlayer: Unit

    def updateCurrentPlayerInfo: Unit

//    def payRent(currentPlayer: IPlayer, field: IBuyable, receiver: IPlayer)

    def buy: Unit

    def buildHouses(streetName: String, amount: Int): Unit

    def getCurrentPlayer: Option[String]

    def catCurrentGameMessage: String

    def turnString(message: String): String

    def playerInfoString(message: String): String

    def infoString(message: String): String

    def userInputString(message: String): String

    def errorString(message: String): String

    def getCurrentGameMessage: String

    def buildablesToString(buildables: List[Set[String]]): String

    def getControllerState: GameStatus

    def getUndoManager: UndoManager

    def getBuildStatus: BuildStatus

    def getCurrentDice: (Int, Int)

    def unstyleString(input: String): String

    def toXml(): Elem

    def toJson(): JsObject

    def saveGame()

    def loadGame(path: String = "save-game")

    def shutdown(): Unit
}
