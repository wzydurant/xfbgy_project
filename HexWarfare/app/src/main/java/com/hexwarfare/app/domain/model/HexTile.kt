package com.hexwarfare.app.domain.model

/**
 * 六角格瓦片类
 */
data class HexTile(
    val coord: HexCoord,
    val terrain: TerrainType,
    val resource: ResourceType? = null,
    val isCapital: Boolean = false
)

/**
 * 地图数据类
 */
data class GameMap(
    val width: Int,
    val height: Int,
    val tiles: Map<HexCoord, HexTile>,
    val capitalPositions: Map<Faction, HexCoord>
)
