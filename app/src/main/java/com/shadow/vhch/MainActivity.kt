package com.shadow.vhch

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import com.shadow.vhch.engine.ArkUnit
import com.shadow.vhch.engine.Board
import com.shadow.vhch.engine.Mech
import com.shadow.vhch.engine.MechClass
import com.shadow.vhch.engine.Pilot
import com.shadow.vhch.engine.PilotAbility
import com.shadow.vhch.engine.PilotStats
import com.shadow.vhch.engine.Position
import com.shadow.vhch.engine.Team

class MainActivity : Activity() {

    private val boardSize = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Сітку створюємо в коді, а не через activity_main.xml —
        // це прибирає залежність від інфлейту XML-ресурсу.
        val gridLayout = GridLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            rowCount = boardSize
            columnCount = boardSize
            setBackgroundColor(Color.WHITE)
        }

        setContentView(gridLayout)

        val board = buildSampleBoard()
        renderBoard(board, gridLayout)
    }

    /** Тимчасова тестова розстановка: один твій юніт і один ворожий, щоб перевірити рушій. */
    private fun buildSampleBoard(): Board {
        val board = Board()

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
        board.placeUnit(playerUnit)

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
        board.placeUnit(enemyUnit)

        return board
    }

    private fun renderBoard(board: Board, gridLayout: GridLayout) {
        gridLayout.removeAllViews()
        gridLayout.rowCount = boardSize
        gridLayout.columnCount = boardSize

        // row=0 малюємо зверху, тому y-координату інвертуємо (у Position (0,0) — низ поля)
        for (row in 0 until boardSize) {
            val y = boardSize - 1 - row
            for (col in 0 until boardSize) {
                val position = Position(col, y)
                val cell = TextView(this)
                cell.text = cellLabel(board, position)
                cell.gravity = Gravity.CENTER
                cell.textSize = 12f
                cell.setTextColor(Color.WHITE)
                cell.setBackgroundColor(cellColor(board, position))

                val params = GridLayout.LayoutParams()
                params.width = 0
                params.height = 0
                params.rowSpec = GridLayout.spec(row, 1f)
                params.columnSpec = GridLayout.spec(col, 1f)
                cell.layoutParams = params

                gridLayout.addView(cell)
            }
        }
    }

    private fun cellLabel(board: Board, position: Position): String {
        val unit = board.unitAt(position) ?: return ""
        return unit.mech.mechClass.displayName.take(1)
    }

    private fun cellColor(board: Board, position: Position): Int {
        val unit = board.unitAt(position)
        return when {
            unit?.team == Team.PLAYER -> Color.rgb(60, 140, 220)
            unit?.team == Team.ENEMY -> Color.rgb(220, 80, 80)
            (position.x + position.y) % 2 == 0 -> Color.rgb(210, 210, 210)
            else -> Color.rgb(170, 170, 170)
        }
    }
}
