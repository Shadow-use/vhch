package com.shadow.vhch.engine

object Directions {
    val STRAIGHT = listOf(Position(1, 0), Position(-1, 0), Position(0, 1), Position(0, -1))
    val DIAGONAL = listOf(Position(1, 1), Position(1, -1), Position(-1, 1), Position(-1, -1))
    val ALL_EIGHT = STRAIGHT + DIAGONAL
    val KNIGHT_OFFSETS = listOf(
        Position(1, 2), Position(2, 1), Position(2, -1), Position(1, -2),
        Position(-1, -2), Position(-2, -1), Position(-2, 1), Position(-1, 2)
    )
}

/** Результат одного удару — зручно для UI: скільки урону, чи знищено ціль, чи спрацювала здібність. */
data class AttackResult(
    val damageDealt: Int,
    val targetDestroyed: Boolean,
    val attackerAbilityTriggered: Boolean
)

class CombatEngine(private val board: Board) {

    /** Усі клітини, куди юніт може переміститись цього ходу (без урахування атаки). */
    fun availableMoves(unit: ArkUnit): List<Position> {
        val mechClass = unit.mech.mechClass
        val result = mutableListOf<Position>()

        when (mechClass.movePattern) {
            MovePattern.KNIGHT_JUMP -> {
                for (offset in Directions.KNIGHT_OFFSETS) {
                    val dest = unit.position + offset
                    if (dest.isOnBoard() && !board.isOccupied(dest)) {
                        result.add(dest)
                    }
                }
            }
            MovePattern.SLIDING_STRAIGHT -> addSlidingMoves(unit, Directions.STRAIGHT, mechClass.moveRange, result)
            MovePattern.SLIDING_DIAGONAL -> addSlidingMoves(unit, Directions.DIAGONAL, mechClass.moveRange, result)
            MovePattern.SINGLE_STEP -> addSlidingMoves(unit, Directions.ALL_EIGHT, mechClass.moveRange, result)
        }
        return result
    }

    private fun addSlidingMoves(
        unit: ArkUnit,
        directions: List<Position>,
        range: Int,
        result: MutableList<Position>
    ) {
        for (dir in directions) {
            var current = unit.position
            for (step in 1..range) {
                current += dir
                if (!current.isOnBoard()) break
                if (board.isOccupied(current)) break // перешкода — далі в цьому напрямку йти не можна
                result.add(current)
            }
        }
    }

    /** Юніти супротивника, доступні для атаки з поточної позиції (за attackRange, чебишевська відстань). */
    fun availableTargets(unit: ArkUnit): List<ArkUnit> {
        val range = unit.mech.mechClass.attackRange
        return board.aliveUnits(if (unit.team == Team.PLAYER) Team.ENEMY else Team.PLAYER)
            .filter { unit.position.distanceTo(it.position) <= range }
    }

    /**
     * Виконує атаку. Урон детермінований: атака - захист (з урахуванням місцевості цілі), мінімум 1.
     * Атакуючий отримує sync rate за влучання, ціль втрачає sync rate за отримання критичного удару (>= 30% max HP за раз).
     */
    fun resolveAttack(attacker: ArkUnit, target: ArkUnit): AttackResult {
        val terrainDefense = board.terrainAt(target.position).defenseBonus
        val abilityReady = attacker.pilot.isAbilityReady()
        val abilityDamageBonus = if (abilityReady) attacker.pilot.ability.damageBonus else 0

        val rawDamage = attacker.effectiveAttack() + abilityDamageBonus - (target.effectiveDefense() + terrainDefense)
        val damage = rawDamage.coerceAtLeast(1)

        target.mech.applyDamage(damage)
        attacker.pilot.gainSync(10 + attacker.pilot.stats.willpower / 2)

        val isCritical = damage >= target.mech.maxHp * 0.3
        if (isCritical) {
            target.pilot.loseSync(15)
        }

        if (abilityReady) {
            attacker.mech.repair(attacker.pilot.ability.healAmount)
            attacker.pilot.consumeAbility()
        }

        val destroyed = target.mech.isDestroyed
        if (destroyed) {
            board.removeUnit(target)
        }

        return AttackResult(
            damageDealt = damage,
            targetDestroyed = destroyed,
            attackerAbilityTriggered = abilityReady
        )
    }

    /** Перевірка умови перемоги: чи залишились живі юніти у команди. */
    fun isTeamDefeated(team: Team): Boolean = board.aliveUnits(team).isEmpty()

    /**
     * Усі клітинки, куди цей юніт може дотягнутись атакою наступного ходу
     * (з поточної позиції або будь-якої клітинки, куди він може дійти).
     * Використовується для показу гравцю "зони загрози" ворога перед його ходом.
     */
    fun threatRange(unit: ArkUnit): Set<Position> {
        val reachablePositions = availableMoves(unit) + unit.position
        val range = unit.mech.mechClass.attackRange
        val result = mutableSetOf<Position>()

        for (origin in reachablePositions) {
            for (x in 0..7) {
                for (y in 0..7) {
                    val candidate = Position(x, y)
                    if (origin.distanceTo(candidate) <= range) {
                        result.add(candidate)
                    }
                }
            }
        }
        return result
    }
}
