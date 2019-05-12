package de.htwg.se.monopoly.model.playerComponent

import de.htwg.se.monopoly.model.boardComponent.{Buyable, Field}
import de.htwg.se.monopoly.util.FieldIterator

case class Player(name: String, money: Int, currentField: Field, bought : List[Buyable], fieldIt: FieldIterator) {

    def walk(steps: Int): Player = {
        var overGo = false
        var field: Field = null
        for (_ <- 0 until steps) {
            field = fieldIt.next()
            if (field.getName.equals("Go")) overGo = true
        }
        this.copy(money = money + (if (overGo) 200 else 0), currentField = field)
    }

    override def toString: String = name + ", money: " + money

    def getDetails: String = "%-10s%d\n%-10s%s\n%-10s%s".format("money:", money, "bought:",listStreets,"field:", currentField.getName)

    def listStreets:String = {
        val sb = new StringBuilder()
        bought.foreach(field => sb.append("\n%-10s%s".format("", field.getName)))
        sb.toString()
    }
}
