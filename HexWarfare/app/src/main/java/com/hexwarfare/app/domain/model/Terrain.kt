package com.hexwarfare.app.domain.model

/**
 * 地形类型枚举
 */
enum class TerrainType(
    val moveCost: Int,
    val visionBonus: Int,
    val defenseBonus: Int
) {
    PLAINS(1, 0, 0),      // 平原
    MOUNTAIN(3, 2, 3),   // 山地
    RIVER(2, 0, 0),      // 河流
    BUILDING(1, 1, 2),   // 建筑群
    IMPASSABLE(Int.MAX_VALUE, 0, 0); // 不可通行

    fun canPass(): Boolean = this != IMPASSABLE
}

/**
 * 资源点类型枚举
 */
enum class ResourceType {
    VILLAGE,   // 村庄
    TOWN,      // 城镇
    CITY,      // 城市
    STABLE     // 马场
}
