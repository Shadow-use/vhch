package com.shadow.vhch.ui

import android.content.Context
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import com.shadow.vhch.game.GameController

/**
 * "Диригент" екрана бою: з'єднує ігрову логіку (GameController) з малювальниками
 * (BoardRenderer, StatsRenderer) і кнопками "Кінець ходу"/"Заново". Це єдине місце,
 * яке знає про всіх трьох одразу — MainActivity знає лише про цей клас.
 */
class GameScreenConductor(private val context: Context) {

    private var gameController = GameController()

    private val boardRenderer = BoardRenderer(context)
    private val statsRenderer = StatsRenderer(context)

    /** Викликається один раз з MainActivity.onCreate() і йде прямо в setContentView(). */
    fun buildRootView(): ScrollView {
        val endTurnButton = Button(context).apply {
            text = "Кінець ходу"
            setOnClickListener {
                gameController.endTurn()
                refresh()
            }
        }

        val restartButton = Button(context).apply {
            text = "Заново"
            setOnClickListener {
                gameController = GameController()
                refresh()
            }
        }

        val buttonsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            addView(endTurnButton)
            addView(restartButton)
        }

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            addView(boardRenderer.view)
            addView(statsRenderer.view)
            addView(buttonsRow)
        }

        refresh()

        return ScrollView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            addView(root)
        }
    }

    private fun refresh() {
        boardRenderer.render(gameController) { position ->
            gameController.onCellTapped(position)
            refresh()
        }
        statsRenderer.render(gameController)
    }
}
