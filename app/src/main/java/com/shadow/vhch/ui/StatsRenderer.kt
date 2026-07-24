package com.shadow.vhch.ui

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.shadow.vhch.engine.Team
import com.shadow.vhch.game.GameController

/**
 * Відповідає РІВНО за одне: показати текст статусу ходу і статистику юнітів.
 * Нічого не знає про сітку поля чи кнопки.
 */
class StatsRenderer(private val context: Context) {

    val view: LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        setPadding(32, 32, 32, 32)
    }

    fun render(gameController: GameController) {
        view.removeAllViews()

        val header = TextView(context).apply {
            text = buildHeaderText(gameController)
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 16)
        }
        view.addView(header)

        for (unit in gameController.allUnits()) {
            val teamLabel = if (unit.team == Team.PLAYER) "гравець" else "ворог"
            val line = TextView(context).apply {
                text = "${unit.mech.mechClass.displayName} ($teamLabel) — " +
                    "HP: ${unit.mech.currentHp}/${unit.mech.maxHp}, синхр.: ${unit.pilot.syncRate}%"
                textSize = 14f
                setTextColor(Color.DKGRAY)
                setPadding(0, 8, 0, 8)
            }
            view.addView(line)
        }
    }

    private fun buildHeaderText(gameController: GameController): String {
        val selectionNote = gameController.selectedUnit?.let {
            " Обрано: ${it.mech.mechClass.displayName} — зелена клітинка = рух, помаранчева = атака."
        } ?: ""
        val inspectionNote = gameController.inspectedEnemy?.let {
            " Розвідка: ${it.mech.mechClass.displayName} (ворог), урон ${it.mech.damage}, " +
                "дальність атаки ${it.mech.mechClass.attackRange} — фіолетові клітинки = куди дотягнеться наступного ходу."
        } ?: ""
        return gameController.statusMessage + selectionNote + inspectionNote
    }
}
