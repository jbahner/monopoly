package de.htwg.se.monopoly.controller

import de.htwg.se.monopoly.controller.GameStatus.BuildStatus.BuildStatus
import de.htwg.se.monopoly.controller.GameStatus.GameStatus
import de.htwg.se.monopoly.model.boardComponent.{Buyable, Field}
import de.htwg.se.monopoly.model.playerComponent.Player
import de.htwg.se.monopoly.util.UndoManager
import play.api.libs.json.JsValue

import scala.swing.Publisher

trait IController extends Publisher {

    var controllerState: GameStatus
    var buildStatus: BuildStatus

    def setUp()

    def getBuyer(buyable: Buyable): Option[Player]

    def rollDice(): Unit

    def nextPlayer(): Unit

    def updateCurrentPlayerInfo(): Unit

    def payRent(currentPlayer: Player, field: Buyable, receiver: Player)

    def buy(): Unit

    def getFieldByName(name: String): Option[Field]

    def buildHouses(streetName: String, amount: Int): Unit

    def getCurrentField: Field

    def getCurrentPlayer(): Option[Player]

    def catCurrentGameMessage(): String

    def turnString(message: String): String

    def playerInfoString(message: String): String

    def infoString(message: String): String

    def userInputString(message: String): String

    def errorString(message: String): String

    def getCurrentGameMessage(): String

    def buildablesToString(buildables: List[Set[String]]): String

    def getJSON(): JsValue

    def getControllerState(): GameStatus

    def getUndoManager: UndoManager

    def getBuildStatus: BuildStatus

    def getCurrentDice:(Int, Int)
}
