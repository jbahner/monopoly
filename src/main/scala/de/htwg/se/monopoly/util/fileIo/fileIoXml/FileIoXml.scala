package de.htwg.se.monopoly.util.fileIo.fileIoXml

import com.google.inject.Guice
import de.htwg.se.monopoly.MonopolyModule
import de.htwg.se.monopoly.controller.{GameStatus, IController}
import de.htwg.se.monopoly.model.boardComponent.boardBaseImpl.Board
import de.htwg.se.monopoly.util.fileIo.IFileIo

import scala.xml.PrettyPrinter

class FileIoXml extends IFileIo {
    def load: IController = {
        val controller = Guice.createInjector(new MonopolyModule).getInstance(classOf[IController])
        val file = scala.xml.XML.loadFile("save-game.xml")
        controller.controllerState = GameStatus.revMap((file \\ "controller" \ "game-status").text)
        controller.buildStatus = GameStatus.BuildStatus.revMap((file \\ "controller" \ "build-status").text)
        //controller.setBoard(Board())



        controller
    }

    def saveString(controller: IController): Unit = {
        import java.io._
        val pw = new PrintWriter(new File("save-game.xml"))
        val prettyPrinter = new PrettyPrinter(120, 4)
        val xml = prettyPrinter.format(controller.toXml)

        pw.write(xml)
        pw.close()
    }


    def save(controller: IController): Unit = saveString(controller)

}
