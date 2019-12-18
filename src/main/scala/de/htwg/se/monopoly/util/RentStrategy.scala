package de.htwg.se.monopoly.util

import de.htwg.se.monopoly.controller.IController
import de.htwg.se.monopoly.model.boardComponent.boardBaseImpl.{Building, Buyable, Street}

trait RentStrategy {
    def executeStrategy(buyable: Buyable): Int
}

case class StreetRentStrategy(controller: IController, street: Street) extends RentStrategy {
    override def executeStrategy(buyable: Buyable): Int = {
        if (street.numHouses == 0 && GeneralUtil.hasWholeGroup(controller.getBuyer(street.asInstanceOf[Buyable]).get, street.getName))
            street.rentCosts(street.numHouses) * 2
        else
            street.rentCosts(street.numHouses)
    }
}

case class BuildingRentStrategy(controller: IController, building: Building) extends RentStrategy {
    override def executeStrategy(buyable: Buyable): Int = {
        if (GeneralUtil.hasWholeGroup(controller.getBuyer(building.asInstanceOf[Buyable]).get, building.getName))
            (controller.getCurrentDice._1 + controller.getCurrentDice._2) * 10
        else
            (controller.getCurrentDice._1 + controller.getCurrentDice._2) * 4
    }
}

object RentContext {
    var controller: IController = _

    lazy val rentStrategy: RentStrategy = {
        controller.getCurrentField match {
            case street: Street => StreetRentStrategy(controller, street)
            case building: Building => BuildingRentStrategy(controller, building)
        }

    }
}