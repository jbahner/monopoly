package monopoly.controller.controllerBaseImpl

import java.io
import java.io.{File, FileInputStream, InputStream}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.{Guice, Injector}
import monopoly.controller._
import monopoly.controller.gamestate.GameStatus.BuildStatus.BuildStatus
import monopoly.controller.gamestate.GameStatus._
import monopoly.util.UndoManager
import monopoly.util.fileIo.IFileIo
import monopoly.{MainComponentServer, MonopolyModule}
import play.api.libs.json.{JsObject, JsValue, Json}

import scala.concurrent.ExecutionContextExecutor
import scala.swing.Publisher
import scala.swing.event.Event
import scala.xml.Elem

// TODO put this into the board -> The RentContext
class Controller extends IController with Publisher {

    implicit val system: ActorSystem = ActorSystem("Controller-System-Actor")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val injector: Injector = Guice.createInjector(new MonopolyModule)
    val undoManager = new UndoManager


    private val fileIo = injector.getInstance(classOf[IFileIo])
    var controllerState: GameStatus = START_OF_TURN
    var buildStatus: BuildStatus = BuildStatus.DEFAULT

    var board: String = _
    var currentDice: (Int, Int) = (0, 0)
    var currentGameMessage: String = _

    def setUp(): Unit = {
        board = undoManager.doStep(new SetupCommand(Set("Player1", "Player2"), this))
        controllerState = START_OF_TURN
        publish(new UpdateInfo)
    }

    def rollDice(): Unit = {
        val (tmpBoard, d1, d2) = MainComponentServer.rollDice(board)
        currentDice = (d1, d2)
        board = tmpBoard

        catCurrentGameMessage()
        board = undoManager.doStep(WalkCommand(currentDice, this))
        publish(new UpdateInfo)
    }

    def catCurrentGameMessage(): String = {
        controllerState match {
            case START_OF_TURN => currentGameMessage = userInputString("\"r\" to roll, \"q\" to quit, \"u\" to undo or \"re\" to redo!")
                currentGameMessage
            case ROLLED => currentGameMessage = infoString("Rolled: " + currentDice._1 + " and " + currentDice._2 + "\n")
                currentGameMessage
            case PASSED_GO => currentGameMessage += infoString("Received 200€ by passing Go\n")
                currentGameMessage
            case NEW_FIELD => currentGameMessage = infoString("New Field: " + MainComponentServer.getCurrentFieldName(board) + "\n")
                currentGameMessage
            case ALREADY_BOUGHT => currentGameMessage += infoString("You already own this street\n")
                currentGameMessage
            case CAN_BUY =>
                currentGameMessage += userInputString("Do you want to buy %s ? (Y/N)".format(MainComponentServer.getCurrentFieldName(board)) + "\n")
                currentGameMessage
            case BOUGHT_BY_OTHER =>
                val currentFieldName = MainComponentServer.getCurrentFieldName(board)
                currentGameMessage += infoString("Field already bought by " + getOwnersName(currentFieldName) + ".\n" +
                    // RentPay 1
                    "You must pay " + MainComponentServer.getCurrentFieldRent(board) + " rent!\n")
                currentGameMessage
            case CAN_BUILD =>
                buildStatus match {
                    case BuildStatus.DEFAULT => currentGameMessage += userInputString("You can build on: \n" + MainComponentServer.getPossibleBuildPlacesToString(board) +
                        "\nType the name of the street and the amount of houses you want to build. Press \"q\" to quit, \"u\" to undo or \"re\" to redo.\n")
                        currentGameMessage
                    case BuildStatus.BUILT => currentGameMessage = infoString("Successfully built!\n")
                        currentGameMessage
                    case BuildStatus.INVALID_ARGS => currentGameMessage
                    case BuildStatus.NOT_OWN => currentGameMessage = errorString("You don't own this street!")
                        currentGameMessage
                    case BuildStatus.TOO_MANY_HOUSES => currentGameMessage += errorString("There can only be 5 houses on a street\n")
                        currentGameMessage
                    case BuildStatus.MISSING_MONEY => currentGameMessage += errorString("You don't have enough money!\n")
                        currentGameMessage
                }
            case DONE => currentGameMessage = turnString(MainComponentServer.getCurrentPlayerName(board) + " ended his turn.\n\n")
                currentGameMessage
            case NEXT_PLAYER => currentGameMessage = turnString("Next player: " + MainComponentServer.getCurrentPlayerName(board) + "\n") +
                playerInfoString(MainComponentServer.getCurrentPlayerDetails(board))
                currentGameMessage
            case MISSING_MONEY => currentGameMessage = "You do not have enough money!"
                currentGameMessage
            case BOUGHT => currentGameMessage = infoString("Successfully bought the street")
                currentGameMessage
            case NOTHING => currentGameMessage = ""
                currentGameMessage
            case _ => ""
        }
    }

    def turnString(message: String): String = Console.BOLD + Console.UNDERLINED + Console.GREEN + message + Console.RESET

    def playerInfoString(message: String): String = Console.BOLD + Console.MAGENTA + message + Console.RESET

    def infoString(message: String): String = Console.BOLD + Console.BLUE + message + Console.RESET

    def userInputString(message: String): String = Console.BOLD + Console.YELLOW + message + Console.RESET

    def errorString(message: String): String = Console.BOLD + Console.RED + message + Console.RESET

    def getOwnersName(streetName: String): String = {
        MainComponentServer.getOwnersName(board, streetName)
    }

    def nextPlayer(): Unit = {

        val boardString: String = MainComponentServer.requestNextPlayer(board)
        board = boardString

        updateCurrentPlayerInfo()
        publish(new UpdateInfo)
    }

    def updateCurrentPlayerInfo(): Unit = {
        controllerState = NEXT_PLAYER
        publish(new UpdateInfo)
        controllerState = START_OF_TURN
        buildStatus = BuildStatus.DEFAULT
        publish(new UpdateGui)
    }

    // TODO put this into the board
    def payRent(): Unit = {
        val payAmount = MainComponentServer.getCurrentFieldRent(board)
        val currentPlayerMoney = MainComponentServer.getCurrentPlayerMoney(board)
        if (currentPlayerMoney < payAmount) {
            controllerState = MISSING_MONEY
            publish(new UpdateInfo)
        } else {

            val tmpBoard = MainComponentServer.currentPlayerPaysRent(board)
            board = tmpBoard
        }
        publish(new UpdateInfo)
    }

    // TODO enable this again
    def buy(): Unit = {
        //        val currentPlayer = getCurrentPlayer()
        //        val currentField = getCurrentField()
        //        if (currentPlayer.get.getMoney < currentField.getPrice) {
        //            controllerState = MISSING_MONEY
        //            publish(new UpdateInfo)
        //            return
        //        }
        //        board = undoManager.doStep(BuyCommand(this))

    }

    def buildHouses(streetName: String, amount: Int): Unit = {

        if (!MainComponentServer.canCurrentPlayerBuildOnStreet(board, streetName))
            buildStatus = BuildStatus.NOT_OWN
        else if (MainComponentServer.getAmountOfHousesOnStreet(board, streetName) + amount > 5)
            buildStatus = BuildStatus.TOO_MANY_HOUSES
        else if (MainComponentServer.getCurrentPlayerMoney(board) < MainComponentServer.getHouseCost(board, streetName) * amount)
            buildStatus = BuildStatus.MISSING_MONEY
        else {
            board = MainComponentServer.buildHouses(board, streetName, amount)
            buildStatus = BuildStatus.BUILT
        }


        publish(new UpdateInfo)
    }

    def getCurrentGameMessage: String = {
        currentGameMessage
    }

    def getControllerState(): GameStatus = {
        controllerState
    }

    def getJSON(): JsValue = {
        Json.obj("test" -> "test")
        //            "board" -> Json.obj(
        //                "state" -> controllerState.toString,
        //                "current_player" -> getCurrentPlayer().get.getName,
        //                "players" -> board.getPlayerIt.list.map(p => p.getJSON).toList
        //            )
    }

    def getUndoManager(): UndoManager = {
        undoManager
    }

    def getBuildStatus(): BuildStatus = {
        buildStatus
    }

    def getCurrentDice: (Int, Int) = {
        currentDice
    }

    def getBoard(): String = board

    def setBoard(board: String): Unit = {
        this.board = board
    }

    def saveGame(): Unit = {
        fileIo.save(this)
    }

    def loadGame(path: String = "save-game"): String = {
        val (lBoard, lControllerState, lBuildStatus) = fileIo.load(new FileInputStream(path))
        controllerState = lControllerState
        buildStatus = lBuildStatus
        currentGameMessage = ""
        publish(new UpdateInfo)
        lBoard
    }

    def toXml(): Elem = {
        //left out classes:
        //  injector,
        //  RentContext
        //  UndoManager
        <controller>
            <game-status>
                {controllerState}
            </game-status>
            <build-status>
                {buildStatus}
            </build-status>
            <!--            {board.toXml()}-->
            <current-dice>
                {currentDice._1 + "," + currentDice._2}
            </current-dice>
            <current-game-message>
                {unstyleString(currentGameMessage)}
            </current-game-message>
        </controller>
    }

    def unstyleString(input: String): String = {
        input.replaceAll("\\[..", "")
    }

    def toJson(): JsObject = {
        Json.obj(
            "controller" -> Json.obj(
                "game-status" -> controllerState,
                "build-status" -> buildStatus,
                //                "board" -> board.toJson(),
                "current-dice" -> Json.toJson(currentDice._1 + "," + currentDice._2),
                "current-game-message" -> unstyleString(currentGameMessage)
            )
        )
    }

    def shutdown(): Unit = {
        // TODO maybe shutdown other services too?
        sys.exit(1)
    }


    //    def buyCurrentField(): IBoard = {
    //        board.buyCurrentField()
    //    }

    def currentPlayerWalk(): String = {
        MainComponentServer.playerWalk(board)
    }


    def getDidPlayerPassGo(): Boolean = {
        (Json.parse(board).as[JsObject] \ "passedGo").as[Boolean]
    }

    override def loadDefaultGame(): String = {
        val (lBoard, lControllerState, lBuildStatus) = fileIo.load(getClass.getResourceAsStream("/save-game.json"))

        controllerState = lControllerState
        buildStatus = lBuildStatus
        currentGameMessage = ""
        publish(new UpdateInfo)
        lBoard
    }
}

class UpdateInfo extends Event

class UpdateGui extends Event

class CatGuiMessage extends Event