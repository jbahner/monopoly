package de.htwg.se.monopoly.model.boardComponent.boardBaseImpl

import de.htwg.se.monopoly.model.boardComponent.Card
import de.htwg.se.monopoly.model.boardComponent.CardType.CardType
import de.htwg.se.monopoly.util.ICardStack

import scala.util.Random

class CardStack(override val cards: List[Card]) extends ICardStack {
    private var stack = Random.shuffle(cards)
    override def draw(kind: CardType): Card = {
        val card = stack.last
        stack = stack.init
        card
    }

    override def shuffle(kind: CardType): Unit = {
        stack = Random.shuffle(cards)
    }
}
