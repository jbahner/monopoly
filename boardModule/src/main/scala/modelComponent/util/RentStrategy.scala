package modelComponent.util

import modelComponent.boardComponent.IBoard
import modelComponent.fieldComponent.{IBuilding, IBuyable, IStreet}

trait RentStrategy {
    def executeStrategy(board: IBoard, buyable: IBuyable): Int
}

// This Rent Strategy is called 3 times.
// 2 times to display the amount that needs to be payed
// and 1 time to actually pay the amount called from the controller
case class StreetRentStrategy(board: IBoard, street: IStreet) extends RentStrategy {
    override def executeStrategy(board: IBoard, buyable: IBuyable): Int = {
        if (street.getNumHouses == 0 &&
            GeneralUtil.hasWholeGroup(
              board.getBuyer(street.asInstanceOf[IBuyable]).get,
              street.getName))

            street.getRentCosts(street.getNumHouses) * 2
        else
            street.getRentCosts(street.getNumHouses)
    }
}

case class BuildingRentStrategy(board: IBoard, building: IBuilding) extends RentStrategy {
    override def executeStrategy(board: IBoard, buyable: IBuyable): Int = {
        if (GeneralUtil.hasWholeGroup(board.getBuyer(building.asInstanceOf[IBuyable]).get, building.getName))
            board.currentDice * 10
        else
            board.currentDice * 4
    }
}

object RentContext {
    lazy val rentStrategy: RentStrategy = {
        board.getCurrentField() match {
            case street: IStreet => StreetRentStrategy(board, street)
            case building: IBuilding => BuildingRentStrategy(board, building)
        }

    }
    var board: IBoard = _
}