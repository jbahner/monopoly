package de.htwg.se.monopoly.util

import de.htwg.se.monopoly.controller.IController
import de.htwg.se.monopoly.model.boardComponent.{IBuilding, IBuyable, IStreet}

trait RentStrategy {
    def executeStrategy(buyable: IBuyable): Int
}

case class StreetRentStrategy(controller: IController, street: IStreet) extends RentStrategy {
    override def executeStrategy(buyable: IBuyable): Int = {
        if (street.getNumHouses == 0 && GeneralUtil.hasWholeGroup(controller.getBuyer(street.asInstanceOf[IBuyable]).get, street.getName))
            street.getRentCosts(street.getNumHouses) * 2
        else
            street.getRentCosts(street.getNumHouses)
    }
}

case class BuildingRentStrategy(controller: IController, building: IBuilding) extends RentStrategy {
    override def executeStrategy(buyable: IBuyable): Int = {
        if (GeneralUtil.hasWholeGroup(controller.getBuyer(building.asInstanceOf[IBuyable]).get, building.getName))
            (controller.getCurrentDice._1 + controller.getCurrentDice._2) * 10
        else
            (controller.getCurrentDice._1 + controller.getCurrentDice._2) * 4
    }
}

object RentContext {
    var controller: IController = _

    lazy val rentStrategy: RentStrategy = {
        controller.getCurrentField match {
            case street: IStreet => StreetRentStrategy(controller, street)
            case building: IBuilding => BuildingRentStrategy(controller, building)
        }

    }
}