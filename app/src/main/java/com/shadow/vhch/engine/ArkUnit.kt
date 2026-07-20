package com.shadow.vhch.engine

enum class Team { PLAYER, ENEMY }

class ArkUnit(
    val id: String,
    val team: Team,
    val pilot: Pilot,
    val mech: Mech,
    var position: Position
) {
    val isAlive: Boolean get() = !mech.isDestroyed

    /** Ефективна атака = базовий урон меха + бонус точності пілота (спрощена детермінована модель). */
    fun effectiveAttack(): Int = mech.damage + pilot.stats.accuracy

    /** Ефективний захист = броня меха + ухилення пілота + бонус місцевості (додається окремо на Board). */
    fun effectiveDefense(): Int = mech.armor + pilot.stats.evasion
}
