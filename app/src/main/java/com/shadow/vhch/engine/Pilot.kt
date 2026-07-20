package com.shadow.vhch.engine

data class PilotStats(
    val accuracy: Int,     // додається до шансу/точності влучання (в детермінованій моделі — до damage або до ухилення цілі)
    val evasion: Int,      // знижує ефективний урон по цьому пілоту
    val willpower: Int,    // швидкість росту sync rate
    val level: Int = 1,
    val experience: Int = 0
)

/** Разова здібність, що активується при заповненні sync rate до 100. */
data class PilotAbility(
    val name: String,
    val description: String,
    val damageBonus: Int = 0,
    val healAmount: Int = 0,
    val extraMoveRange: Int = 0
)

class Pilot(
    val id: String,
    val name: String,
    var stats: PilotStats,
    val ability: PilotAbility,
    var syncRate: Int = 0
) {
    companion object {
        const val SYNC_MAX = 100
    }

    fun gainSync(amount: Int) {
        syncRate = (syncRate + amount).coerceIn(0, SYNC_MAX)
    }

    fun loseSync(amount: Int) {
        syncRate = (syncRate - amount).coerceIn(0, SYNC_MAX)
    }

    fun isAbilityReady(): Boolean = syncRate >= SYNC_MAX

    /** Викликається після використання здібності — скидає шкалу. */
    fun consumeAbility() {
        syncRate = 0
    }
}
