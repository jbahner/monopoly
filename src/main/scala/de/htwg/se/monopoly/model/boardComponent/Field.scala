package de.htwg.se.monopoly.model.boardComponent

import de.htwg.se.monopoly.controller.GameStatus.GameStatus
import de.htwg.se.monopoly.model.playerComponent.IPlayer
import play.api.libs.json.JsObject

import scala.xml.Elem

trait Field {

    def action(player: IPlayer): GameStatus

    def getName: String

    def getPrice: Int

    def toXml(): Elem

    def toJson(): JsObject

    def nameToXml(): Elem

    def nameToJson(): JsObject
}
