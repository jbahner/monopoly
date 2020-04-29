package de.htwg.se.monopoly

import com.google.inject.AbstractModule
import de.htwg.se.monopoly.controller.IController
import de.htwg.se.monopoly.controller.controllerBaseImpl.Controller
import de.htwg.se.monopoly.model.boardComponent.boardBaseImpl._
import de.htwg.se.monopoly.model.playerComponent.IPlayer
import de.htwg.se.monopoly.model.playerComponent.playerBaseImpl.Player
import de.htwg.se.monopoly.util.FieldIterator
import de.htwg.se.monopoly.util.fileIo.IFileIo
import de.htwg.se.monopoly.util.fileIo.fileIoXml.FileIoXml
import net.codingwell.scalaguice.ScalaModule

class MonopolyModule extends AbstractModule with ScalaModule {


    def configure(): Unit = {
        bind[IController].to[Controller]
        //bind[IController].to[MockController]

        bind[IPlayer].toInstance(Player("", 0, ActionField(""), Set(), FieldIterator(List())))
        //bind[IBoard].toInstance(Board(List(), null, PlayerIterator(Array())))
        bind[IFileIo].to[FileIoXml]
        //bind[IFileIo].to[FileIoJson]
    }

}