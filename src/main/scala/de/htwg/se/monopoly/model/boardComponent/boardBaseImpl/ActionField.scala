package de.htwg.se.monopoly.model.boardComponent.boardBaseImpl

import de.htwg.se.monopoly.controller.GameStatus.{GameStatus, NOTHING}
import de.htwg.se.monopoly.model.boardComponent.IActionField
import de.htwg.se.monopoly.model.playerComponent.IPlayer

case class ActionField(name: String) extends IActionField {

    override def action(player: IPlayer): GameStatus = {
        name match {
            case _ => NOTHING
        }
    }

    override def getName: String = name

    def copy(name: String): IActionField = ActionField(name)

    def getPrice: Int = ???
}
