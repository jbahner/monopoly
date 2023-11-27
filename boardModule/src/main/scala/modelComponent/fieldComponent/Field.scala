package modelComponent.fieldComponent

import modelComponent.playerComponent.IPlayer
import play.api.libs.json.JsObject

import scala.xml.Elem

trait Field {

    def action(player: IPlayer): String

    def getName: String

    def getPrice: Int

    def toXml(): Elem

    def toJson(): JsObject

    def nameToXml(): Elem

    def nameToJson(): JsObject
}
