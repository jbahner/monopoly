package modelComponent.fieldComponent

import modelComponent.playerComponent.IPlayer

import scala.xml.Elem

trait IActionField extends Field {

    def action(player: IPlayer): String

    def getName: String

    def copy(name: String = IActionField.this.getName): IActionField

    def toXml(): Elem

    def nameToXml(): Elem

}
