package de.htwg.se.monopoly.model.boardComponent.boardBaseImpl

import de.htwg.se.monopoly.controller.GameStatus.{GameStatus, NOTHING}
import de.htwg.se.monopoly.model.boardComponent.Field
import de.htwg.se.monopoly.model.playerComponent.IPlayer

case class ActionField(name: String) extends Field(name) {
    override def action(player: IPlayer): GameStatus = {
        name match {
            case _ => NOTHING
        }
    }
}
