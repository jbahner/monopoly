package modelComponent.persistence.relational

import modelComponent.boardComponent.IBoard
import modelComponent.persistence.IDaoBoard

import scala.concurrent.duration.Duration
import scala.concurrent.Await

object RelationalAdapter extends IDaoBoard {

    private val boardMapping = BoardMapping

    override def saveBoard(board: IBoard): Boolean = {
        boardMapping.saveBoard(board)
    }

    override def loadBoard(): IBoard = {
        Await.result(boardMapping.loadBoard(), Duration.Inf)
    }
}
