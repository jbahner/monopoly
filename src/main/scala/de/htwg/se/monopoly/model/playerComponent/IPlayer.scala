package de.htwg.se.monopoly.model.playerComponent

import de.htwg.se.monopoly.model.boardComponent.Field
import de.htwg.se.monopoly.model.boardComponent.boardBaseImpl.Buyable
import de.htwg.se.monopoly.model.playerComponent.playerBaseImpl.Player
import de.htwg.se.monopoly.util.FieldIterator
import play.api.libs.json.JsValue

trait IPlayer {
    this: Player =>

    def walk(steps: Int, overGo: Boolean = true): (IPlayer, Boolean)

    def adjustMoney(amount: Int) : IPlayer

    def stepsUntil(fieldName: String) : Int

    def toString: String

    def getDetails: String

    def listStreets: String

    def equals(that: IPlayer): Boolean

    def getJSON: JsValue

    def getBought: Set[Buyable]

    def getMoney: Int

    def getName: String

    def copy(name: String = IPlayer.this.name, money: Int = IPlayer.this.money, currentField: Field = IPlayer.this.getCurrentField, bought: Set[Buyable] = IPlayer.this.bought, fieldIt: FieldIterator = IPlayer.this.fieldIt): Player

    def getCurrentField: Field

    override def getFieldIt: FieldIterator
}
