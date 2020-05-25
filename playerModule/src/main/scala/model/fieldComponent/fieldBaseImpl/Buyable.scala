package model.fieldComponent.fieldBaseImpl

import model.gamestate.GameStatus
import model.gamestate.GameStatus.GameStatus
import play.api.libs.json.{JsObject, Json}
import model.fieldComponent.IBuyable
import model.playerComponent.IPlayer

import scala.xml.Elem

abstract class Buyable(name: String, price: Int, isBought: Boolean = false) extends IBuyable {

    override def action(player: IPlayer): GameStatus = {
        if (isBought) {
            if (player.getBought.contains(this)) GameStatus.ALREADY_BOUGHT
            else GameStatus.BOUGHT_BY_OTHER
        }
        else GameStatus.CAN_BUY
    }

    override def getPrice: Int = price

    override def toXml(): Elem = {
        <buyable>
            <name>
                {name}
            </name>
            <price>
                {price}
            </price>
            <is-bought>
                {isBought}
            </is-bought>
        </buyable>
    }

    override def toJson(): JsObject = {
        Json.obj(
            "field" -> Json.obj(
                "type" -> "buyable",
                "name" -> name,
                "price" -> price,
                "isBought" -> isBought
            )
        )
    }
}
