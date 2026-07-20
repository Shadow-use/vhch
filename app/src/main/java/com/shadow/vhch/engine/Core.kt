package com.shadow.vhch.engine

/** Координата на полі 8x8. (0,0) — лівий нижній кут. */
data class Position(val x: Int, val y: Int) {
    fun isOnBoard(): Boolean = x in 0..7 && y in 0..7

    operator fun plus(other: Position): Position = Position(x + other.x, y + other.y)

    fun distanceTo(other: Position): Int {
        // Чебишевська відстань — зручна для сітки, де діагональ рахується як 1 крок
        return maxOf(kotlin.math.abs(x - other.x), kotlin.math.abs(y - other.y))
    }
}

enum class Terrain(val moveCost: Int, val defenseBonus: Int) {
    PLAIN(moveCost = 1, defenseBonus = 0),
    RUINS(moveCost = 2, defenseBonus = 2),
    FOREST(moveCost = 1, defenseBonus = 1)
}

/** Тип руху визначає, як генерується список доступних клітинок для класу меха. */
enum class MovePattern {
    SLIDING_STRAIGHT,   // як тура: по прямій, поки не впреться в перешкоду
    SLIDING_DIAGONAL,   // як слон
    KNIGHT_JUMP,        // Г-подібний стрибок, ігнорує перешкоди на шляху
    SINGLE_STEP         // до moveRange клітин у будь-якому з 8 напрямків, блокується юнітами на шляху
}

enum class MechClass(
    val displayName: String,
    val movePattern: MovePattern,
    val moveRange: Int,
    val attackRange: Int,
    val baseDamage: Int,
    val baseArmor: Int,
    val baseHp: Int,
    val canMoveAndAttackSameTurn: Boolean = true
) {
    RECON(
        displayName = "Recon",
        movePattern = MovePattern.SLIDING_DIAGONAL,
        moveRange = 3,
        attackRange = 1,
        baseDamage = 4,
        baseArmor = 1,
        baseHp = 12
    ),
    STRIKER(
        displayName = "Striker",
        movePattern = MovePattern.KNIGHT_JUMP,
        moveRange = 1, // для стрибка range не використовується, беремо фіксований патерн
        attackRange = 1,
        baseDamage = 7,
        baseArmor = 2,
        baseHp = 14
    ),
    GUARDIAN(
        displayName = "Guardian",
        movePattern = MovePattern.SINGLE_STEP,
        moveRange = 1,
        attackRange = 1,
        baseDamage = 5,
        baseArmor = 5,
        baseHp = 22
    ),
    ARTILLERY(
        displayName = "Artillery",
        movePattern = MovePattern.SLIDING_STRAIGHT,
        moveRange = 2,
        attackRange = 3,
        baseDamage = 8,
        baseArmor = 1,
        baseHp = 10,
        canMoveAndAttackSameTurn = false // якщо стріляє — не рухається цей хід, і навпаки
    ),
    VANGUARD(
        displayName = "Vanguard",
        movePattern = MovePattern.SINGLE_STEP,
        moveRange = 2,
        attackRange = 1,
        baseDamage = 6,
        baseArmor = 3,
        baseHp = 18
    )
}
