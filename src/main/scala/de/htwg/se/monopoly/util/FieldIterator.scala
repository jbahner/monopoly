package de.htwg.se.monopoly.util

import de.htwg.se.monopoly.model.boardComponent.Field

class FieldIterator(fields: List[Field]) extends Iterator[Field] {
    private var current = 0

    override def next(): Field = {
        current += 1
        if (!hasNext)
            throw new NoSuchElementException
        fields(current)
    }

    override def hasNext: Boolean = {
        if (current >= fields.size)
            current = 0
        fields(current) != null
    }
}
