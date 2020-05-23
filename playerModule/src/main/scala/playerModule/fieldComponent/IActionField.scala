package playerModule.fieldComponent

import gamestate.GameStatus.GameStatus
import playerModule.playerComponent.IPlayer

import scala.xml.Elem

trait IActionField extends Field {

    def action(player: IPlayer): GameStatus

    def getName: String

    def copy(name: String = IActionField.this.getName): IActionField

    def toXml(): Elem

    def nameToXml(): Elem

}
