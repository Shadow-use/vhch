package com.shadow.vhch.game

import com.shadow.vhch.engine.ArkUnit
import com.shadow.vhch.engine.Board
import com.shadow.vhch.engine.CombatEngine
import com.shadow.vhch.engine.Position
import com.shadow.vhch.engine.Team
import com.shadow.vhch.engine.Terrain

/**
 * Координатор бою. З'єднує дошку з рушієм і трьома окремими станами
 * (SelectionState, ScoutingState, TurnManager). Сам нічого не рахує —
 * тільки скеровує тапи в потрібне місце і надає дані для малювання екрана.
 * Публічний інтерфейс для MainActivity лишається незмінним навмисно —
 * екран не повинен знати, що всередині щось перебудували.
 */
class GameController {

    val board: Board = SampleBoardFactory.build()
    private val combatEngine = CombatEngine(board)
    private val enemyAI = EnemyAI(board, combatEngine)

    private val selection = SelectionState(combatEngine)
    private val scouting = ScoutingState(combatEngine)
    private val turnManager = TurnManager(combatEngine, enemyAI)

    val selectedUnit: ArkUnit? get() = selection.selectedUnit
    val inspectedEnemy: ArkUnit? get() = scouting.inspectedEnemy
    val currentTurn: Team get() = turnManager.currentTurn
    val statusMessage: String get() = turnManager.statusMessage
    val gameOver: Boolean get() = turnManager.gameOver

    /** Тап по клітинці: вибір юніта, розвідка ворога, рух або атака. */
    fun onCellTapped(position: Position) {
        if (!turnManager.isPlayerTurn()) return

        val tappedUnit = board.unitAt(position)
        val currentSelection = selection.selectedUnit

        when {
            currentSelection == null && tappedUnit != null && tappedUnit.team == Team.PLAYER &&
                !turnManager.hasActed(tappedUnit) -> {
                scouting.clear()
                selection.select(tappedUnit)
            }
            currentSelection == null && tappedUnit != null && tappedUnit.team == Team.ENEMY -> {
                if (scouting.isInspecting(tappedUnit)) scouting.clear() else scouting.inspect(tappedUnit)
            }
            currentSelection != null && tappedUnit == currentSelection -> {
                selection.clear()
            }
            currentSelection != null && tappedUnit != null && tappedUnit.team == Team.ENEMY &&
                selection.isAvailableTarget(tappedUnit) -> {
                val result = combatEngine.resolveAttack(currentSelection, tappedUnit)
                turnManager.markActed(currentSelection)
                turnManager.reportAttack(currentSelection, result)
                selection.clear()
                turnManager.checkGameOver()
            }
            currentSelection != null && selection.isAvailableMove(position) -> {
                board.moveUnit(currentSelection, position)
                turnManager.markActed(currentSelection)
                selection.clear()
            }
            currentSelection != null && tappedUnit != null && tappedUnit.team == Team.PLAYER &&
                !turnManager.hasActed(tappedUnit) -> {
                selection.select(tappedUnit)
            }
            else -> {
                selection.clear()
                scouting.clear()
            }
        }
    }

    /** Викликається кнопкою "Кінець ходу". */
    fun endTurn() {
        selection.clear()
        scouting.clear()
        turnManager.endTurn()
    }

    fun unitAt(position: Position): ArkUnit? = board.unitAt(position)

    fun isSelected(position: Position): Boolean = selection.isSelected(board.unitAt(position))

    fun isAvailableMove(position: Position): Boolean = selection.isAvailableMove(position)

    fun isAvailableTarget(position: Position): Boolean = selection.isAvailableTarget(board.unitAt(position))

    fun hasActed(position: Position): Boolean {
        val unit = board.unitAt(position) ?: return false
        return turnManager.hasActed(unit)
    }

    fun terrainAt(position: Position): Terrain = board.terrainAt(position)

    fun isThreatCell(position: Position): Boolean = scouting.isThreatCell(position)

    fun allUnits(): List<ArkUnit> = board.allUnits()
}
