package monopoly

import com.google.inject.{Guice, Injector}
import monopoly.controller.IController
import monopoly.controller.controllerBaseImpl.UpdateInfo
import monopoly.view.{Gui, IUi, Tui}

import scala.io.StdIn.readLine

object Monopoly {

    val injector: Injector = Guice.createInjector(new MonopolyModule)

    val controller: IController = injector.getInstance(classOf[IController])
    controller.setUp

    val tui: IUi = new Tui(controller)
    val gui: IUi = new Gui(controller)

    def main(args: Array[String]): Unit = {

        controller.publish(new UpdateInfo)
        while (true) {
            val input = readLine()
            tui.processInput(input)
        }
    }
}
