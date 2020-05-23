package playerModule.fieldComponent

import gamestate.GameStatus.GameStatus
import playerModule.playerComponent.IPlayer

import scala.xml.Elem

trait IBuyable extends Field {

    def action(player: IPlayer): GameStatus

    def getPrice: Int

    def toXml(): Elem

    def nameToXml(): Elem

}
