package boardComponent.boardBaseImpl

import boardComponent.IBoard
import model.fieldComponent.fieldBaseImpl.{ActionField, Street}
import model.fieldComponent.{Field, IBuyable, IStreet}
import model.gamestate.GameStatus._
import model.playerComponent.IPlayer
import model.playerComponent.playerBaseImpl.Player
import model.util.PlayerIterator
import play.api.libs.json.{JsObject, Json}

import scala.xml.Elem

case class Board(fields: List[Field], currentPlayer: IPlayer, playerIt: PlayerIterator) extends IBoard {

    override def nextPlayerTurn(): IBoard = copy(getFields, nextPlayer(), getPlayerIt)

    override def nextPlayer(): IPlayer = playerIt.next()

    override def replacePlayer(player: IPlayer, newPlayer: IPlayer): IBoard = {
        playerIt.replace(player, newPlayer)
        copy(getFields, currentPlayer = if (currentPlayer == player) newPlayer else currentPlayer, getPlayerIt)
    }

    def getFields: List[Field] = fields

    def copy(fields: List[Field], currentPlayer: IPlayer, playerIt: PlayerIterator): IBoard = Board(fields, currentPlayer, playerIt)

    def getPlayerIt: PlayerIterator = playerIt

    def replaceField(field: IBuyable, newField: IBuyable): IBoard = {
        val newPlayers = playerIt.list.map(p => {
            p.copy(fieldIt = p.getFieldIt.replace(field, newField),
                currentField = if (p.getCurrentField == field) newField else p.getCurrentField,
                bought = p.getBought.map(f => if (f == field) newField else f))
        })
        copy(fields = fields.updated(fields.indexOf(field), newField), currentPlayer = newPlayers.find(p => p.getName == currentPlayer.getName).get,
            playerIt = new PlayerIterator(newPlayers.toArray, playerIt.currentIdx))
    }

    def getCurrentPlayer: IPlayer = currentPlayer

    def getPlayerByName(name: String): IPlayer = {
        var player = List[IPlayer]()
        playerIt.foreach(playerItPlayer =>
            if (playerItPlayer.getName == name) {
                player = player :+ playerItPlayer.copy()
            }
        )
        player.head
    }

    def givePlayerMoney(player: IPlayer, money: Int): IBoard = {
        this.replacePlayer(player, player.copy(money = player.getMoney + money))
    }

    def buildHouses(streetName: String, amount: Int): IBoard = {
        var buildStatus = BuildStatus.DEFAULT
        val field = getFieldByName(streetName)
        if (field.isEmpty || !field.get.isInstanceOf[IStreet]) {
            buildStatus = BuildStatus.INVALID_ARGS
        }
        val street = field.get.asInstanceOf[IStreet]
        val buyer = getBuyer(street)
        if (buyer.isEmpty || !buyer.get.equals(getCurrentPlayer))
            buildStatus = BuildStatus.NOT_OWN
        else if (street.getNumHouses + amount > 5)
            buildStatus = BuildStatus.TOO_MANY_HOUSES
        else if (currentPlayer.getMoney < street.getHouseCost * amount)
            buildStatus = BuildStatus.MISSING_MONEY
        this
    }

    def getBuyer(buyable: IBuyable): Option[IPlayer] = {
        val players = getPlayerIt.list
        val player = players.find(p => p.getBought.contains(buyable))
        player
    }

    def getFieldByName(name: String): Option[Field] = {
        getFields.find(field => field.getName.equals(name))
    }

    def toXml(): Elem = {
        <board>
            <fields>
                {for {
                field <- fields
            } yield field.toXml()}
            </fields>
            <current-player>
                {currentPlayer.getName}
            </current-player>{playerIt.toXml()}
        </board>
    }

    override def toJson(): JsObject = {
        Json.obj(
            "num-fields" -> fields.size,
            "fields" -> fields.map(field => field.toJson()),
            "current-player" -> currentPlayer.getName,
            "player-iterator" -> playerIt.toJson()
        )
    }
}

object Board {
    def fromJson(json: JsObject): Board = {
        var fields = List[Field]()
        for (i <- 0 until (json \ "controller" \ "board" \ "num-fields").as[Int]) {
            val f = ((json \ "controller" \ "board" \ "fields") (i) \ "field").as[JsObject]
            (f \ "type").get.as[String] match {
                case "action-field" =>
                    fields = fields :+ ActionField.fromJson(f)
                case "street" =>
                    fields = fields :+ Street.fromJson(f)
                case _ =>
            }
        }
        var players = List[Player]()
        for (i <- 0 until (json \ "controller" \ "board" \ "player-iterator" \ "num-players").as[Int]) {
            val p = ((json \ "controller" \ "board" \ "player-iterator" \ "players") (i) \ "player").as[JsObject]
            players = players :+ Player.fromJson(p, fields)
        }
        Board(
            fields,
            currentPlayer = players.find(p => p.name.equals((json \ "controller" \ "board" \ "current-player").get.as[String])).get,
            playerIt = PlayerIterator(players.toArray, (json \ "controller" \ "board" \ "player-iterator" \ "start-idx").get.as[Int]))
    }

    def fromSimplefiedJson(inputJson: JsObject): Board = {
        var fields = List[Field]()
        val json = (inputJson \ "controller" \ "board")
        for (i <- 0 until (json \ "num-fields").as[Int]) {
            val f = ((json \ "fields") (i) \ "field").as[JsObject]
            (f \ "type").get.as[String] match {
                case "action-field" =>
                    fields = fields :+ ActionField.fromJson(f)
                case "street" =>
                    fields = fields :+ Street.fromJson(f)
                case _ =>
            }
        }
        var players = List[Player]()
        for (i <- 0 until (json \ "player-iterator" \ "num-players").as[Int]) {
            val p = ((json \ "player-iterator" \ "players") (i) \ "player").as[JsObject]
            players = players :+ Player.fromJson(p, fields)
        }
        Board(
            fields,
            currentPlayer = players.find(p => p.name.equals((json \ "current-player").get.as[String])).get,
            playerIt = PlayerIterator(players.toArray, (json \ "player-iterator" \ "start-idx").get.as[Int]))
    }
}