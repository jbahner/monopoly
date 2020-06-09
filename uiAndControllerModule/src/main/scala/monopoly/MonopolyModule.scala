package monopoly

;

import com.google.inject.AbstractModule
import modelComponent.fieldComponent.fieldBaseImpl.ActionField
import modelComponent.playerComponent.IPlayer
import modelComponent.playerComponent.playerBaseImpl.Player
import modelComponent.util.FieldIterator
import monopoly.controller.IController
import monopoly.controller.controllerBaseImpl.Controller
import monopoly.util.fileIo.IFileIo
import monopoly.util.fileIo.fileIoJson.FileIoJson
import net.codingwell.scalaguice.ScalaModule

class MonopolyModule extends AbstractModule with ScalaModule {


    def configure(): Unit = {
        bind[IController].to[Controller]
        //bind[IController].to[MockController]

        bind[IPlayer].toInstance(Player("", 0, ActionField(""), Set(), FieldIterator(List())))
        //bind[IBoard].toInstance(Board(List(), null, PlayerIterator(Array())))
        //        bind[IFileIo].to[FileIoXml]
        bind[IFileIo].to[FileIoJson]
    }

}