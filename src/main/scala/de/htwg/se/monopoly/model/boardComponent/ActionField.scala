package de.htwg.se.monopoly.model.boardComponent

import de.htwg.se.monopoly.controller.GameStatus._
import de.htwg.se.monopoly.model.playerComponent.Player

case class ActionField(name: String) extends Field(name) {
    override def action(player: Player): GameStatus = {
        name match {
            case _ => NOTHING
        }
    }
}
