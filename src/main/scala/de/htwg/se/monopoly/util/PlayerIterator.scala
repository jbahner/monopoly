package de.htwg.se.monopoly.util

import de.htwg.se.monopoly.model.playerComponent.Player

class PlayerIterator(var players: Array[Player]) extends Iterator[Player] {
    private var current = 0

    override def hasNext: Boolean = {
        if (current >= players.size)
            current = 0
        players(current) != null
    }

    override def next(): Player = {
        if (!hasNext)
            throw new NoSuchElementException
        val tmp = current
        current += 1
        if(current >= players.size) current = 0
        players(tmp)
    }

    def get(): Player = players(current)

    def replace(player:Player) = players(current) = player
}