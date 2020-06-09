package monopoly.util

import modelComponent.boardComponent.IBoard

trait Command {

    def doStep(): String

    def undoStep(): String

    def redoStep(): String

}
