package modelComponent.persistence

import modelComponent.boardComponent.IBoard

trait IDaoBoard {

    def saveBoard(board:IBoard): Boolean

    def loadBoard(): IBoard

}
