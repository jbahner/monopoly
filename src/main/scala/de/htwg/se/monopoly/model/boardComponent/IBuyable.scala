package de.htwg.se.monopoly.model.boardComponent

import de.htwg.se.monopoly.controller.GameStatus.GameStatus
import de.htwg.se.monopoly.model.playerComponent.IPlayer

import scala.xml.Elem

trait IBuyable extends Field {

    def action(player: IPlayer): GameStatus

    def getPrice: Int

    def toXml(): Elem

    def nameToXml(): Elem

}
