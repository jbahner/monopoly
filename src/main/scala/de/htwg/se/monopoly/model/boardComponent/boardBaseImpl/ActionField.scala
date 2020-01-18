package de.htwg.se.monopoly.model.boardComponent.boardBaseImpl

import com.google.inject.Inject
import de.htwg.se.monopoly.controller.GameStatus.{GameStatus, NOTHING}
import de.htwg.se.monopoly.model.boardComponent.IActionField
import de.htwg.se.monopoly.model.playerComponent.IPlayer

import scala.xml.Elem

case class ActionField @Inject()(name: String) extends IActionField {

    override def action(player: IPlayer): GameStatus = {
        name match {
            case _ => NOTHING
        }
    }

    override def getName: String = name

    def copy(name: String): IActionField = ActionField(name)

    def getPrice: Int = ???

    override def toXml(): Elem = {
        <action-field>
            <name>
                {name}
            </name>
        </action-field>

    }

    override def nameToXml(): Elem = toXml()
}
