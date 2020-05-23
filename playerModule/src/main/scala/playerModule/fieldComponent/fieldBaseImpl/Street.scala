package playerModule.fieldComponent.fieldBaseImpl

import playerModule.fieldComponent.IStreet
import gamestate.GameStatus
import gamestate.GameStatus.GameStatus
import play.api.libs.json.{JsObject, Json}
import playerModule.playerComponent.IPlayer

import scala.xml.Elem

case class Street(name: String, price: Int, rentCosts: Array[Int], houseCost: Int, numHouses: Int = 0, isBought: Boolean = false) extends IStreet {

    def buyHouses(amount: Int): IStreet = {
        this.copy(numHouses = numHouses + amount)
    }

    def copy(name: String, price: Int, rentCosts: Array[Int], houseCost: Int, numHouses: Int, isBought: Boolean): IStreet =
        Street(name, price, rentCosts, houseCost, numHouses, isBought)

    def setBought(): IStreet = this.copy(isBought = true)

    def getJSON: JsObject = Json.obj("name" -> name, "houses" -> numHouses, "houseCost" -> houseCost)

    def getName: String = name

    def getPrice: Int = price

    def getRentCosts: Array[Int] = rentCosts

    def getHouseCost: Int = houseCost

    def getNumHouses: Int = numHouses

    def getIsBought: Boolean = isBought

    override def action(player: IPlayer): GameStatus = {
        if (isBought) {
            if (player.getBought.contains(this)) GameStatus.ALREADY_BOUGHT
            else GameStatus.BOUGHT_BY_OTHER
        }
        else GameStatus.CAN_BUY
    }

    override def toXml(): Elem = {
        <street>
            <name>
                {name}
            </name>
            <price>
                {price}
            </price>
            <rent-cost>
                {rentCosts.map(cost => cost)}
            </rent-cost>
            <house-cost>
                {houseCost}
            </house-cost>
            <num-houses>
                {numHouses}
            </num-houses>
            <is-bought>
                {isBought}
            </is-bought>
        </street>
    }

    override def toJson(): JsObject = {
        Json.obj("field" -> Json.obj(
            "type" -> "street",
            "name" -> name,
            "price" -> price,
            "rent-cost" -> rentCosts,
            "houseCost" -> houseCost,
            "numHouses" -> numHouses,
            "is-bought" -> isBought
        ))
    }

    override def nameToXml(): Elem = {
        <street>
            <name>
                {name}
            </name>
        </street>
    }

    override def nameToJson(): JsObject = {
        Json.obj(
            "field" -> Json.obj(
                "type" -> "street",
                "name" -> name
            )
        )
    }
}
