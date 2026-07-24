package com.shadow.vhch.game

import com.shadow.vhch.engine.ArkUnit
import com.shadow.vhch.engine.CombatEngine
import com.shadow.vhch.engine.Position

/**
 * Відповідає РІВНО за одне: який юніт гравця зараз обрано, і куди він може
 * ходити чи кого атакувати. Нічого не знає про черговість ходів чи розвідку.
 */
class SelectionState(private val combatEngine: CombatEngine) {

    var selectedUnit: ArkUnit? = null
        private set

    var availableMoveCells: List<Position> = emptyList()
        private set

    var availableTargets: List<ArkUnit> = emptyList()
        private set

    fun select(unit: ArkUnit) {
        selectedUnit = unit
        availableMoveCells = combatEngine.availableMoves(unit)
        availableTargets = combatEngine.availableTargets(unit)
    }

    fun clear() {
        selectedUnit = null
        availableMoveCells = emptyList()
        availableTargets = emptyList()
    }

    fun isSelected(unit: ArkUnit?): Boolean = unit != null && unit == selectedUnit

    fun isAvailableMove(position: Position): Boolean = position in availableMoveCells

    fun isAvailableTarget(unit: ArkUnit?): Boolean = unit != null && unit in availableTargets
}
