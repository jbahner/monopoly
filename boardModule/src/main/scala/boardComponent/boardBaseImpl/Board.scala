package boardComponent.boardBaseImpl

import boardComponent.IBoard
import play.api.libs.json.{JsObject, Json}
import playerModule.fieldComponent.{Field, IBuyable}
import playerModule.playerComponent.IPlayer
import playerModule.util.PlayerIterator

import scala.xml.Elem

case class Board(fields: List[Field], currentPlayer: IPlayer, playerIt: PlayerIterator) extends IBoard {

    override def nextPlayerTurn(): IBoard = copy(getFields, nextPlayer(), getPlayerIt)

    override def nextPlayer(): IPlayer = playerIt.next()

    override def replacePlayer(player: IPlayer, newPlayer: IPlayer): IBoard = {
        playerIt.replace(player, newPlayer)
        copy(getFields, currentPlayer = if (currentPlayer == player) newPlayer else currentPlayer, getPlayerIt)
    }

    def getFields: List[Field] = fields

    def copy(fields: List[Field], currentPlayer: IPlayer, playerIt: PlayerIterator): IBoard = Board(fields, currentPlayer, playerIt)

    def getPlayerIt: PlayerIterator = playerIt

    def replaceField(field: IBuyable, newField: IBuyable): IBoard = {
        val newPlayers = playerIt.list.map(p => {
            p.copy(fieldIt = p.getFieldIt.replace(field, newField),
                currentField = if (p.getCurrentField == field) newField else p.getCurrentField,
                bought = p.getBought.map(f => if (f == field) newField else f))
        })
        copy(fields = fields.updated(fields.indexOf(field), newField), currentPlayer = newPlayers.find(p => p.getName == currentPlayer.getName).get,
            playerIt = new PlayerIterator(newPlayers.toArray, playerIt.currentIdx))
    }

    def getPlayerit: PlayerIterator = playerIt

    def getCurrentPlayer: IPlayer = currentPlayer

    def toXml(): Elem = {
        <board>
            <fields>
                {for {
                field <- fields
            } yield field.toXml()}
            </fields>
            <current-player>
                {currentPlayer.getName}
            </current-player>{playerIt.toXml()}
        </board>
    }

    override def toJson(): JsObject = {
        Json.obj(
            "num-fields" -> fields.size,
            "fields" -> fields.map(field => field.toJson()),
            "current-player" -> currentPlayer.getName,
            "player-iterator" -> playerIt.toJson()
        )
    }
}
