package model.fieldComponent

import model.gamestate.GameStatus.GameStatus
import model.playerComponent.IPlayer

import scala.xml.Elem

trait IBuyable extends Field {

    val isBought: Boolean

    def action(player: IPlayer): GameStatus

    def getPrice: Int

    def toXml(): Elem

    def nameToXml(): Elem

}
