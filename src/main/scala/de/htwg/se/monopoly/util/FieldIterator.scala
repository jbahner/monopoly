package de.htwg.se.monopoly.util

import de.htwg.se.monopoly.model.boardComponent.Field
import play.api.libs.json.{JsObject, Json}

import scala.xml.Elem

case class FieldIterator(fields: List[Field]) extends Iterator[Field] {
    var current = 0

    override def next(): Field = {
        current += 1
        if (!hasNext)
            throw new NoSuchElementException
        fields(current)
    }

    override def hasNext: Boolean = {
        if (current >= fields.size)
            current = 0
        fields.isDefinedAt(current)
    }

    def getCurrent: Field = {
        fields(current)
    }

    def walkOverFields(steps: Int): Boolean = {
        val walkedOverFields = new Array[Field](steps)
        for (i <- 0 until steps) {
            walkedOverFields(i) = this.next()
        }
        walkedOverFields.exists(_.getName.equals("Go"))
    }

    def replace(field: Field, newField: Field) = this.copy(fields = fields.updated(fields.indexOf(field), newField))

    def toXml(): Elem = {
        <field-iterator>
            <current-idx>{current}</current-idx>
        </field-iterator>
    }

    def toJson(): JsObject = {
        Json.obj(
            "current-idx" -> current
        )
    }
}
