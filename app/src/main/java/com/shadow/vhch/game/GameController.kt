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
import com.shadow.vhch.engine.Terrain

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
    private val actedUnitIds: MutableSet<String> = mutableSetOf()

    var currentTurn: Team = Team.PLAYER
        private set

    var statusMessage: String = "Твій хід"
        private set

    var gameOver: Boolean = false
        private set

    /** Тестова розстановка: по два юніти на сторону, кілька клітинок місцевості. */
    private fun buildSampleBoard(): Board {
        val terrainMap = mapOf(
            Position(3, 3) to Terrain.RUINS,
            Position(4, 4) to Terrain.RUINS,
            Position(2, 5) to Terrain.FOREST,
            Position(5, 2) to Terrain.FOREST
        )
        val newBoard = Board(terrainMap)

        val playerPilot1 = Pilot(
            id = "p1",
            name = "Тестовий пілот",
            stats = PilotStats(accuracy = 2, evasion = 1, willpower = 3),
            ability = PilotAbility(name = "Прорив", description = "Бонус урону + самолікування", damageBonus = 5, healAmount = 4)
        )
        val playerMech1 = Mech(id = "m1", mechClass = MechClass.GUARDIAN)
        newBoard.placeUnit(
            ArkUnit(id = "u1", team = Team.PLAYER, pilot = playerPilot1, mech = playerMech1, position = Position(1, 1))
        )

        val playerPilot2 = Pilot(
            id = "p2",
            name = "Другий пілот",
            stats = PilotStats(accuracy = 1, evasion = 2, willpower = 2),
            ability = PilotAbility(name = "Ривок", description = "Бонус урону", damageBonus = 3)
        )
        val playerMech2 = Mech(id = "m2", mechClass = MechClass.STRIKER)
        newBoard.placeUnit(
            ArkUnit(id = "u2", team = Team.PLAYER, pilot = playerPilot2, mech = playerMech2, position = Position(2, 1))
        )

        val enemyPilot1 = Pilot(
            id = "e1",
            name = "Ворог 1",
            stats = PilotStats(accuracy = 1, evasion = 1, willpower = 2),
            ability = PilotAbility(name = "Стрибок", description = "Бонус урону", damageBonus = 3)
        )
        val enemyMech1 = Mech(id = "em1", mechClass = MechClass.STRIKER)
        newBoard.placeUnit(
            ArkUnit(id = "e_u1", team = Team.ENEMY, pilot = enemyPilot1, mech = enemyMech1, position = Position(6, 6))
        )

        val enemyPilot2 = Pilot(
            id = "e2",
            name = "Ворог 2",
            stats = PilotStats(accuracy = 1, evasion = 0, willpower = 1),
            ability = PilotAbility(name = "Важкий залп", description = "Бонус урону", damageBonus = 4)
        )
        val enemyMech2 = Mech(id = "em2", mechClass = MechClass.ARTILLERY)
        newBoard.placeUnit(
            ArkUnit(id = "e_u2", team = Team.ENEMY, pilot = enemyPilot2, mech = enemyMech2, position = Position(5, 6))
        )

        return newBoard
    }

    /** Тап по клітинці: вибір юніта, зняття вибору, рух або атака. */
    fun onCellTapped(position: Position) {
        if (gameOver || currentTurn != Team.PLAYER) return

        val tappedUnit = board.unitAt(position)
        val currentSelection = selectedUnit

        when {
            currentSelection == null -> {
                if (tappedUnit != null && tappedUnit.team == Team.PLAYER && tappedUnit.id !in actedUnitIds) {
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
                actedUnitIds.add(currentSelection.id)
                clearSelection()
            }
            tappedUnit != null && tappedUnit.team == Team.PLAYER && tappedUnit.id !in actedUnitIds -> {
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
        actedUnitIds.clear()
    }

    private fun performAttack(attacker: ArkUnit, target: ArkUnit) {
        val result = combatEngine.resolveAttack(attacker, target)
        actedUnitIds.add(attacker.id)
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

    fun isSelected(position: Position): Boolean {
        val unit = board.unitAt(position) ?: return false
        return unit == selectedUnit
    }

    fun isAvailableMove(position: Position): Boolean = position in availableMoveCells

    fun isAvailableTarget(position: Position): Boolean {
        val unit = board.unitAt(position) ?: return false
        return unit in availableTargets
    }

    fun hasActed(position: Position): Boolean {
        val unit = board.unitAt(position) ?: return false
        return unit.id in actedUnitIds
    }

    fun terrainAt(position: Position): Terrain = board.terrainAt(position)

    fun allUnits(): List<ArkUnit> = board.allUnits()
}
