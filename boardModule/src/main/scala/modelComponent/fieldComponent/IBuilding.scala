package modelComponent.fieldComponent

import scala.xml.Elem

trait IBuilding extends IBuyable {

    def setBought(): IBuilding

    def getName: String

    def copy(name: String = IBuilding.this.getName, price: Int = IBuilding.this.getPrice, isBought: Boolean = IBuilding.this.getIsBought): IBuilding

    def getIsBought: Boolean

    def toXml(): Elem

    def nameToXml(): Elem

}
