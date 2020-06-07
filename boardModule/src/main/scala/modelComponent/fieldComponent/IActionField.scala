package modelComponent.fieldComponent

import modelComponent.gamestate.GameStatus.GameStatus
import modelComponent.playerComponent.IPlayer

import scala.xml.Elem

trait IActionField extends Field {

    def action(player: IPlayer): GameStatus

    def getName: String

    def copy(name: String = IActionField.this.getName): IActionField

    def toXml(): Elem

    def nameToXml(): Elem

}
