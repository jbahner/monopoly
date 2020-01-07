package de.htwg.se.monopoly.util

import de.htwg.se.monopoly.model.boardComponent.Card
import de.htwg.se.monopoly.model.boardComponent.CardType.CardType

trait ICardStack {
    val cards: List[Card]

    def draw(kind: CardType): Card

    def shuffle(kind: CardType)

}
