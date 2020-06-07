package modelComponent.fieldComponent

import modelComponent.gamestate.GameStatus.GameStatus
import play.api.libs.json.JsObject
import modelComponent.playerComponent.IPlayer

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
