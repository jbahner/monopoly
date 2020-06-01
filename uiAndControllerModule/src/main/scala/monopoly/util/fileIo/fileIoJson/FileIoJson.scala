package monopoly.util.fileIo.fileIoJson

import model.gamestate.GameStatus
import model.gamestate.GameStatus.BuildStatus.BuildStatus
import model.gamestate.GameStatus.{BuildStatus, GameStatus}
import monopoly.MainComponentServer
import monopoly.controller.IController
import monopoly.util.fileIo.IFileIo
import play.api.libs.json.{JsObject, Json}

import scala.io.Source
import scala.util.{Failure, Success, Try}

class FileIoJson extends IFileIo {
    override def load(path: String): (String, GameStatus, BuildStatus) = {
        val source = Try(Source.fromFile(path.replace(".xml", ".json"))) match {
            case Success(value) => value
            case Failure(_) => Source.fromFile(getClass.getClassLoader.getResource("save-game.json").getPath)
        }
        val json = Json.parse(source.getLines.mkString).as[JsObject]
        source.close
        (
            MainComponentServer.requestParsingBoardFromJson(json.toString()),
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
