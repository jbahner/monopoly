package monopoly.controller.controllerMockImpl

import boardComponent.IBoard
import com.google.inject.{Guice, Injector}
import gamestate.GameStatus.BuildStatus.BuildStatus
import gamestate.GameStatus.GameStatus
import monopoly.MonopolyModule
import monopoly.controller.IController
import monopoly.util.UndoManager
import play.api.libs.json.{JsObject, JsValue}
import playerModule.fieldComponent.{Field, IBuyable}
import playerModule.playerComponent.IPlayer

import scala.swing.Publisher
import scala.xml.Elem

class MockController extends IController with Publisher {
    val injector: Injector = Guice.createInjector(new MonopolyModule)
    var controllerState: GameStatus = _
    var buildStatus: BuildStatus = _
    var currentGameMessage: String = _
    var currentDice: (Int, Int) = _

    def getBoard: IBoard = ???

    def setBoard(board: IBoard): Unit = ???

    def setUp: Unit = {
        print("THIS IS A MOCK IMPLEMENTATION\n")
        throw new Exception("MOCK IMPLEMENTATION")
    }

    def getBuyer(buyable: IBuyable): Option[IPlayer] = ???

    def rollDice: Unit = ???

    def nextPlayer: Unit = ???

    def updateCurrentPlayerInfo: Unit = ???

    def payRent(currentPlayer: IPlayer, field: IBuyable, receiver: IPlayer): Unit = ???

    def buy: Unit = ???

    def getFieldByName(name: String): Option[Field] = ???

    def buildHouses(streetName: String, amount: Int): Unit = ???

    def getCurrentField: Field = ???

    def getCurrentPlayer: Option[IPlayer] = ???

    def catCurrentGameMessage: String = ???

    def turnString(message: String): String = ???

    def playerInfoString(message: String): String = ???

    def infoString(message: String): String = ???

    def userInputString(message: String): String = ???

    def errorString(message: String): String = ???

    def getCurrentGameMessage: String = ???

    def buildablesToString(buildables: List[Set[String]]): String = ???

    def getJSON: JsValue = ???

    def getControllerState: GameStatus = ???

    def getUndoManager: UndoManager = ???

    def getBuildStatus: BuildStatus = ???

    def getCurrentDice: (Int, Int) = ???

    override def toXml(): Elem = ???

    override def saveGame(): Unit = ???

    override def loadGame(path: String): Unit = ???

    override def unstyleString(input: String): String = ???

    override def toJson(): JsObject = ???
}
