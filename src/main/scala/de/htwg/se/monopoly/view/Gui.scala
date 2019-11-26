package de.htwg.se.monopoly.view

import java.awt.Color
import java.util

import com.sun.source.tree.BinaryTree
import de.htwg.se.monopoly.controller.{Controller, GameStatus, UpdateGui, UpdateInfo}
import de.htwg.se.monopoly.model.boardComponent.{Buyable, Street}
import javax.swing.BorderFactory
import javax.swing.border.Border

import scala.collection.{SortedSet, mutable}
import scala.swing.Swing.LineBorder
import scala.swing._
import scala.swing.event._
import scala.util.Try

class Gui(controller: Controller) extends Frame {

    //TODO
    // - show how much houses are on which street
    // - wrap text output in own labels per line to make mupltiple lines in gui passible

    val windowDimension = new Dimension(700, 300)
    val menuBarDimension = new Dimension(1000, 30)

    listenTo(controller)
    title = "Monopoly+"

    var infoPanel = new GridPanel(3, 1)
    var buttonPanel = new FlowPanel
    var textPanel = new FlowPanel

    minimumSize = windowDimension

    visible = true
    //pack()

    reactions += {
        case event: UpdateInfo => redraw()
        case event: UpdateGui => redraw()

    }

    def redraw(): Unit = {
        contents = new BorderPanel {
            add(createMenuBar(), BorderPanel.Position.North)
            add(generateTextPanel(), BorderPanel.Position.Center)
            add(redrawButtons(), BorderPanel.Position.South)
            add(generateLeftPanel(), BorderPanel.Position.West)
            add(generateBuildButtons(), BorderPanel.Position.East)
            //repaint
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

    def generateTextPanel(): GridPanel = {
        val currentMsg = getCurrentGameMessage().split("\n")

        new GridPanel(currentMsg.size, 1) {
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
            contents += new Label(controller.getCurrentField.getName)
        }
    }

    def getCurrentGameMessage(): String = {
        controller.controllerState match {
            case GameStatus.START_OF_TURN => "  It is your start of the turn!\nRoll the dice.  "
            case GameStatus.CAN_BUILD =>
                controller.buildStatus match {
                    case GameStatus.BuildStatus.DEFAULT => "  Your rolled a " + controller.currentDice + ".\nYour new Field is " + controller.getCurrentField.getName + ".  "
                    case GameStatus.BuildStatus.BUILT => "  Sucessfully build house.  "
                }

            case _ => "------ ERROR ------"
        }
    }

    override def closeOperation(): Unit = {
        sys.exit(0)
    }

}
