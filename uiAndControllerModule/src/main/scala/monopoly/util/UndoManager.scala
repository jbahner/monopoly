package monopoly.util

class UndoManager {
    private var undoStack: List[Command] = Nil
    private var redoStack: List[Command] = Nil

    def doStep(command: Command): String = {
        undoStack = command :: undoStack
        command.doStep()
    }

    def undoStep(): String = {
        undoStack match {
            case head :: stack =>
                val board = head.undoStep()
                undoStack = stack
                redoStack = head :: redoStack
                board
        }
    }

    def redoStep(): String = {
        redoStack match {
            case head :: stack =>
                val board = head.redoStep()
                redoStack = stack
                undoStack = head :: undoStack
                board
        }
    }

    def emptyStack(): Unit = {
        undoStack = Nil
        redoStack = Nil
    }
}