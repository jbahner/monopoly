package de.htwg.se.monopoly

import com.google.inject.AbstractModule
import de.htwg.se.monopoly.controller.IController
import de.htwg.se.monopoly.controller.controllerBaseImpl.Controller
import de.htwg.se.monopoly.model.boardComponent.{IActionField, IBoard, IBuilding, IBuyable, IStreet}
import de.htwg.se.monopoly.model.boardComponent.boardBaseImpl.{ActionField, Board, Building, Buyable, Street}
import de.htwg.se.monopoly.model.playerComponent.IPlayer
import de.htwg.se.monopoly.model.playerComponent.playerBaseImpl.Player
import net.codingwell.scalaguice.ScalaModule

class MonopolyModule extends AbstractModule with ScalaModule {


    def configure(): Unit = {
        bind[IController].to[Controller]

        bind[IPlayer].to[Player]
        bind[IBoard].to[Board]

        bind[IActionField].to[ActionField]
        bind[IBuilding].to[Building]
        bind[IBuyable].to[Buyable]
        bind[IStreet].to[Street]
    }

}