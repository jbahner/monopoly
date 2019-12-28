package de.htwg.se.monopoly.model.playerComponent.playerBaseImpl

import de.htwg.se.monopoly.model.boardComponent.Field
import de.htwg.se.monopoly.model.boardComponent.boardBaseImpl.{Buyable, Street}
import de.htwg.se.monopoly.model.playerComponent.IPlayer
import de.htwg.se.monopoly.util.FieldIterator
import play.api.libs.json.{JsNumber, JsValue, Json}

case class  Player(name: String, money: Int, currentField: Field, bought: Set[Buyable], fieldIt: FieldIterator) extends IPlayer {

    override def walk(steps: Int, overGo: Boolean = true): (IPlayer, Boolean) = {
        var passedGo = false
        var field: Field = currentField
        for (_ <- 0 until steps) {
            field = fieldIt.next()
            if (field.getName.equals("Go")) passedGo = true
        }
        // 0 steps only for move cards, just to be sure
        if(steps == 0 && field.getName.equals("Go"))
            passedGo = true
        if(overGo && passedGo)
            (this.copy(money = money + (if (passedGo) 200 else 0), currentField = field), passedGo)
        else
            (this.copy(currentField = field), false)
    }

    override def toString: String = name + ", money: " + money

    override def getDetails: String = "%-10s%d\n%-10s%s\n%-10s%s".format("money:", money, "bought:", listStreets, "field:", currentField.getName)

    override def listStreets: String = {
        val sb = new StringBuilder()
        bought.foreach(field => {
            sb.append("\n%-10s%s".format("", field.getName))
            field match {
                case street: Street =>
                    sb.append("\thouses: %d".format(street.numHouses))
                case _ =>
            }
        })
        sb.toString()
    }

    override def equals(that: IPlayer): Boolean = {
        name.equals(that.getName)
    }

    override def getJSON: JsValue = {
        Json.obj(
            "name" -> name,
            "money" -> JsNumber(money),
            "current_field" -> currentField.getName,
            "bought_fields" -> bought.filter(b => b.isInstanceOf[Street]).map(s => s.asInstanceOf[Street].getJSON).toList)
    }

    override def getBought: Set[Buyable] = bought

    override def getMoney: Int = money

    override def getName: String = name

    def copy(name: String, money: Int, currentField: Field, bought: Set[Buyable], fieldIt: FieldIterator): Player =
        new Player(name,money,currentField,bought,fieldIt)

    override def getCurrentField: Field = currentField

    override def getFieldIt: FieldIterator = fieldIt

    override def adjustMoney(amount: Int): IPlayer = this.copy(money = money + amount)

    override def stepsUntil(fieldName: String): Int = fieldIt.stepsUntil(fieldName)
}
