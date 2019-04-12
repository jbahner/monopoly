package de.htwg.se.monopoly.model.playerComponent

import de.htwg.se.monopoly.model.boardComponent.Field
import de.htwg.se.monopoly.util.FieldIterator

case class Player(name : String, money:Int, fieldIt: FieldIterator) {

  def walk(steps: Int): Player = {
    var overGo = false
    for (_ <- 1 to steps) {
      fieldIt.next()
      if(fieldIt.get().getName.equals("Go")) overGo = true
    }
    this.copy(money = money + (if(overGo) 200 else 0))
  }

  def getCurrentField : Field = fieldIt.get()

  override def toString: String = name + ", money: " + money
}
