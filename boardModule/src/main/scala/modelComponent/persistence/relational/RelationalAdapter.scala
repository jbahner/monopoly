package modelComponent.persistence.relational

import modelComponent.boardComponent.IBoard
import modelComponent.persistence.IDaoBoard

object RelationalAdapter extends IDaoBoard {

    private val boardMapping = BoardMapping

    override def saveBoard(board: IBoard): Boolean = {
        boardMapping.saveBoard(board)
    }

    override def loadBoard(): IBoard = {
        boardMapping.loadBoard() match {
            case Some(board) => board
            case None => println("Loading Board Failed")
                throw new RuntimeException("Loading Board Failed")
        }
    }
}
