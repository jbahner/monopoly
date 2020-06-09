package monopoly.util

import modelComponent.boardComponent.IBoard

trait Command {

    def doStep(): IBoard

    def undoStep(): IBoard

    def redoStep(): IBoard

}
