package monopoly.util.fileIo.fileIoJson

import monopoly.controller.IController
import monopoly.controller.gamestate.GameStatus
import monopoly.controller.gamestate.GameStatus.BuildStatus.BuildStatus
import monopoly.controller.gamestate.GameStatus.{BuildStatus, GameStatus}
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

        val tmp1 = (json \ "controller" \ "board")
        val tmp2 = tmp1.get
        val tmp3 = tmp2.toString

        (tmp3,
            GameStatus.fromJson(json),
            BuildStatus.fromJson(json))
    }

    def save(controller: IController): Unit = {
        import java.io._
        val pw = new PrintWriter(new File("save-game.json"))
        pw.write(Json.prettyPrint(controller.toJson()))
        pw.close()
    }

}
