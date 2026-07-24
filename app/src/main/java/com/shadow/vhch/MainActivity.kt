package com.shadow.vhch

import android.app.Activity
import android.os.Bundle
import com.shadow.vhch.ui.GameScreenConductor

/**
 * Точка входу застосунку. Нічого сама не малює і нічого не рахує —
 * тільки створює диригента екрана (GameScreenConductor) і показує його вигляд.
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val conductor = GameScreenConductor(this)
        setContentView(conductor.buildRootView())
    }
}
