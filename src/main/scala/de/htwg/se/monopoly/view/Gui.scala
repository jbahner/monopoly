package de.htwg.se.monopoly.view

import java.awt.Color
import java.util

import de.htwg.se.monopoly.controller.controllerBaseImpl.{UpdateGui, UpdateInfo}
import de.htwg.se.monopoly.controller.{GameStatus, IController}
import de.htwg.se.monopoly.model.boardComponent.Field
import de.htwg.se.monopoly.model.boardComponent.boardBaseImpl.{Building, Street}
import javax.swing.{BorderFactory, ImageIcon}

import scala.swing._
import scala.swing.event._

class Gui(controller: IController) extends Frame with IUi {

    //TODO
    // - show how much houses are on which street
    // - wrap text output in own labels per line to make mupltiple lines in gui passible
    // - make auto setup an extra command -- set up game with custom player names and -count

    val windowDimension = new Dimension(800, 400)
    val menuBarDimension = new Dimension(1000, 30)

    listenTo(controller)
    title = "Monopoly+"

    minimumSize = windowDimension

    visible = true
    //pack()

    reactions += {
        case event: UpdateInfo => redraw()
        case event: UpdateGui => redraw()
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
                    print("Not implemented yet")
                })
                contents += new MenuItem(Action("Save") {
                    print("Not implemented yet")
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

    def redrawButtons(): FlowPanel = {
        // Customizing the buttons
        //TODO check which buttons need to be added

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
            case GameStatus.CAN_BUILD => new GridPanel(controller.getCurrentPlayer.get.getBought.size, 1) {
                if (controller.getCurrentPlayer.isDefined)
                    controller.getCurrentPlayer.get.getBought.toSeq.sortBy(_.getName)
                        .foreach(bought => contents += generateBuildButton(bought.getName))
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
                tooltip = controller.getFieldByName(streetName).get.asInstanceOf[Street].houseCost + "€"
            }
            contents += new Label(" -- " + controller.getFieldByName(streetName).get.asInstanceOf[Street].numHouses.toString)
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
            val curField: Field = controller.getCurrentField


            contents += new GridPanel(7, 1) {
                contents += new Label("Current Field") {
                    font = (new Font(font.getFontName, font.getStyle, 20))
                    background = Color.CYAN
                    opaque = true
                }


                contents += new Label(curField.getName)


                curField match {
                    case street: Street =>
                        contents += new Label(
                            if (curField.asInstanceOf[Street].isBought) "Bought by " + controller.getBuyer(curField.asInstanceOf[Street]).get.getName
                            else "Not owned")
                        contents += new Label("Current Rent: " + curField.asInstanceOf[Street].rentCosts(curField.asInstanceOf[Street].numHouses))
                        contents += new Label("Houses: " + curField.asInstanceOf[Street].numHouses.toString)
                    case building: Building =>
                        contents += new Label(
                            if (curField.asInstanceOf[Building].isBought) "Bought by " + controller.getBuyer(curField.asInstanceOf[Building]).get.getName
                            else "Not owned")
                    case _ =>
                }
            }


            if (curField.getName.equals("Go")) {
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

    def generateLeftPanel(): GridPanel = {

        new GridPanel(10, 1) {
            contents += new Label("")
            contents += new Label("  " + controller.getCurrentPlayer.get.getName + "    ")
            contents += new Label(controller.getCurrentPlayer.get.getMoney + " €")
            contents += new Label("")
        }
    }

    def getCurrentGameMessage(): String = {
        controller.getControllerState match {
            case GameStatus.START_OF_TURN => "  " + controller.getCurrentPlayer.get.getName + "'s turn.\nIt is your start of the turn!\nRoll the dice.  "
            case GameStatus.CAN_BUILD =>
                controller.getBuildStatus match {
                    case GameStatus.BuildStatus.DEFAULT => "  Your rolled a " + controller.getCurrentDice + ".\nYour new Field is " + controller.getCurrentField.getName + ".  "
                    case GameStatus.BuildStatus.BUILT => "  Sucessfully build house.  "
                    case _ => "  Un catched BuildStatus  "
                }

            case _ => "------ ERROR ------"
        }
    }

    override def closeOperation(): Unit = {
        sys.exit(0)
    }

    def processInput(input: String): Unit = {
        throw new UnsupportedOperationException
    }
}
