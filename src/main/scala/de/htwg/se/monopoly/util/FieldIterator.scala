package de.htwg.se.monopoly.util

import de.htwg.se.monopoly.model.boardComponent.Field

import scala.xml.Elem

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

    def replace(field: Field, newField: Field) = this.copy(fields = fields.updated(fields.indexOf(field), newField))

    def toXml(): Elem = {
        <field-iterator>
            <fields>
                {for { field <- fields} yield field.nameToXml()}
            </fields>
            <current-idx>{current}</current-idx>
        </field-iterator>
    }
}
