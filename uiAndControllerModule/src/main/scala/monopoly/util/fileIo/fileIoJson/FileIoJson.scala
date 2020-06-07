package monopoly.util.fileIo.fileIoJson

import modelComponent.boardComponent.IBoard
import modelComponent.boardComponent.boardBaseImpl.Board
import modelComponent.gamestate.GameStatus
import modelComponent.gamestate.GameStatus.BuildStatus.BuildStatus
import modelComponent.gamestate.GameStatus.{BuildStatus, GameStatus}
import monopoly.controller.IController
import monopoly.util.fileIo.IFileIo
import play.api.libs.json.{JsObject, Json}
import modelComponent.fieldComponent.fieldBaseImpl.{ActionField, Street}
import modelComponent.fieldComponent.{Field, IBuyable}
import modelComponent.playerComponent.playerBaseImpl.Player
import modelComponent.util.{FieldIterator, PlayerIterator}

import scala.io.Source
import scala.util.{Failure, Success, Try}

class FileIoJson extends IFileIo {
    override def load(path: String): (IBoard, GameStatus, BuildStatus) = {
        val source = Try(Source.fromFile(path.replace(".xml", ".json"))) match {
            case Success(value) => value
            case Failure(_) => Source.fromFile(getClass.getClassLoader.getResource("save-game.json").getPath)
        }
        val json = Json.parse(source.getLines.mkString).as[JsObject]
        source.close
        (
          Board.fromJson(json),
          GameStatus.fromJson(json),
          BuildStatus.fromJson(json)
        )
    }

    def save(controller: IController): Unit = {
        import java.io._
        val pw = new PrintWriter(new File("save-game.json"))
        pw.write(Json.prettyPrint(controller.toJson()))
        pw.close()
    }

}
