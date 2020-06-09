package monopoly.view

import java.awt.Color
import java.util

import javax.swing.{BorderFactory, ImageIcon}
import monopoly.MainComponentServer
import monopoly.controller.IController
import monopoly.controller.controllerBaseImpl.{CatGuiMessage, UpdateGui, UpdateInfo}
import monopoly.controller.gamestate.GameStatus

import scala.swing._
import scala.swing.event._

class Gui(controller: IController) extends Frame with IUi {

    val windowDimension = new Dimension(800, 400)
    val menuBarDimension = new Dimension(1000, 30)
    var bufferedMessage: String = ""

    listenTo(controller)
    title = "Monopoly+"

    minimumSize = windowDimension

    visible = true
    //pack()

    reactions += {
        case event: UpdateInfo => redraw()
        case event: UpdateGui => redraw()
        case event: CatGuiMessage => catMessage()
    }

    def redraw(): Unit = {
        contents = new BorderPanel {
            add(generateCenterPanel(), BorderPanel.Position.Center)
            add(createMenuBar(), BorderPanel.Position.North)
            add(generateBuildButtons(), BorderPanel.Position.East)
            add(redrawButtons(), BorderPanel.Position.South)
            add(generateLeftPanel(), BorderPanel.Position.West)
            minimumSize = windowDimension

        }
    }

    def createMenuBar(): MenuBar = {
        new MenuBar {
            contents += new Menu("File") {
                contents += new MenuItem(Action("New Game") {
                    controller.setUp
                })
                contents += new MenuItem(Action("Load") {
                    import javax.swing.JFileChooser
                    val chooser = new JFileChooser();
                    chooser.setCurrentDirectory(new java.io.File("."));

                    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        controller.loadGame(chooser.getSelectedFile.getAbsolutePath)
                    }

                })
                contents += new MenuItem(Action("Save") {
                    controller.saveGame()
                })
                contents += new MenuItem(Action("Exit") {
                    closeOperation()
                })
            }
            contents += new Menu("Edit") {
                contents += new MenuItem(Action("Refresh") {
                    controller.publish(new UpdateInfo)
                })
                contents += new MenuItem(Action("Undo") {
                    controller.getUndoManager.undoStep()
                })
                contents += new MenuItem(Action("Redo") {
                    controller.getUndoManager.redoStep()
                })
            }
        }
    }

    override def closeOperation(): Unit = {
        controller.shutdown()
    }

    def redrawButtons(): FlowPanel = {
        // Customizing the buttons
        val buttonList = new util.ArrayList[Button]()

        controller.getControllerState match {
            case GameStatus.START_OF_TURN =>
                buttonList.add(new Button("Roll Dice") {
                    reactions += {
                        case _: ButtonClicked => controller.rollDice
                    }
                })
            case GameStatus.CAN_BUILD =>
                buttonList.add(new Button("End Turn") {
                    reactions += {
                        case _: ButtonClicked => controller.nextPlayer
                    }
                })
            case _ =>
        }

        new FlowPanel {
            buttonList.forEach(button => contents += button)
        }
    }

    def generateBuildButtons(): GridPanel = {
        controller.getControllerState match {
            case GameStatus.CAN_BUILD => new GridPanel(MainComponentServer.getCurrentPlayBoughtStreetsCount(controller.getBoard()), 1) {
                val boughtFields = MainComponentServer.getCurrentPlayerBoughtFieldnames(controller.getBoard())
                boughtFields.foreach(bought => contents += generateBuildButton(bought))
            }
            case _ => new GridPanel(1, 1)
        }

    }

    def generateBuildButton(streetName: String): FlowPanel = {
        new FlowPanel() {
            contents += new Button("Buy House on " + streetName) {
                reactions += {
                    case _: ButtonClicked =>
                        controller.buildHouses(streetName, 1)
                }
                tooltip = MainComponentServer.getHouseCost(controller.getBoard(), streetName) + "€"
            }
            contents += new Label(" -- " + MainComponentServer.getAmountOfHousesOnStreet(controller.getBoard(), streetName))

        }

    }

    def generateCenterPanel(): GridPanel = {
        new GridPanel(1, 2) {
            contents += generateTextPanel()
            contents += generateCenterCurrentFieldDetails()
        }
    }

    def generateCenterCurrentFieldDetails(): GridPanel = {
        new GridPanel(2, 1) {
            val curFieldType: String = MainComponentServer.getCurrentFieldType(controller.getBoard())


            contents += new GridPanel(7, 1) {
                contents += new Label("Current Field") {
                    font = (new Font(font.getFontName, font.getStyle, 20))
                    background = Color.CYAN
                    opaque = true
                }


                contents += new Label(MainComponentServer.getCurrentFieldName(controller.getBoard()))


                curFieldType match {
                    case "Street" =>
                        contents += new Label(MainComponentServer.getOwnersName(controller.getBoard(), MainComponentServer.getCurrentFieldName(controller.getBoard())))

                        contents += new Label("Current Rent: " + MainComponentServer.getCurrentFieldRent(controller.getBoard()))
                        val currentFieldName = MainComponentServer.getCurrentFieldName(controller.getBoard())
                        contents += new Label("Houses: " + MainComponentServer.getAmountOfHousesOnStreet(controller.getBoard(), currentFieldName))
                    case "Building" =>
                        contents += new Label(MainComponentServer.getOwnersName(controller.getBoard(), MainComponentServer.getCurrentFieldName(controller.getBoard())))
                    case _ =>
                }
            }


            if (MainComponentServer.getCurrentFieldName(controller.getBoard()).equals("Go")) {
                contents += new Label() {
                    icon = new ImageIcon("src\\main\\scala\\de\\htwg\\se\\monopoly\\view\\textures\\go_field.png")
                    maximumSize = new Dimension(100, 100)
                }
            }


            border = BorderFactory.createLineBorder(Color.black)

            //var curFieldStreet
            //contents += new Label ("Houses: " + curField.asInstanceOf[Street].numHouses)
            //contents += new Label ("Bought??: " + curField.asInstanceOf[Street].isBought)
        }
    }

    def generateTextPanel(): GridPanel = {
        val currentMsg = getCurrentGameMessage().split("\n")

        new GridPanel(7, 1) {
            contents += new Label("Game Info") {
                font = (new Font(font.getFontName, font.getStyle, 20))
                background = Color.CYAN
                opaque = true
            }
            currentMsg.foreach(cutMsg => this.contents += new Label(cutMsg))
            border = BorderFactory.createLineBorder(Color.black)
        }
    }

    def getCurrentGameMessage(): String = {
        var msg = ""
        controller.getControllerState match {
            case GameStatus.START_OF_TURN =>
                msg = "  " + MainComponentServer.getCurrentPlayerName(controller.getBoard()) + "'s turn.\nIt is your start of the turn!\nRoll the dice.  "
                bufferedMessage = ""
            case GameStatus.CAN_BUILD =>
                controller.getBuildStatus match {
                    case GameStatus.BuildStatus.DEFAULT => msg = bufferedMessage
                    case GameStatus.BuildStatus.BUILT => msg = "  Successfully built house.  "
                    case _ => msg = "  Uncaught BuildStatus  " + controller.getControllerState
                }
            case GameStatus.BOUGHT_BY_OTHER =>
            case _ => "------ ERROR ------: " + controller.getControllerState
        }

        msg
    }

    def generateLeftPanel(): GridPanel = {

        new GridPanel(10, 1) {
            contents += new Label("")
            contents += new Label("  " + MainComponentServer.getCurrentPlayerName(controller.getBoard()) + "    ")
            // TODO current Players money is not displayed correctly
            contents += new Label(MainComponentServer.getCurrentPlayerMoney(controller.getBoard()) + " €")
            contents += new Label("")
        }
    }

    def catMessage(): Unit = {
        controller.getControllerState match {
            case GameStatus.ROLLED =>
                bufferedMessage = "  Rolled " + controller.getCurrentDice._1 + " and " + controller.getCurrentDice._2 + "  \n"
            case GameStatus.NEW_FIELD =>
                bufferedMessage = bufferedMessage + "  Your new Field is " + MainComponentServer.getCurrentFieldName(controller.getBoard()) + ".  \n"
            case GameStatus.ALREADY_BOUGHT =>
                bufferedMessage = bufferedMessage + "  You already own this street.  \n"
            case GameStatus.BOUGHT_BY_OTHER =>
                // RentPay 2
                bufferedMessage = bufferedMessage + "  Field already bought by " +
                    MainComponentServer.getOwnersName(controller.getBoard(),
                        MainComponentServer.getCurrentFieldName(controller.getBoard())) +
                    "\nYou must pay " + MainComponentServer.getCurrentFieldRent(controller.getBoard()) + "€ rent."
            case GameStatus.PASSED_GO =>
                bufferedMessage = bufferedMessage + "  Earned 200€ for passing Go.  \n"
            case GameStatus.NOTHING =>
        }
    }

    def processInput(input: String): Unit = ???
}
