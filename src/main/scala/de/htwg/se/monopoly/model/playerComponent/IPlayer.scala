package de.htwg.se.monopoly.model.playerComponent

import de.htwg.se.monopoly.model.boardComponent.{Field, IBuyable}
import de.htwg.se.monopoly.model.playerComponent.playerBaseImpl.Player
import de.htwg.se.monopoly.util.FieldIterator
import play.api.libs.json.JsValue

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

    def copy(name: String = IPlayer.this.name, money: Int = IPlayer.this.money, currentField: Field = IPlayer.this.getCurrentField, bought: Set[IBuyable] = IPlayer.this.getBought, fieldIt: FieldIterator = IPlayer.this.fieldIt): Player

    def getCurrentField: Field

    override def getFieldIt: FieldIterator

    def toXml(): Elem

    def nameToXml(): Elem
}
