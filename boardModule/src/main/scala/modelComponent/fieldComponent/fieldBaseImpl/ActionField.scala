package modelComponent.fieldComponent.fieldBaseImpl

import com.google.inject.Inject
import modelComponent.fieldComponent.IActionField
import modelComponent.gamestate.GameStatus
import modelComponent.playerComponent.IPlayer
import play.api.libs.json.{JsObject, Json}

import scala.xml.Elem

case class ActionField @Inject()(name: String) extends IActionField {

    override def action(player: IPlayer): String = {
        name match {
            case _ => GameStatus.map(GameStatus.NOTHING)
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

object ActionField {
    def fromJson(json: JsObject): ActionField = {
        ActionField(
            name = (json \ "name").get.as[String]
        )
    }
}
