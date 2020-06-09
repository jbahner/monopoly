package monopoly.util

trait Command {

    def doStep(): String

    def undoStep(): String

    def redoStep(): String

}
