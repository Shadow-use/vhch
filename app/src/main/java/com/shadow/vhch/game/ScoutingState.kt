package com.shadow.vhch.game

import com.shadow.vhch.engine.ArkUnit
import com.shadow.vhch.engine.CombatEngine
import com.shadow.vhch.engine.Position

/**
 * Відповідає РІВНО за одне: якого ворожого юніта зараз "розвідує" гравець,
 * і які клітинки той може атакувати наступного ходу.
 */
class ScoutingState(private val combatEngine: CombatEngine) {

    var inspectedEnemy: ArkUnit? = null
        private set

    private var threatCells: Set<Position> = emptySet()

    fun inspect(unit: ArkUnit) {
        inspectedEnemy = unit
        threatCells = combatEngine.threatRange(unit)
    }

    fun clear() {
        inspectedEnemy = null
        threatCells = emptySet()
    }

    fun isInspecting(unit: ArkUnit): Boolean = inspectedEnemy == unit

    fun isThreatCell(position: Position): Boolean = position in threatCells
}
