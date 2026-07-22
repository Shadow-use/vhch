package com.shadow.vhch.game

import com.shadow.vhch.engine.ArkUnit
import com.shadow.vhch.engine.AttackResult
import com.shadow.vhch.engine.Board
import com.shadow.vhch.engine.CombatEngine
import com.shadow.vhch.engine.Mech
import com.shadow.vhch.engine.MechClass
import com.shadow.vhch.engine.Pilot
import com.shadow.vhch.engine.PilotAbility
import com.shadow.vhch.engine.PilotStats
import com.shadow.vhch.engine.Position
import com.shadow.vhch.engine.Team

/**
 * Тримає весь стан поточного бою: дошку, кого обрано, доступні ходи/цілі,
 * чия зараз черга, і чи гру вже завершено. MainActivity лише запитує тут
 * дані для малювання і повідомляє про тапи/кінець ходу.
 */
class GameController {

    val board: Board = buildSampleBoard()
    private val combatEngine: CombatEngine = CombatEngine(board)
    private val enemyAI = EnemyAI(board, combatEngine)

    var selectedUnit: ArkUnit? = null
        private set

    var availableMoveCells: List<Position> = emptyList()
        private set

    private var availableTargets: List<ArkUnit> = emptyList()

    var currentTurn: Team = Team.PLAYER
        private set

    var statusMessage: String = "Твій хід"
        private set

    var gameOver: Boolean = false
        private set

    /** Тимчасова тестова розстановка: один твій юніт і один ворожий. */
    private fun buildSampleBoard(): Board {
        val newBoard = Board()

        val playerPilot = Pilot(
            id = "p1",
            name = "Тестовий пілот",
            stats = PilotStats(accuracy = 2, evasion = 1, willpower = 3),
            ability = PilotAbility(name = "Прорив", description = "Бонус урону", damageBonus = 5)
        )
        val playerMech = Mech(id = "m1", mechClass = MechClass.GUARDIAN)
        val playerUnit = ArkUnit(
            id = "u1",
            team = Team.PLAYER,
            pilot = playerPilot,
            mech = playerMech,
            position = Position(1, 1)
        )
        newBoard.placeUnit(playerUnit)

        val enemyPilot = Pilot(
            id = "e1",
            name = "Ворог",
            stats = PilotStats(accuracy = 1, evasion = 1, willpower = 2),
            ability = PilotAbility(name = "Стрибок", description = "-", damageBonus = 0)
        )
        val enemyMech = Mech(id = "m2", mechClass = MechClass.STRIKER)
        val enemyUnit = ArkUnit(
            id = "u2",
            team = Team.ENEMY,
            pilot = enemyPilot,
            mech = enemyMech,
            position = Position(6, 6)
        )
        newBoard.placeUnit(enemyUnit)

        return newBoard
    }

    /** Тап по клітинці: вибір юніта, зняття вибору, рух або атака. */
    fun onCellTapped(position: Position) {
        if (gameOver || currentTurn != Team.PLAYER) return

        val tappedUnit = board.unitAt(position)
        val currentSelection = selectedUnit

        when {
            currentSelection == null -> {
                if (tappedUnit != null && tappedUnit.team == Team.PLAYER) {
                    select(tappedUnit)
                }
            }
            tappedUnit == currentSelection -> {
                clearSelection()
            }
            tappedUnit != null && tappedUnit.team == Team.ENEMY && tappedUnit in availableTargets -> {
                performAttack(currentSelection, tappedUnit)
            }
            position in availableMoveCells -> {
                board.moveUnit(currentSelection, position)
                clearSelection()
            }
            tappedUnit != null && tappedUnit.team == Team.PLAYER -> {
                select(tappedUnit)
            }
            else -> {
                clearSelection()
            }
        }
    }

    /** Викликається кнопкою "Кінець ходу": передає хід ворогу, той діє, повертає хід гравцю. */
    fun endTurn() {
        if (gameOver) return
        clearSelection()
        currentTurn = Team.ENEMY
        statusMessage = "Хід ворога..."

        enemyAI.takeTurn()

        if (checkGameOver()) return

        currentTurn = Team.PLAYER
        statusMessage = "Твій хід"
    }

    private fun performAttack(attacker: ArkUnit, target: ArkUnit) {
        val result = combatEngine.resolveAttack(attacker, target)
        statusMessage = buildAttackMessage(attacker, result)
        clearSelection()
        checkGameOver()
    }

    private fun buildAttackMessage(attacker: ArkUnit, result: AttackResult): String {
        val destroyedNote = if (result.targetDestroyed) " Ціль знищено!" else ""
        val abilityNote = if (result.attackerAbilityTriggered) " Здібність пілота готова!" else ""
        return "${attacker.mech.mechClass.displayName} завдав ${result.damageDealt} урону.$destroyedNote$abilityNote"
    }

    private fun checkGameOver(): Boolean {
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

    private fun select(unit: ArkUnit) {
        selectedUnit = unit
        availableMoveCells = combatEngine.availableMoves(unit)
        availableTargets = combatEngine.availableTargets(unit)
    }

    private fun clearSelection() {
        selectedUnit = null
        availableMoveCells = emptyList()
        availableTargets = emptyList()
    }

    fun unitAt(position: Position): ArkUnit? = board.unitAt(position)

    fun isSelected(position: Position): Boolean = board.unitAt(position) == selectedUnit

    fun isAvailableMove(position: Position): Boolean = position in availableMoveCells

    fun isAvailableTarget(position: Position): Boolean {
        val unit = board.unitAt(position) ?: return false
        return unit in availableTargets
    }

    fun allUnits(): List<ArkUnit> = board.allUnits()
}
