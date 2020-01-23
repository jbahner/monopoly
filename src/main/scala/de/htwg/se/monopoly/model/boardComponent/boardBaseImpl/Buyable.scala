package de.htwg.se.monopoly.model.boardComponent.boardBaseImpl

import de.htwg.se.monopoly.controller.GameStatus._
import de.htwg.se.monopoly.model.boardComponent.IBuyable
import de.htwg.se.monopoly.model.playerComponent.IPlayer
import play.api.libs.json.{JsObject, Json, Writes}

import scala.xml.Elem

abstract class Buyable(name: String, price: Int, isBought: Boolean = false) extends IBuyable {

    override def action(player: IPlayer): GameStatus = {
        if (isBought) {
            if (player.getBought.contains(this)) ALREADY_BOUGHT
            else BOUGHT_BY_OTHER
        }
        else CAN_BUY
    }

    override def getPrice: Int = price

    override def toXml(): Elem = {
        <buyable>
            <name>{name}</name>
            <price>{price}</price>
            <is-bought>{isBought}</is-bought>
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
