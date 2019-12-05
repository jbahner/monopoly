package de.htwg.se.monopoly.model.boardComponent

import de.htwg.se.monopoly.controller.Controller
import de.htwg.se.monopoly.util.GeneralUtil

trait RentStrategy {
    def executeStrategy(buyable: Buyable): Int
}

case class StreetRentStrategy(controller: Controller, street: Street) extends RentStrategy {
    override def executeStrategy(buyable: Buyable): Int = {
        if (street.numHouses == 0 && GeneralUtil.hasWholeGroup(controller.getBuyer(street.asInstanceOf[Buyable]).get, street.getName))
            street.rentCosts(street.numHouses) * 2
        else
            street.rentCosts(street.numHouses)
    }
}

case class BuildingRentStrategy(controller: Controller, building: Building) extends RentStrategy {
    override def executeStrategy(buyable: Buyable): Int = {
        if (GeneralUtil.hasWholeGroup(controller.getBuyer(building.asInstanceOf[Buyable]).get, building.getName))
            (controller.currentDice._1 + controller.currentDice._2) * 10
        else
            (controller.currentDice._1 + controller.currentDice._2) * 4
    }
}

object RentContext {
    var controller: Controller = _

    lazy val rentStrategy: RentStrategy = {
        controller.getCurrentField match {
            case street: Street => StreetRentStrategy(controller, street)
            case building: Building => BuildingRentStrategy(controller, building)
        }

    }
}