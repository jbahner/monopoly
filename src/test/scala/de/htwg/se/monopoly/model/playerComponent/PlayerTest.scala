package de.htwg.se.monopoly.model.playerComponent

import junit.framework.TestCase
import org.junit.Assert._
import org.junit.{BeforeClass, Test}


class PlayerTest extends TestCase {

    var player: Player = _

    @BeforeClass
    override def setUp(): Unit = {
        player = Player("Nik", 1500)
    }

    @Test
    def testConstructor(): Unit = {
        assertTrue(player.name.equals("Nik"))
        assertTrue(player.money == 1500)
    }

}
