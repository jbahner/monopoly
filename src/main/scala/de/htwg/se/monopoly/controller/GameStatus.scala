package de.htwg.se.monopoly.controller

object GameStatus extends Enumeration {
    type GameStatus = Value
    val START_OF_TURN, NEW_FIELD, NEXT_PLAYER, BOUGHT_BY_OTHER, CAN_BUY, ALREADY_BOUGHT, BOUGHT, NOTHING, PASSED_GO, MISSING_MONEY, CAN_BUILD = Value
    val map: Map[GameStatus, String] = Map[GameStatus, String](
        NEW_FIELD -> "New Field: \n",
        NEXT_PLAYER -> "Next Player: ",
        CAN_BUY -> "You can buy: ",
        BOUGHT_BY_OTHER -> "Belongs to someone else",
        ALREADY_BOUGHT -> "Already owning the field",
    )

    def message(gameStatus: GameStatus): String = map(gameStatus)
    object BuildStatus extends Enumeration {
        type BuildStatus = Value
        val DEFAULT, DONE, INVALID_ARGS, NOT_OWN, MISSING_MONEY, BUILT, TOO_MANY_HOUSES = Value
    }
}
