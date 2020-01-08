package de.htwg.se.monopoly.model.playerComponent.playerBaseImpl

import de.htwg.se.monopoly.model.boardComponent.{Field, IBuyable, IStreet}
import de.htwg.se.monopoly.model.playerComponent.IPlayer
import de.htwg.se.monopoly.util.FieldIterator
import play.api.libs.json.{JsNumber, JsValue, Json}

case class  Player(name: String, money: Int, currentField: Field, bought: Set[IBuyable], fieldIt: FieldIterator) extends IPlayer {

    override def walk(steps: Int): (IPlayer, Boolean) = {
        var passedGo = false
        var field: Field = null
        for (_ <- 0 until steps) {
            field = fieldIt.next()
            if (field.getName.equals("Go")) passedGo = true
        }
        (this.copy(money = money + (if (passedGo) 200 else 0), currentField = field), passedGo)
    }

    override def toString: String = name + ", money: " + money

    override def getDetails: String = "%-10s%d\n%-10s%s\n%-10s%s".format("money:", money, "bought:", listStreets, "field:", currentField.getName)

    override def listStreets: String = {
        val sb = new StringBuilder()
        bought.foreach(field => {
            sb.append("\n%-10s%s".format("", field.getName))
            field match {
                case street: IStreet =>
                    sb.append("\thouses: %d".format(street.getNumHouses))
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
            "bought_fields" -> bought.filter(b => b.isInstanceOf[IStreet]).map(s => s.asInstanceOf[IStreet].getJSON).toList)
    }

    override def getBought: Set[IBuyable] = bought

    override def getMoney: Int = money

    override def getName: String = name

    def copy(name: String, money: Int, currentField: Field, bought: Set[IBuyable], fieldIt: FieldIterator): Player =
        Player(name,money,currentField,bought,fieldIt)

    override def getCurrentField: Field = currentField

    override def getFieldIt: FieldIterator = fieldIt
}
