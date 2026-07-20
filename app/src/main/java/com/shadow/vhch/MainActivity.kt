package com.shadow.vhch

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.shadow.vhch.engine.ArkUnit
import com.shadow.vhch.engine.Board
import com.shadow.vhch.engine.CombatEngine
import com.shadow.vhch.engine.Mech
import com.shadow.vhch.engine.MechClass
import com.shadow.vhch.engine.Pilot
import com.shadow.vhch.engine.PilotAbility
import com.shadow.vhch.engine.PilotStats
import com.shadow.vhch.engine.Position
import com.shadow.vhch.engine.Team

class MainActivity : Activity() {

    private val boardSize = 8

    private lateinit var board: Board
    private lateinit var combatEngine: CombatEngine
    private lateinit var gridLayout: GridLayout
    private lateinit var statsContainer: LinearLayout

    private var selectedUnit: ArkUnit? = null
    private var availableMoveCells: List<Position> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        board = buildSampleBoard()
        combatEngine = CombatEngine(board)

        val screenWidth = resources.displayMetrics.widthPixels

        gridLayout = GridLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(screenWidth, screenWidth)
            rowCount = boardSize
            columnCount = boardSize
            setBackgroundColor(Color.BLACK)
        }

        statsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(32, 32, 32, 32)
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            addView(gridLayout)
            addView(statsContainer)
        }

        val scrollView = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            addView(root)
        }

        setContentView(scrollView)

        refreshUi()
    }

    /** Тимчасова тестова розстановка: один твій юніт і один ворожий, щоб перевірити рушій. */
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

    /** Обробка тапу по клітинці: вибір юніта, зняття вибору, або переміщення на підсвічену клітинку. */
    private fun handleCellTap(position: Position) {
        val tappedUnit = board.unitAt(position)
        val currentSelection = selectedUnit

        when {
            currentSelection == null -> {
                if (tappedUnit != null && tappedUnit.team == Team.PLAYER) {
                    selectedUnit = tappedUnit
                    availableMoveCells = combatEngine.availableMoves(tappedUnit)
                }
            }
            tappedUnit == currentSelection -> {
                selectedUnit = null
                availableMoveCells = emptyList()
            }
            position in availableMoveCells -> {
                board.moveUnit(currentSelection, position)
                selectedUnit = null
                availableMoveCells = emptyList()
            }
            tappedUnit != null && tappedUnit.team == Team.PLAYER -> {
                selectedUnit = tappedUnit
                availableMoveCells = combatEngine.availableMoves(tappedUnit)
            }
            else -> {
                selectedUnit = null
                availableMoveCells = emptyList()
            }
        }

        refreshUi()
    }

    private fun refreshUi() {
        renderBoard()
        renderStats()
    }

    private fun renderBoard() {
        gridLayout.removeAllViews()
        gridLayout.rowCount = boardSize
        gridLayout.columnCount = boardSize

        val screenWidth = resources.displayMetrics.widthPixels
        val cellSize = screenWidth / boardSize

        // row=0 малюємо зверху, тому y-координату інвертуємо (у Position (0,0) — низ поля)
        for (row in 0 until boardSize) {
            val y = boardSize - 1 - row
            for (col in 0 until boardSize) {
                val position = Position(col, y)
                val cell = TextView(this)
                cell.text = cellLabel(position)
                cell.gravity = Gravity.CENTER
                cell.textSize = 12f
                cell.setTextColor(Color.WHITE)
                cell.setBackgroundColor(cellColor(position))
                cell.setOnClickListener { handleCellTap(position) }

                val params = GridLayout.LayoutParams()
                params.width = cellSize
                params.height = cellSize
                params.rowSpec = GridLayout.spec(row)
                params.columnSpec = GridLayout.spec(col)
                params.setMargins(1, 1, 1, 1)
                cell.layoutParams = params

                gridLayout.addView(cell)
            }
        }
    }

    private fun renderStats() {
        statsContainer.removeAllViews()

        val header = TextView(this).apply {
            text = if (selectedUnit != null) {
                "Обрано: ${selectedUnit!!.mech.mechClass.displayName} — тапни зелену клітинку, щоб перемістити"
            } else {
                "Статистика юнітів"
            }
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 16)
        }
        statsContainer.addView(header)

        for (unit in board.allUnits()) {
            val teamLabel = if (unit.team == Team.PLAYER) "гравець" else "ворог"
            val line = TextView(this).apply {
                text = "${unit.mech.mechClass.displayName} ($teamLabel) — " +
                    "HP: ${unit.mech.currentHp}/${unit.mech.maxHp}, синхр.: ${unit.pilot.syncRate}%"
                textSize = 14f
                setTextColor(Color.DKGRAY)
                setPadding(0, 8, 0, 8)
            }
            statsContainer.addView(line)
        }
    }

    private fun cellLabel(position: Position): String {
        val unit = board.unitAt(position) ?: return ""
        return unit.mech.mechClass.displayName.take(1)
    }

    private fun cellColor(position: Position): Int {
        val unit = board.unitAt(position)
        return when {
            unit != null && unit == selectedUnit -> Color.rgb(255, 200, 0) // золотий — обраний юніт
            unit?.team == Team.PLAYER -> Color.rgb(60, 140, 220)
            unit?.team == Team.ENEMY -> Color.rgb(220, 80, 80)
            position in availableMoveCells -> Color.rgb(140, 220, 140) // зелений — доступний хід
            (position.x + position.y) % 2 == 0 -> Color.rgb(210, 210, 210)
            else -> Color.rgb(170, 170, 170)
        }
    }
}
