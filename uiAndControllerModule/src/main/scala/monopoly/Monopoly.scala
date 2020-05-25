package monopoly

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.stream.ActorMaterializer
import com.google.inject.{Guice, Injector}
import monopoly.controller.IController
import monopoly.controller.controllerBaseImpl.UpdateInfo
import monopoly.view.{Gui, IUi, Tui}

import scala.io.StdIn
import scala.io.StdIn.readLine

object Monopoly {

    // Akka Inits
    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val injector: Injector = Guice.createInjector(new MonopolyModule)
    val controller: IController = injector.getInstance(classOf[IController])
    controller.setUp
    val tui: IUi = new Tui(controller)
    val gui: IUi = new Gui(controller)



    def main(args: Array[String]): Unit = {

        val route =
            path("") {
                get {
                    complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, controller.getCurrentGameMessage))
                }
            }
        val bindingFuture = Http().bindAndHandle(route, "localhost", 8081)

        controller.publish(new UpdateInfo)

        var input = readLine()
        while (input != "q") {
            input = readLine()
            tui.processInput(input)
        }


        // Server Shutdown
        println("Server shutting down")
        System.exit(0)
    }


}
