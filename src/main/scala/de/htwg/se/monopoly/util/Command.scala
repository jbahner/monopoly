package de.htwg.se.monopoly.util

trait Command {

    def doStep() : Unit
    def undoStep() : Unit
    def redoStep() : Unit

}
