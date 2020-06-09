package modelComponent.playerComponent

import play.api.libs.json.{JsObject, JsValue}
import modelComponent.fieldComponent.{Field, IBuyable}
import modelComponent.playerComponent.playerBaseImpl.Player
import modelComponent.util.FieldIterator

import scala.xml.Elem

trait IPlayer {
    this: Player =>

    def walk(steps: Int): (IPlayer, Boolean)

    def toString: String

    def getDetails: String

    def listStreets: String

    def equals(that: IPlayer): Boolean

    def getJSON: JsValue

    def getBought: Set[IBuyable]

    def getMoney: Int

    def getName: String

    def copy(name: String = IPlayer.this.name, money: Int = IPlayer.this.money, currentField: Field = IPlayer.this.getCurrentField(), bought: Set[IBuyable] = IPlayer.this.getBought, fieldIt: FieldIterator = IPlayer.this.fieldIt): Player

    def getCurrentField(): Field

    override def getFieldIt: FieldIterator

    def toXml(): Elem

    def toJson(): JsObject
}
