package monopoly.controller.gamestate

import play.api.libs.json.JsObject

object GameStatus extends Enumeration {
    type GameStatus = Value
    val START_OF_TURN, ROLLED, NEW_FIELD, NEXT_PLAYER, BOUGHT_BY_OTHER, CAN_BUY, ALREADY_BOUGHT, BOUGHT, NOTHING, PASSED_GO, MISSING_MONEY, CAN_BUILD, DONE = Value
    val map: Map[GameStatus, String] = Map[GameStatus, String](
        START_OF_TURN -> "START_OF_TURN",
        ROLLED -> "ROLLED",
        NEW_FIELD -> "NEW_FIELD",
        NEXT_PLAYER -> "NEXT_PLAYER",
        BOUGHT_BY_OTHER -> "BOUGHT_BY_OTHER",
        CAN_BUY -> "CAN_BUY",
        ALREADY_BOUGHT -> "ALREADY_BOUGHT",
        BOUGHT -> "BOUGHT",
        NOTHING -> "NOTHING",
        PASSED_GO -> "PASSED_GO",
        MISSING_MONEY -> "MISSING_MONEY",
        CAN_BUILD -> "CAN_BUILD",
        DONE -> "DONE"
    )

    val revMap: Map[String, GameStatus] = Map[String, GameStatus](

        "START_OF_TURN" -> START_OF_TURN,
        "ROLLED" -> ROLLED,
        "NEW_FIELD" -> NEW_FIELD,
        "NEXT_PLAYER" -> NEXT_PLAYER,
        "BOUGHT_BY_OTHER" -> BOUGHT_BY_OTHER,
        "CAN_BUY" -> CAN_BUY,
        "ALREADY_BOUGHT" -> ALREADY_BOUGHT,
        "BOUGHT" -> BOUGHT,
        "NOTHING" -> NOTHING,
        "PASSED_GO" -> PASSED_GO,
        "MISSING_MONEY" -> MISSING_MONEY,
        "CAN_BUILD" -> CAN_BUILD,
        "DONE" -> DONE
    )

    def fromJson(json: JsObject): GameStatus = revMap((json \ "controller" \ "game-status").get.as[String])

    def message(gameStatus: GameStatus): String = map(gameStatus)

    object BuildStatus extends Enumeration {
        type BuildStatus = Value
        val DEFAULT, DONE, INVALID_ARGS, NOT_OWN, MISSING_MONEY, BUILT, TOO_MANY_HOUSES = Value

        val revMap: Map[String, BuildStatus] = Map[String, BuildStatus](
            "DEFAULT" -> DEFAULT,
            "DONE" -> DONE,
            "INVALID_ARGS" -> INVALID_ARGS,
            "NOT_OWN" -> NOT_OWN,
            "MISSING_MONEY" -> MISSING_MONEY,
            "BUILT" -> BUILT,
            "TOO_MANY_HOUSES" -> TOO_MANY_HOUSES
        )

        def fromJson(json: JsObject) : BuildStatus = revMap((json \ "controller" \ "build-status").get.as[String])
    }
}
