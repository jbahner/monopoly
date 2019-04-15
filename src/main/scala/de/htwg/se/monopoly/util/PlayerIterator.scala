package de.htwg.se.monopoly.util

import de.htwg.se.monopoly.model.playerComponent.Player

class PlayerIterator(players: Array[Player]) extends Iterator[Player] {
    private var current = 0

    override def next(): Player = {
        current += 1
        if (!hasNext)
            throw new NoSuchElementException
        players(current)
    }

    override def hasNext: Boolean = {
        if (current >= players.length)
            current = 0
        players(current) != null
    }

    def replace(player: Player, newPlayer: Player) = players(players.indexOf(player)) = newPlayer
}