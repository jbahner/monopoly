package monopoly.view

trait IUi {

    def processInput(input: String): Unit

    def closeOperation(): Unit

}
