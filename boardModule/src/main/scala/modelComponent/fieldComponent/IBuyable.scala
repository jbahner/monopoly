package modelComponent.fieldComponent

import modelComponent.gamestate.GameStatus.GameStatus
import modelComponent.playerComponent.IPlayer

import scala.xml.Elem

trait IBuyable extends Field {

    val isBought: Boolean

    def action(player: IPlayer): String

    def getPrice: Int

    def toXml(): Elem

    def nameToXml(): Elem

}
