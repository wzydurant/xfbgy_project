package com.xfbgy.hexmap.data

/**
 * 地形类型枚举
 *
 * @param chineseName 中文名称
 * @param colorHex 颜色Hex值
 * @param movementCost 移动力消耗
 */
enum class TerrainType(
    val chineseName: String,
    val colorHex: String,
    val movementCost: Int
) {
    PLAIN("平原", "#A8D5A2", 1),
    FOREST("树林", "#2D6A4F", 1),
    HILL("山地", "#8B5E3C", 2),
    MOUNTAIN("高山", "#222222", 20),
    URBAN("建筑群", "#9E9E9E", 1);

    companion object {
        /**
         * 根据索引获取地形类型
         */
        fun fromOrdinal(ordinal: Int): TerrainType {
            return entries.getOrElse(ordinal) { PLAIN }
        }
    }
}