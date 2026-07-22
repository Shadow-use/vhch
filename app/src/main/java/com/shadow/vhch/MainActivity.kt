package com.shadow.vhch

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.shadow.vhch.engine.Position
import com.shadow.vhch.engine.Team
import com.shadow.vhch.game.GameController

/**
 * Тільки малює екран за даними з GameController і передає йому тапи/кнопки.
 * Жодної ігрової логіки тут бути не повинно — усе рахує GameController + engine.
 */
class MainActivity : Activity() {

    private val boardSize = 8
    private var gameController = GameController()

    private lateinit var gridLayout: GridLayout
    private lateinit var statsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        val endTurnButton = Button(this).apply {
            text = "Кінець ходу"
            setOnClickListener {
                gameController.endTurn()
                refreshUi()
            }
        }

        val restartButton = Button(this).apply {
            text = "Заново"
            setOnClickListener {
                gameController = GameController()
                refreshUi()
            }
        }

        val buttonsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            addView(endTurnButton)
            addView(restartButton)
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            addView(gridLayout)
            addView(statsContainer)
            addView(buttonsRow)
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

    private fun onCellTapped(position: Position) {
        gameController.onCellTapped(position)
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
                cell.setOnClickListener { onCellTapped(position) }

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
            text = buildHeaderText()
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 16)
        }
        statsContainer.addView(header)

        for (unit in gameController.allUnits()) {
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

    private fun buildHeaderText(): String {
        val selectionNote = gameController.selectedUnit?.let {
            " Обрано: ${it.mech.mechClass.displayName} — зелена клітинка = рух, помаранчева = атака."
        } ?: ""
        return gameController.statusMessage + selectionNote
    }

    private fun cellLabel(position: Position): String {
        val unit = gameController.unitAt(position) ?: return ""
        return unit.mech.mechClass.displayName.take(1)
    }

    private fun cellColor(position: Position): Int {
        val unit = gameController.unitAt(position)
        return when {
            gameController.isSelected(position) -> Color.rgb(255, 200, 0) // золотий — обраний юніт
            gameController.isAvailableTarget(position) -> Color.rgb(255, 140, 0) // помаранчевий — можна атакувати
            unit?.team == Team.PLAYER -> Color.rgb(60, 140, 220)
            unit?.team == Team.ENEMY -> Color.rgb(220, 80, 80)
            gameController.isAvailableMove(position) -> Color.rgb(140, 220, 140) // зелений — доступний хід
            (position.x + position.y) % 2 == 0 -> Color.rgb(210, 210, 210)
            else -> Color.rgb(170, 170, 170)
        }
    }
}
