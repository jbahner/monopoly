package de.htwg.se.monopoly.util

import de.htwg.se.monopoly.model.boardComponent.Field

class FieldIterator(fields: List[Field]) extends Iterator[Field] {
    private var current = 0

    override def hasNext: Boolean = {
        if (current >= fields.size)
            current = 0
        fields(current) != null
    }

    override def next(): Field = {
        if (!hasNext)
            throw new NoSuchElementException
        val tmp = current
        current += 1
        fields(tmp)
    }

    def get(): Field = if(current >= fields.size) fields.head else fields(current)

}
