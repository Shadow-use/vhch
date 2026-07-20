package com.shadow.vhch.engine

data class MechUpgrades(
    val armorBonus: Int = 0,
    val damageBonus: Int = 0,
    val hpBonus: Int = 0
)

class Mech(
    val id: String,
    val mechClass: MechClass,
    val upgrades: MechUpgrades = MechUpgrades()
) {
    var currentHp: Int = mechClass.baseHp + upgrades.hpBonus
        private set

    val maxHp: Int get() = mechClass.baseHp + upgrades.hpBonus
    val armor: Int get() = mechClass.baseArmor + upgrades.armorBonus
    val damage: Int get() = mechClass.baseDamage + upgrades.damageBonus

    val isDestroyed: Boolean get() = currentHp <= 0

    fun applyDamage(amount: Int) {
        currentHp = (currentHp - amount).coerceAtLeast(0)
    }

    fun repair(amount: Int) {
        currentHp = (currentHp + amount).coerceAtMost(maxHp)
    }
}
