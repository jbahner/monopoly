package de.htwg.se.monopoly.util

import de.htwg.se.monopoly.model.playerComponent.Player

object GeneralUtil {

    //TODO this needs to be somewhere else
    val groupList: List[Set[String]] = List(
        Set("Street1", "Street2", "Street3"),
        Set("Street4", "Street5", "Street6"),
        Set("Street7", "Street8", "Street9"))


    def hasWholeGroup(player: Player, street: String): Boolean = {
        val group = groupList.find(g => g.contains(street)).get
        group.subsetOf(player.bought.flatMap(street => street.getName).asInstanceOf[Set[String]])
    }

    def getWholeGroups(player: Player): List[Set[String]] = {
        var list: List[Set[String]] = List()
        groupList.foreach(group => {
            if (group.subsetOf(player.bought.map(street => street.getName))) {
                list = list :+ group
            }
        })
        list
    }


}
