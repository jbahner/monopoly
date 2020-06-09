package modelComponent.boardComponent.boardBaseImpl

import modelComponent.boardComponent.IBoard
import modelComponent.fieldComponent.fieldBaseImpl.{ActionField, Street}
import modelComponent.fieldComponent.{Field, IBuilding, IBuyable, IStreet}
import modelComponent.playerComponent.IPlayer
import modelComponent.playerComponent.playerBaseImpl.Player
import modelComponent.util.{GeneralUtil, PlayerIterator, RentContext}
import play.api.libs.json.{JsObject, Json}

import scala.xml.Elem

case class Board(fields: List[Field], currentPlayer: IPlayer, playerIt: PlayerIterator) extends IBoard {

    RentContext.board = this
    var currentDice: Int = 0
    var didPlayerPassGo: Boolean = false

    override def nextPlayerTurn(): IBoard = copy(getFields, nextPlayer(), getPlayerIt, currentDice)

    override def nextPlayer(): IPlayer = playerIt.next()

    override def replacePlayer(player: IPlayer, newPlayer: IPlayer): IBoard = {
        playerIt.replace(player, newPlayer)
        copy(getFields, currentPlayer = if (currentPlayer == player) newPlayer else currentPlayer, getPlayerIt, currentDice)
    }

    def getFields: List[Field] = fields

    def copy(fields: List[Field], currentPlayer: IPlayer, playerIt: PlayerIterator, currentDice: Int): IBoard = Board(fields, currentPlayer, playerIt)

    def getPlayerIt: PlayerIterator = playerIt

    def replaceField(field: IBuyable, newField: IBuyable): IBoard = {
        val newPlayers = playerIt.list.map(player => {
            player.copy(fieldIt = player.getFieldIt.replace(field, newField),
                currentField = if (player.getCurrentField().getName == field.getName) newField else player.getCurrentField(),
                bought = player.getBought.map(f => if (f.getName == field.getName) newField else f))
        })
        copy(fields = fields.updated(fields.indexOf(field), newField),
            currentPlayer = newPlayers.find(p => p.getName == currentPlayer.getName).get,
            playerIt = new PlayerIterator(newPlayers.toArray, playerIt.currentIdx),
            currentDice)
    }

    def getPlayerit: PlayerIterator = playerIt

    def getCurrentPlayer(): Option[IPlayer] = Option.apply(currentPlayer)

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
            "player-iterator" -> playerIt.toJson(),
            "currentDice" -> currentDice
        )
    }

    override def getHouseCost(streetName: String): Int = {
        getFieldByName(streetName).get.asInstanceOf[Street].houseCost
    }

    override def getFieldByName(fieldName: String): Option[Field] = {
        getFields.find(field => field.getName.equals(fieldName))
    }

    override def getHouseCount(streetName: String): Int = {
        getFieldByName(streetName).get.asInstanceOf[Street].numHouses
    }

    override def getCurrentField(): Field = {
        getCurrentPlayer.get.getCurrentField()
    }

    override def getCurrentFieldType(): String = {
        getCurrentField() match {
            case building: IBuyable => "Building"
            case street: IStreet => "Street"
            case field: Field => "Field"
        }
    }

    override def getCurrentFieldName(): String = {
        getCurrentField().getName
    }

    override def getCurrentFieldOwnedByString(): String = {
        val curField = getCurrentField().asInstanceOf[IBuyable]

        if (curField.isBought) getBuyer(curField).get.getName
        else "Nobody"

    }

    def getBuyer(buyable: IBuyable): Option[IPlayer] = {
        val players = getPlayerIt.list

        for (pl <- players) {
            val filteredSet = pl.getBought.filter(_.getName == buyable.getName)
            if (filteredSet.nonEmpty) {
                return Option.apply(pl)
            }
        }
        Option.empty
    }

    override def getCurrentFieldOwnerName(): String = {
        val currentFieldBuyer = getBuyer(getCurrentField().asInstanceOf[IBuyable])
        if (currentFieldBuyer.isDefined) currentFieldBuyer.get.getName
        else "Field Owner Could not be fetched."
    }

    def buildablesToString(buildables: List[Set[String]]): String = {
        val sb = new StringBuilder()
        buildables.foreach(set => {
            sb.append("\t")
            set.foreach(s => sb.append(s).append(" (").append(getFieldByName(s).get.asInstanceOf[IStreet].getHouseCost).append("€)\n"))
        })
        sb.toString()
    }

    def getPossibleBuildPlacesToString(): String = {
        val wholeGroups = GeneralUtil.getWholeGroups(getCurrentPlayer.get)
        buildablesToString(wholeGroups)
    }

    def getCurrentFieldRent(): Int = {
        RentContext.rentStrategy.executeStrategy(this, getCurrentField().asInstanceOf[IBuyable])
    }

    def buyCurrentField(): IBoard = {
        val buyable = getCurrentField().asInstanceOf[IBuyable]
        var newField = getCurrentField().asInstanceOf[IBuyable]
        newField match {
            case street: IStreet => newField = street.copy(isBought = true)
            case building: IBuilding => newField = building.copy(isBought = true)
        }
        val currentPlayer = getCurrentPlayer()
        val newPlayer: IPlayer = currentPlayer.get.copy(money = currentPlayer.get.getMoney - newField.getPrice,
            bought = currentPlayer.get.getBought + newField)

        replacePlayer(currentPlayer.get, newPlayer)
            .copy(getFields, newPlayer, getPlayerIt, currentDice)

        replaceField(buyable, newField)
    }

    override def buildHouses(streetName: String, amount: Int): IBoard = {
        val street = getFieldByName(streetName).get.asInstanceOf[IStreet]
        var board = replaceField(field = street, newField = street.buyHouses(amount))
        board = board.replacePlayer(board.getCurrentPlayer.get,
            board.getCurrentPlayer.get.copy(money = board.getCurrentPlayer.get.getMoney - street.getHouseCost * amount))
        board
    }

    def getAmountOfHousesOnStreet(streetName: String): Int = {
        getFieldByName(streetName).get.asInstanceOf[IStreet].getNumHouses
    }

    def canCurrentPlayerBuildOnStreet(streetName: String): Boolean = {
        val field = getFieldByName(streetName)

        if (field.isEmpty || !field.get.isInstanceOf[IStreet]) {
            return false
        }

        val street = getFieldByName(streetName).get.asInstanceOf[IStreet]
        val tmp = getBuyer(street).get
        getBuyer(street).isDefined && currentPlayer.getName == getBuyer(street).get.getName
    }

    def getCurrentPlayerMoney(): Int = {
        getCurrentPlayer().get.getMoney
    }

    def getStreetsHouseCost(streetName: String): Int = {
        getFieldByName(streetName).get.asInstanceOf[IStreet].getHouseCost
    }

    override def rollDice(): (Int, Int) = {
        val r = scala.util.Random
        val (r1, r2) = (r.nextInt(6) + 1, r.nextInt(6) + 1)
        currentDice = r1 + r2
        (r1, r2)
    }

    override def currentPlayerWalk(): IBoard = {
        val player = getCurrentPlayer().get
        val (newPlayer, passedGo) = getCurrentPlayer().get.walk(currentDice)

        didPlayerPassGo = passedGo

        replacePlayer(player, newPlayer)
    }

    def getDidPlayerPassGo(): Boolean = {
        didPlayerPassGo
    }

    override def getNewGameStateAfterWalk(): String = {
        getCurrentField().action(getCurrentPlayer().get)
    }

    override def canCurrentPlayerBuyHouses(): Boolean = {
        GeneralUtil.getWholeGroups(getCurrentPlayer().get) != Nil
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
            playerIt = PlayerIterator(players.toArray, (json \ "controller" \ "board" \ "player-iterator" \ "start-idx").get.as[Int])
        )
    }

    def fromSimplefiedJson(json: JsObject): Board = {
        var fields = List[Field]()
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