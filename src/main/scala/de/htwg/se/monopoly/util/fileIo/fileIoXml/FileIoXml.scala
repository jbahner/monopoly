package de.htwg.se.monopoly.util.fileIo.fileIoXml

import de.htwg.se.monopoly.controller.IController
import de.htwg.se.monopoly.util.fileIo.IFileIo

import scala.xml.PrettyPrinter

class FileIoXml extends IFileIo {
    def load: IController = ???

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
