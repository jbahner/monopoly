package model.util

import play.api.libs.json.{JsObject, Json}
import model.playerComponent.IPlayer

import scala.xml.Elem

case class PlayerIterator(players: Array[IPlayer], startIdx: Int = 0) extends Iterator[IPlayer] {
    private var current = startIdx

    override def next(): IPlayer = {
        current += 1
        if (!hasNext)
            throw new NoSuchElementException
        players(current)
    }

    override def hasNext: Boolean = {
        if (current >= players.length)
            current = 0
        players.isDefinedAt(current)
    }

    def replace(player: IPlayer, newPlayer: IPlayer): Unit = {
        var index = 0
        for (i <- players.indices) {
            if (players(i).getName.equals(newPlayer.getName)) index = i
        }
        players(index) = newPlayer
    }

    def list: List[IPlayer] = players.toList

    def currentIdx = current

    def copy = new PlayerIterator(players = players.clone(), startIdx = current)

    def toXml(): Elem = {
        <player-iterator>
            <players>
                {for {player <- players} yield player.toXml()}
            </players>
            <start-idx>
                {startIdx}
            </start-idx>
        </player-iterator>
    }

    def toJson(): JsObject = {
        Json.obj(
            "num-players" -> players.length,
            "players" -> players.map(p => p.toJson()),
            "start-idx" -> current
        )
    }
}