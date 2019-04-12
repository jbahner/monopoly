package de.htwg.se.monopoly.model.boardComponent

import de.htwg.se.monopoly.model.playerComponent.Player

case class ActionField(name: String) extends Field(name) {
    override def action(player: Player): Unit = ???
}
