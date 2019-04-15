package de.htwg.se.monopoly.util

import de.htwg.se.monopoly.controller.GameStatus.GameStatus

trait Observer {
    def update(gameStatus: GameStatus): Unit
}

class Observable {
    var subscribers: Vector[Observer] = Vector()

    def add(s: Observer): Unit = subscribers = subscribers :+ s

    def remove(s: Observer): Unit = subscribers = subscribers.filterNot(o => o == s)

    def notifyObservers(gameStatus: GameStatus): Unit = subscribers.foreach(o => o.update(gameStatus))
}
