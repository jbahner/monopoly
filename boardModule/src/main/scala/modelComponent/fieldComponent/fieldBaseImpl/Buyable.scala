package modelComponent.fieldComponent.fieldBaseImpl

import modelComponent.gamestate.GameStatus
import modelComponent.gamestate.GameStatus.GameStatus
import play.api.libs.json.{JsObject, Json}
import modelComponent.fieldComponent.IBuyable
import modelComponent.playerComponent.IPlayer

import scala.xml.Elem

abstract class Buyable(name: String, price: Int, isBought: Boolean = false) extends IBuyable {

    override def action(player: IPlayer): String = {
        if (isBought) {
            if (player.getBought.contains(this)) GameStatus.map(GameStatus.ALREADY_BOUGHT)
            else GameStatus.map(GameStatus.BOUGHT_BY_OTHER)
        }
        else GameStatus.map(GameStatus.CAN_BUY)
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
}
