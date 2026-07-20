package com.shadow.vhch.engine

class Board(
    private val terrainMap: Map<Position, Terrain> = emptyMap()
) {
    private val units: MutableMap<Position, ArkUnit> = mutableMapOf()

    fun terrainAt(pos: Position): Terrain = terrainMap[pos] ?: Terrain.PLAIN

    fun unitAt(pos: Position): ArkUnit? = units[pos]

    fun placeUnit(unit: ArkUnit) {
        units[unit.position] = unit
    }

    fun removeUnit(unit: ArkUnit) {
        units.remove(unit.position)
    }

    fun moveUnit(unit: ArkUnit, to: Position) {
        units.remove(unit.position)
        unit.position = to
        units[to] = unit
    }

    fun isOccupied(pos: Position): Boolean = units.containsKey(pos)

    fun allUnits(): List<ArkUnit> = units.values.toList()

    fun aliveUnits(team: Team): List<ArkUnit> = units.values.filter { it.team == team && it.isAlive }
}
