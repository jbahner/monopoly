package de.htwg.se.monopoly.util

import de.htwg.se.monopoly.model.playerComponent.Player

class PlayerIterator(players: Array[Player], var current: Int = 0) extends Iterator[Player] {

    override def next(): Player = {
        if (!hasNext)
            throw new NoSuchElementException
        val tmp = current
        current += 1
        if (current >= players.length) current = 0
        players(tmp)
    }

    override def hasNext: Boolean = {
        if (current >= players.length)
            current = 0
        players(current) != null
    }

    def replace(player: Player, newPlayer: Player) = players(players.indexOf(player)) = newPlayer
}