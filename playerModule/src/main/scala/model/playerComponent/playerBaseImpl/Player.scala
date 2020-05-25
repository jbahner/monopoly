package model.playerComponent.playerBaseImpl

import play.api.libs.json.{JsNumber, JsObject, JsValue, Json}
import model.fieldComponent.{Field, IBuyable, IStreet}
import model.playerComponent.IPlayer
import model.util.FieldIterator

import scala.xml.Elem

case class Player(name: String, money: Int, currentField: Field, bought: Set[IBuyable], fieldIt: FieldIterator) extends IPlayer {

    override def walk(steps: Int): (IPlayer, Boolean) = {
        val passedGo = fieldIt.walkOverFields(steps)
        (this.copy(money = money + (if (passedGo) 200 else 0), currentField = fieldIt.getCurrent), passedGo)
    }

    def copy(name: String, money: Int, currentField: Field, bought: Set[IBuyable], fieldIt: FieldIterator): Player =
        Player(name, money, currentField, bought, fieldIt)

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

    override def getCurrentField: Field = currentField

    override def getFieldIt: FieldIterator = fieldIt

    override def toXml(): Elem = {
        <player>
            <name>
                {name}
            </name>
            <money>
                {money}
            </money>
            <current-field>
                {currentField.getName}
            </current-field>
            <bought>
                {for {
                boughtField <- bought
            } yield boughtField.nameToXml()}
            </bought>{fieldIt.toXml()}
        </player>
    }

    override def toJson(): JsObject = {
        Json.obj(
            "player" -> Json.obj(
                "name" -> name,
                "money" -> money,
                "current-field" -> currentField.getName,
                "num-bought" -> bought.size,
                "bought" -> bought.map(f => f.nameToJson()),
                "field-iterator" -> fieldIt.toJson()
            )
        )
    }
}

object Player {
    def fromJson(json : JsObject, fields : List[Field]) : Player = {
        var bought = Set[IBuyable]()
        for (j <- 0 until (json \ "num-bought").get.as[Int]) {
            val s = ((json \ "bought") (j) \ "field")
            bought = bought + fields.find(field => field.getName.equals((s \ "name").get.as[String])).get.asInstanceOf[IBuyable]
        }
        Player(
            name = (json \ "name").get.as[String],
            money = (json \ "money").get.as[Int],
            currentField = fields.find(field => field.getName.equals((json \ "current-field").get.as[String])).get,
            bought = bought,
            fieldIt = FieldIterator(fields)
        )
    }
}