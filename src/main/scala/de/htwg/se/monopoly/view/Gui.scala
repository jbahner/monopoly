package de.htwg.se.monopoly.view

import java.awt.Color
import java.util

import de.htwg.se.monopoly.controller.{Controller, GameStatus, UpdateGui, UpdateInfo}
import de.htwg.se.monopoly.model.boardComponent.{Building, Field, Street}
import javax.imageio.ImageIO
import javax.swing.{BorderFactory, ImageIcon, JLabel}

import scala.reflect.io.File
import scala.swing._
import scala.swing.event._

class Gui(controller: Controller) extends Frame {

    //TODO
    // - show how much houses are on which street
    // - wrap text output in own labels per line to make mupltiple lines in gui passible
    // - make auto setup an extra command -- set up game with custom player names and -count

    val windowDimension = new Dimension(700, 350)
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
        }
    }

    def createMenuBar(): MenuBar = {
        new MenuBar {
            contents += new Menu("File") {
                contents += new MenuItem(Action("New Game") {
                    controller.setUp()
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
            }
        }
    }

    def redrawButtons(): FlowPanel = {
        // Customizing the buttons
        //TODO check which buttons need to be added

        val buttonList = new util.ArrayList[Button]()

        controller.controllerState match {
            case GameStatus.START_OF_TURN =>
                buttonList.add(new Button("Roll Dice") {
                    reactions += {
                        case _: ButtonClicked => controller.rollDice()
                    }
                })
            case GameStatus.CAN_BUILD =>
                buttonList.add(new Button("End Turn") {
                    reactions += {
                        case _: ButtonClicked => controller.nextPlayer()
                    }
                })
            case _ =>
        }

        new FlowPanel {
            buttonList.forEach(button => contents += button)
        }
    }

    def generateBuildButtons(): GridPanel = {
        controller.controllerState match {
            case GameStatus.CAN_BUILD => new GridPanel(controller.getCurrentPlayer().get.bought.size, 1) {
                if (controller.getCurrentPlayer().isDefined)
                    controller.getCurrentPlayer().get.bought.toSeq.sortBy(_.getName)
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
        new GridPanel(7, 1) {

            contents += new Label("Current Field") {
                font = (new Font(font.getFontName, font.getStyle, 20))
                background = Color.CYAN
                opaque = true
            }

            val curField: Field = controller.getCurrentField
            contents += new Label(curField.getName)


            curField match {
                case street: Street =>
                    contents += new Label(
                        if (curField.asInstanceOf[Street].isBought) "Bought by " + controller.getBuyer(curField.asInstanceOf[Street]).get.name
                        else "Not owned")
                    contents += new Label("Houses: " + curField.asInstanceOf[Street].numHouses.toString)
                    contents += new Label("House price: " + curField.asInstanceOf[Street].houseCost)
                    contents += new Label("Current Rent: " + curField.asInstanceOf[Street].getRent())
                case building: Building =>
                    contents += new Label(
                        if (curField.asInstanceOf[Building].isBought) "Bought by " + controller.getBuyer(curField.asInstanceOf[Building]).get.name
                        else "Not owned")
                case _ =>
            }
            if (curField.getName.equals("Go")) {
                contents += new Label() {
                    icon = new ImageIcon("D:\\HTWG\\5.Semester\\monopoly\\src\\main\\scala\\de\\htwg\\se\\monopoly\\view\\textures\\go_field.png")
                    maximumSize = new Dimension(100,100)
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
            contents += new Label("  " + controller.getCurrentPlayer().get.name + "    ")
            contents += new Label(controller.getCurrentPlayer().get.money + " €")
            contents += new Label("")
        }
    }

    def getCurrentGameMessage(): String = {
        controller.controllerState match {
            case GameStatus.START_OF_TURN => "  " + controller.getCurrentPlayer().get.name + "'s turn.\nIt is your start of the turn!\nRoll the dice.  "
            case GameStatus.CAN_BUILD =>
                controller.buildStatus match {
                    case GameStatus.BuildStatus.DEFAULT => "  Your rolled a " + controller.currentDice + ".\nYour new Field is " + controller.getCurrentField.getName + ".  "
                    case GameStatus.BuildStatus.BUILT => "  Sucessfully build house.  "
                    case _ => "  Un catched BuildStatus  "
                }

            case _ => "------ ERROR ------"
        }
    }

    override def closeOperation(): Unit = {
        sys.exit(0)
    }

}
