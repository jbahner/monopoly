package de.htwg.se.monopoly.model.playerComponent

import de.htwg.se.monopoly.model.boardComponent.Field
import de.htwg.se.monopoly.util.FieldIterator

case class Player(name: String, money: Int, currentField: Field, fieldIt: FieldIterator) {

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
}
