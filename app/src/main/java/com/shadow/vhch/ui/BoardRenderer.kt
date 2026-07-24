package com.shadow.vhch.ui

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.shadow.vhch.engine.Position
import com.shadow.vhch.engine.Team
import com.shadow.vhch.engine.Terrain
import com.shadow.vhch.game.GameController

/**
 * Відповідає РІВНО за одне: намалювати квадратну сітку поля бою за даними
 * з GameController. Нічого не знає про кнопки чи текстову статистику.
 */
class BoardRenderer(private val context: Context, private val boardSize: Int = 8) {

    val view: GridLayout = GridLayout(context).apply {
        val screenWidth = context.resources.displayMetrics.widthPixels
        layoutParams = LinearLayout.LayoutParams(screenWidth, screenWidth)
        rowCount = boardSize
        columnCount = boardSize
        setBackgroundColor(Color.BLACK)
    }

    /** onCellTapped викликається з готовою Position — сам view нічого не вирішує. */
    fun render(gameController: GameController, onCellTapped: (Position) -> Unit) {
        view.removeAllViews()
        view.rowCount = boardSize
        view.columnCount = boardSize

        val screenWidth = context.resources.displayMetrics.widthPixels
        val cellSize = screenWidth / boardSize

        // row=0 малюємо зверху, тому y-координату інвертуємо (у Position (0,0) — низ поля)
        for (row in 0 until boardSize) {
            val y = boardSize - 1 - row
            for (col in 0 until boardSize) {
                val position = Position(col, y)
                val cell = TextView(context).apply {
                    text = cellLabel(gameController, position)
                    gravity = Gravity.CENTER
                    textSize = 12f
                    setTextColor(Color.WHITE)
                    setBackgroundColor(cellColor(gameController, position))
                    setOnClickListener { onCellTapped(position) }
                }

                val params = GridLayout.LayoutParams().apply {
                    width = cellSize
                    height = cellSize
                    rowSpec = GridLayout.spec(row)
                    columnSpec = GridLayout.spec(col)
                    setMargins(1, 1, 1, 1)
                }
                cell.layoutParams = params

                view.addView(cell)
            }
        }
    }

    private fun cellLabel(gameController: GameController, position: Position): String {
        val unit = gameController.unitAt(position) ?: return ""
        return unit.mech.mechClass.displayName.take(1)
    }

    private fun cellColor(gameController: GameController, position: Position): Int {
        val unit = gameController.unitAt(position)
        val terrain = gameController.terrainAt(position)
        return when {
            gameController.isSelected(position) -> Color.rgb(255, 200, 0) // золотий — обраний юніт
            gameController.isAvailableTarget(position) -> Color.rgb(255, 140, 0) // помаранчевий — можна атакувати
            unit?.team == Team.PLAYER && gameController.hasActed(position) -> Color.rgb(30, 70, 110) // тьмяний — вже діяв
            unit?.team == Team.PLAYER -> Color.rgb(60, 140, 220)
            unit?.team == Team.ENEMY -> Color.rgb(220, 80, 80)
            gameController.isAvailableMove(position) -> Color.rgb(140, 220, 140) // зелений — доступний хід
            gameController.isThreatCell(position) -> Color.rgb(160, 90, 200) // фіолетовий — зона загрози
            terrain == Terrain.RUINS -> Color.rgb(150, 130, 110)
            terrain == Terrain.FOREST -> Color.rgb(90, 150, 90)
            (position.x + position.y) % 2 == 0 -> Color.rgb(210, 210, 210)
            else -> Color.rgb(170, 170, 170)
        }
    }
}
