package com.shadow.vhch.game

import com.shadow.vhch.engine.ArkUnit
import com.shadow.vhch.engine.Board
import com.shadow.vhch.engine.CombatEngine
import com.shadow.vhch.engine.Team

/**
 * Перша, дуже проста версія "мозку" для ворожих юнітів: якщо є ціль у радіусі атаки —
 * б'є; якщо ні — рухається до найближчого юніта гравця. Не тактична, просто щоб
 * вороги не стояли на місці, поки не зробимо щось розумніше.
 */
class EnemyAI(
    private val board: Board,
    private val combatEngine: CombatEngine
) {

    fun takeTurn() {
        val enemyUnits = board.aliveUnits(Team.ENEMY)
        for (unit in enemyUnits) {
            if (unit.isAlive) {
                actForUnit(unit)
            }
        }
    }

    private fun actForUnit(unit: ArkUnit) {
        val targetsInRange = combatEngine.availableTargets(unit)
        if (targetsInRange.isNotEmpty()) {
            val target = targetsInRange.minByOrNull { unit.position.distanceTo(it.position) } ?: return
            combatEngine.resolveAttack(unit, target)
            return
        }

        val nearestPlayerUnit = board.aliveUnits(Team.PLAYER)
            .minByOrNull { unit.position.distanceTo(it.position) } ?: return

        val moves = combatEngine.availableMoves(unit)
        if (moves.isEmpty()) return

        val bestMove = moves.minByOrNull { it.distanceTo(nearestPlayerUnit.position) } ?: return
        board.moveUnit(unit, bestMove)

        if (unit.mech.mechClass.canMoveAndAttackSameTurn) {
            val targetsAfterMove = combatEngine.availableTargets(unit)
            if (targetsAfterMove.isNotEmpty()) {
                val target = targetsAfterMove.minByOrNull { unit.position.distanceTo(it.position) } ?: return
                combatEngine.resolveAttack(unit, target)
            }
        }
    }
}
