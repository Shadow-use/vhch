package com.shadow.vhch.game

import com.shadow.vhch.engine.ArkUnit
import com.shadow.vhch.engine.AttackResult
import com.shadow.vhch.engine.CombatEngine
import com.shadow.vhch.engine.Team

/**
 * Відповідає РІВНО за одне: чия зараз черга, хто з юнітів гравця вже діяв
 * цього ходу, і чи бій уже завершено. Нічого не знає про те, як гравець
 * обирає юнітів чи розвідує ворога — тільки чиста черговість.
 */
class TurnManager(
    private val combatEngine: CombatEngine,
    private val enemyAI: EnemyAI
) {

    private val actedUnitIds: MutableSet<String> = mutableSetOf()

    var currentTurn: Team = Team.PLAYER
        private set

    var statusMessage: String = "Твій хід"
        private set

    var gameOver: Boolean = false
        private set

    fun isPlayerTurn(): Boolean = currentTurn == Team.PLAYER && !gameOver

    fun hasActed(unit: ArkUnit): Boolean = unit.id in actedUnitIds

    fun markActed(unit: ArkUnit) {
        actedUnitIds.add(unit.id)
    }

    fun endTurn() {
        if (gameOver) return
        currentTurn = Team.ENEMY
        statusMessage = "Хід ворога..."

        enemyAI.takeTurn()

        if (checkGameOver()) return

        currentTurn = Team.PLAYER
        statusMessage = "Твій хід"
        actedUnitIds.clear()
    }

    fun reportAttack(attacker: ArkUnit, result: AttackResult) {
        val destroyedNote = if (result.targetDestroyed) " Ціль знищено!" else ""
        val abilityNote = if (result.attackerAbilityTriggered) " Здібність пілота готова!" else ""
        statusMessage = "${attacker.mech.mechClass.displayName} завдав ${result.damageDealt} урону.$destroyedNote$abilityNote"
    }

    fun checkGameOver(): Boolean {
        return when {
            combatEngine.isTeamDefeated(Team.PLAYER) -> {
                gameOver = true
                statusMessage = "Поразка... Натисни \"Заново\", щоб спробувати ще раз."
                true
            }
            combatEngine.isTeamDefeated(Team.ENEMY) -> {
                gameOver = true
                statusMessage = "Перемога! Натисни \"Заново\" для нового бою."
                true
            }
            else -> false
        }
    }
}
