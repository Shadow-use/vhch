package com.shadow.vhch.game

import com.shadow.vhch.engine.ArkUnit
import com.shadow.vhch.engine.Board
import com.shadow.vhch.engine.Mech
import com.shadow.vhch.engine.MechClass
import com.shadow.vhch.engine.Pilot
import com.shadow.vhch.engine.PilotAbility
import com.shadow.vhch.engine.PilotStats
import com.shadow.vhch.engine.Position
import com.shadow.vhch.engine.Team
import com.shadow.vhch.engine.Terrain

/**
 * Відповідає РІВНО за одне: зібрати тестову дошку з юнітами й місцевістю для бою.
 * Коли з'явиться кампанія, тут буде завантаження реального складу загону гравця
 * замість цієї тимчасової фіксованої розстановки — і більше ніде в проєкті
 * не доведеться це шукати.
 */
object SampleBoardFactory {

    fun build(): Board {
        val board = Board(terrainMap())

        board.placeUnit(buildPlayerGuardian())
        board.placeUnit(buildPlayerStriker())
        board.placeUnit(buildEnemyStriker())
        board.placeUnit(buildEnemyArtillery())

        return board
    }

    private fun terrainMap(): Map<Position, Terrain> = mapOf(
        Position(3, 3) to Terrain.RUINS,
        Position(4, 4) to Terrain.RUINS,
        Position(2, 5) to Terrain.FOREST,
        Position(5, 2) to Terrain.FOREST
    )

    private fun buildPlayerGuardian(): ArkUnit {
        val pilot = Pilot(
            id = "p1",
            name = "Тестовий пілот",
            stats = PilotStats(accuracy = 2, evasion = 1, willpower = 3),
            ability = PilotAbility(name = "Прорив", description = "Бонус урону + самолікування", damageBonus = 5, healAmount = 4)
        )
        val mech = Mech(id = "m1", mechClass = MechClass.GUARDIAN)
        return ArkUnit(id = "u1", team = Team.PLAYER, pilot = pilot, mech = mech, position = Position(1, 1))
    }

    private fun buildPlayerStriker(): ArkUnit {
        val pilot = Pilot(
            id = "p2",
            name = "Другий пілот",
            stats = PilotStats(accuracy = 1, evasion = 2, willpower = 2),
            ability = PilotAbility(name = "Ривок", description = "Бонус урону", damageBonus = 3)
        )
        val mech = Mech(id = "m2", mechClass = MechClass.STRIKER)
        return ArkUnit(id = "u2", team = Team.PLAYER, pilot = pilot, mech = mech, position = Position(2, 1))
    }

    private fun buildEnemyStriker(): ArkUnit {
        val pilot = Pilot(
            id = "e1",
            name = "Ворог 1",
            stats = PilotStats(accuracy = 1, evasion = 1, willpower = 2),
            ability = PilotAbility(name = "Стрибок", description = "Бонус урону", damageBonus = 3)
        )
        val mech = Mech(id = "em1", mechClass = MechClass.STRIKER)
        return ArkUnit(id = "e_u1", team = Team.ENEMY, pilot = pilot, mech = mech, position = Position(6, 6))
    }

    private fun buildEnemyArtillery(): ArkUnit {
        val pilot = Pilot(
            id = "e2",
            name = "Ворог 2",
            stats = PilotStats(accuracy = 1, evasion = 0, willpower = 1),
            ability = PilotAbility(name = "Важкий залп", description = "Бонус урону", damageBonus = 4)
        )
        val mech = Mech(id = "em2", mechClass = MechClass.ARTILLERY)
        return ArkUnit(id = "e_u2", team = Team.ENEMY, pilot = pilot, mech = mech, position = Position(5, 6))
    }
}
