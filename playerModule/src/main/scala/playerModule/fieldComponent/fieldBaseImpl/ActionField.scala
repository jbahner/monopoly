package playerModule.fieldComponent.fieldBaseImpl

import com.google.inject.Inject
import gamestate.GameStatus
import gamestate.GameStatus.GameStatus
import play.api.libs.json.{JsObject, Json}
import playerModule.fieldComponent.IActionField
import playerModule.playerComponent.IPlayer

import scala.xml.Elem

case class ActionField @Inject()(name: String) extends IActionField {

    override def action(player: IPlayer): GameStatus = {
        name match {
            case _ => GameStatus.NOTHING
        }
    }

    override def getName: String = name

    def copy(name: String): IActionField = ActionField(name)

    def getPrice: Int = ???

    override def nameToXml(): Elem = toXml()

    override def toXml(): Elem = {
        <action-field>
            <name>
                {name}
            </name>
        </action-field>
    }

    override def nameToJson(): JsObject = toJson()

    override def toJson(): JsObject = {
        Json.obj(
            "field" -> Json.obj(
                "type" -> "action-field",
                "name" -> name
            )
        )
    }
}
