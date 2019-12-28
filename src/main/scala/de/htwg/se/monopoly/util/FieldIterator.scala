package de.htwg.se.monopoly.util

import de.htwg.se.monopoly.model.boardComponent.Field

case class FieldIterator(fields: List[Field]) extends Iterator[Field] {
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

    def stepsUntil(fieldName: String): Int = {
        if(!fields.map(f => f.getName).contains(fieldName))
            return -1
        val fieldIdx = fields.map(f => f.getName).indexOf(fieldName)
        if(fieldIdx < current)
            fields.size - current + fieldIdx
        else
            fieldIdx - current
    }

    def replace(field: Field, newField: Field) = this.copy(fields =fields.updated(fields.indexOf(field), newField))
}
