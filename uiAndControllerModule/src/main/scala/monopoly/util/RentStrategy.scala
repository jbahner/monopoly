package monopoly.util

import monopoly.controller.IController
import model.fieldComponent.{IBuilding, IBuyable, IStreet}

trait RentStrategy {
    def executeStrategy(buyable: IBuyable): Int
}

// This Rent Strategy is called 3 times.
// 2 times to display the amount that needs to be payed
// and 1 time to actually pay the amount called from the controller
case class StreetRentStrategy(controller: IController, street: IStreet) extends RentStrategy {
    override def executeStrategy(buyable: IBuyable): Int = {
        if (street.getNumHouses == 0 &&
            GeneralUtil.hasWholeGroup(
              controller.getBuyer(street.asInstanceOf[IBuyable]).get,
              street.getName))

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
    lazy val rentStrategy: RentStrategy = {
        controller.getCurrentField match {
            case street: IStreet => StreetRentStrategy(controller, street)
            case building: IBuilding => BuildingRentStrategy(controller, building)
        }

    }
    var controller: IController = _
}