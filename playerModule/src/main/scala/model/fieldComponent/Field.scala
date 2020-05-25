package model.fieldComponent

import model.gamestate.GameStatus.GameStatus
import play.api.libs.json.JsObject
import model.playerComponent.IPlayer

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
