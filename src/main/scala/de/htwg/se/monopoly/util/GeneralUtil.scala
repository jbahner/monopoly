package de.htwg.se.monopoly.util

import java.awt.Color

import de.htwg.se.monopoly.model.playerComponent.IPlayer

import scala.swing.Color

object GeneralUtil {

    //TODO this needs to be somewhere else
    val groupList: List[Set[String]] = List(
        Set("Street1", "Street2", "Street3"),
        Set("Street4", "Street5", "Street6"),
        Set("Street7", "Street8", "Street9"))

    val standardColor: Color = new Color(242, 180, 114)

    val groupColors: Map[Int, Color] = Map(
        0 -> new Color(160,82,45),
        1 -> new Color(51, 232, 232),
        2 -> new Color(224, 79, 178),
        3 -> new Color(232, 140, 53),
        4 -> new Color(242, 41, 41),
        5 -> new Color(230, 250, 55),
        6 -> new Color(59, 186, 45),
        7 -> new Color(27, 116, 171)
    )

    def hasWholeGroup(player: IPlayer, street: String): Boolean = {
        val group = groupList.find(g => g.contains(street)).get
        group.subsetOf(player.getBought.flatMap(street => street.getName).asInstanceOf[Set[String]])
    }

    def getWholeGroups(player: IPlayer): List[Set[String]] = {
        var list: List[Set[String]] = List()
        groupList.foreach(group => {
            if (group.subsetOf(player.getBought.map(street => street.getName))) {
                list = list :+ group
            }
        })
        list
    }


}
