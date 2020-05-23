package monopoly.controller.controllerBaseImpl

import boardComponent.IBoard
import com.google.inject.{Guice, Injector}
import gamestate.GameStatus.BuildStatus.BuildStatus
import gamestate.GameStatus.{BuildStatus, GameStatus, _}
import monopoly.MonopolyModule
import monopoly.controller._
import monopoly.util.fileIo.IFileIo
import monopoly.util.{GeneralUtil, RentContext, UndoManager}
import play.api.libs.json.{JsObject, JsValue, Json}
import playerModule.fieldComponent.{Field, IBuyable, IStreet}
import playerModule.playerComponent.IPlayer

import scala.swing.Publisher
import scala.swing.event.Event
import scala.xml.Elem

class Controller extends IController with Publisher {

    val injector: Injector = Guice.createInjector(new MonopolyModule)
    val undoManager = new UndoManager

    RentContext.controller = this
    private val fileIo = injector.getInstance(classOf[IFileIo])
    var controllerState: GameStatus = START_OF_TURN
    var buildStatus: BuildStatus = BuildStatus.DEFAULT

    var board: IBoard = _
    var currentDice: (Int, Int) = (0, 0)
    var currentGameMessage: String = _

    def setUp: Unit = {
        undoManager.doStep(new SetupCommand(Set("Player1", "Player2"), this))
        controllerState = START_OF_TURN
        publish(new UpdateInfo)
    }

    def rollDice: Unit = {
        val r = scala.util.Random
        currentDice = (r.nextInt(6) + 1, r.nextInt(6) + 1)
        catCurrentGameMessage
        undoManager.doStep(WalkCommand(currentDice, this))
        publish(new UpdateInfo)
    }

    def catCurrentGameMessage: String = {
        controllerState match {
            case START_OF_TURN => currentGameMessage = userInputString("\"r\" to roll, \"q\" to quit, \"u\" to undo or \"re\" to redo!")
                currentGameMessage
            case ROLLED => currentGameMessage = infoString("Rolled: " + currentDice._1 + " and " + currentDice._2 + "\n")
                currentGameMessage
            case PASSED_GO => currentGameMessage += infoString("Received 200€ by passing Go\n")
                currentGameMessage
            case NEW_FIELD => currentGameMessage = infoString("New Field: " + getCurrentField.getName + "\n")
                currentGameMessage
            case ALREADY_BOUGHT => currentGameMessage += infoString("You already own this street\n")
                currentGameMessage
            case CAN_BUY =>
                val field: IBuyable = getCurrentField.asInstanceOf[IBuyable]
                currentGameMessage += userInputString("Do you want to buy %s for %d€? (Y/N)".format(field.getName, field.getPrice) + "\n")
                currentGameMessage
            case BOUGHT_BY_OTHER =>
                val field = getCurrentField.asInstanceOf[IBuyable]
                currentGameMessage += infoString("Field already bought by " + getBuyer(field).get.getName + ".\n" +
                  "You must pay " + RentContext.rentStrategy.executeStrategy(field) + " rent!\n")
                currentGameMessage
            case CAN_BUILD =>
                buildStatus match {
                    case BuildStatus.DEFAULT => val wholeGroups = GeneralUtil.getWholeGroups(getCurrentPlayer.get)
                        currentGameMessage += userInputString("You can build on: \n" + buildablesToString(wholeGroups) +
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
                    //TODO delete if not needed
                    //case BuildStatus.DONE => currentGameMessageString = ""
                    //    currentGameMessageString
                }
            case DONE => currentGameMessage = turnString(getCurrentPlayer.get.getName + " ended his turn.\n\n")
                currentGameMessage
            case NEXT_PLAYER => currentGameMessage = turnString("Next player: " + getCurrentPlayer.get.getName + "\n") + playerInfoString(getCurrentPlayer.get.getDetails)
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

    def buildablesToString(buildables: List[Set[String]]): String = {
        val sb = new StringBuilder()
        buildables.foreach(set => {
            sb.append("\t")
            set.foreach(s => sb.append(s).append(" (").append(getFieldByName(s).get.asInstanceOf[IStreet].getHouseCost).append("€)\n"))
        })
        sb.toString()
    }

    def nextPlayer: Unit = {
        board = board.nextPlayerTurn()
        updateCurrentPlayerInfo
        publish(new UpdateInfo)
    }

    def updateCurrentPlayerInfo: Unit = {
        controllerState = NEXT_PLAYER
        publish(new UpdateInfo)
        controllerState = START_OF_TURN
        buildStatus = BuildStatus.DEFAULT
        publish(new UpdateGui)
    }

    def payRent(currentPlayer: IPlayer, field: IBuyable, receiver: IPlayer): Unit = {
        val payAmount = RentContext.rentStrategy.executeStrategy(field)
        if (currentPlayer.getMoney < payAmount) {
            controllerState = MISSING_MONEY
            publish(new UpdateInfo)
        } else {
            board.replacePlayer(currentPlayer, currentPlayer.copy(money = currentPlayer.getMoney - payAmount))
            board.replacePlayer(receiver, receiver.copy(money = receiver.getMoney + payAmount))
        }
        //publish(new UpdateGui)
    }

    def buy: Unit = {
        val currentPlayer = getCurrentPlayer
        val currentField = getCurrentField
        if (currentPlayer.get.getMoney < currentField.getPrice) {
            controllerState = MISSING_MONEY
            publish(new UpdateInfo)
            return
        }
        undoManager.doStep(BuyCommand(currentField.asInstanceOf[IBuyable], this))

    }

    def getCurrentField: Field = board.getCurrentPlayer.getCurrentField

    def getCurrentPlayer: Option[IPlayer] = {
        Option(board.getCurrentPlayer)
    }

    def buildHouses(streetName: String, amount: Int): Unit = {
        val field = getFieldByName(streetName)
        if (field.isEmpty || !field.get.isInstanceOf[IStreet]) {
            buildStatus = BuildStatus.INVALID_ARGS
            return
        }
        val street = field.get.asInstanceOf[IStreet]
        val buyer = getBuyer(street)
        if (buyer.isEmpty || !buyer.get.equals(getCurrentPlayer.get))
            buildStatus = BuildStatus.NOT_OWN
        else if (street.getNumHouses + amount > 5)
            buildStatus = BuildStatus.TOO_MANY_HOUSES
        else if (getCurrentPlayer.get.getMoney < street.getHouseCost * amount)
            buildStatus = BuildStatus.MISSING_MONEY
        else
            undoManager.doStep(BuildCommand(street, amount, this))
        publish(new UpdateInfo)
    }

    def getBuyer(buyable: IBuyable): Option[IPlayer] = {
        val players = board.getPlayerIt.list
        val player = players.find(p => p.getBought.contains(buyable))
        player
    }

    def getFieldByName(name: String): Option[Field] = {
        board.getFields.find(field => field.getName.equals(name))
    }

    def getCurrentGameMessage: String = {
        currentGameMessage
    }

    def getControllerState: GameStatus = {
        controllerState
    }

    def getJSON: JsValue = {
        Json.obj(
            "board" -> Json.obj(
                "state" -> controllerState.toString,
                "current_player" -> getCurrentPlayer.get.getName,
                "players" -> board.getPlayerIt.list.map(p => p.getJSON).toList
            )
        )
    }

    def getUndoManager: UndoManager = {
        undoManager
    }

    def getBuildStatus: BuildStatus = {
        buildStatus
    }

    def getCurrentDice: (Int, Int) = {
        currentDice
    }

    def getBoard: IBoard = board

    def setBoard(board: IBoard): Unit = {
        this.board = board
    }

    def saveGame() = {
        fileIo.save(this)
    }

    def loadGame(path: String = "save-game"): Unit = {
        val (lBoard, lControllerState, lBuildStatus) = fileIo.load(path)
        board = lBoard
        controllerState = lControllerState
        buildStatus = lBuildStatus
        currentGameMessage = ""
        publish(new UpdateInfo)
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
            </build-status>{board.toXml()}<current-dice>
            {currentDice._1 + "," + currentDice._2}
        </current-dice>
            <current-game-message>
                {unstyleString(currentGameMessage)}
            </current-game-message>
        </controller>
    }

    def toJson(): JsObject = {
        Json.obj(
            "controller" -> Json.obj(
                "game-status" -> controllerState,
                "build-status" -> buildStatus,
                "board" -> board.toJson(),
                "current-dice" -> Json.toJson(currentDice._1 + "," + currentDice._2),
                "current-game-message" -> unstyleString(currentGameMessage)
            )
        )
    }

    def unstyleString(input: String): String = {
        input.replaceAll("\\[..", "")
    }
}

class UpdateInfo extends Event

class UpdateGui extends Event

class CatGuiMessage extends Event