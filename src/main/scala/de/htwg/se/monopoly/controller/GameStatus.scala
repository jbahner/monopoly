package de.htwg.se.monopoly.controller

object GameStatus extends Enumeration {
    type GameStatus = Value
    val NEWFIELD, NEXTPLAYER = Value
    val map : Map[GameStatus, String] = Map[GameStatus, String] (
        NEWFIELD -> "New Field: \n",
        NEXTPLAYER -> "Next Player: "
    )

    def message(gameStatus: GameStatus) : String = map(gameStatus)

}
