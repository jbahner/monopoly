package model.fieldComponent

import model.gamestate.GameStatus.GameStatus
import model.playerComponent.IPlayer

import scala.xml.Elem

trait IActionField extends Field {

    def action(player: IPlayer): GameStatus

    def getName: String

    def copy(name: String = IActionField.this.getName): IActionField

    def toXml(): Elem

    def nameToXml(): Elem

}
