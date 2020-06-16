package monopoly.controller

import com.google.inject.Injector
import monopoly.controller.gamestate.GameStatus.BuildStatus.BuildStatus
import monopoly.controller.gamestate.GameStatus.GameStatus
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

    def getBoard(): String

    def setBoard(board: String): Unit

    def setUp(): Unit

    def getOwnersName(streetName: String): String

    def rollDice(): Unit

    def nextPlayer(): Unit

    def updateCurrentPlayerInfo(): Unit

    def payRent()

    def buy(): Unit

    def buildHouses(streetName: String, amount: Int): Unit

    def catCurrentGameMessage(): String

    def turnString(message: String): String

    def playerInfoString(message: String): String

    def infoString(message: String): String

    def userInputString(message: String): String

    def errorString(message: String): String

    def getCurrentGameMessage: String

    def getJSON(): JsValue

    def getControllerState(): GameStatus

    def getUndoManager(): UndoManager

    def getBuildStatus(): BuildStatus

    def getCurrentDice: (Int, Int)

    def unstyleString(input: String): String

    def toXml(): Elem

    def toJson(): JsObject

    def saveGame(): Unit

    def loadGame(path: String = "save-game"): String

    def loadDefaultGame(): String

    def shutdown(): Unit

    def currentPlayerWalk(): String

    def getDidPlayerPassGo(): Boolean

    def loadControllerFromDb(): Unit

    def loadBoardFromDb(): Unit

}
