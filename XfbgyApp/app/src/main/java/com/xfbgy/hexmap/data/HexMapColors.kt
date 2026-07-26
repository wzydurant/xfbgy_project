package com.xfbgy.hexmap.data

/**
 * 颜色常量定义
 * 参考文档：十、参考颜色规范
 */
object HexMapColors {
    // 地形颜色
    const val PLAIN = "#A8D5A2"       // 浅绿色 - 平原
    const val FOREST = "#2D6A4F"      // 深绿色 - 树林
    const val HILL = "#8B5E3C"        // 褐色 - 山地
    const val MOUNTAIN = "#222222"    // 黑色 - 高山
    const val URBAN = "#9E9E9E"       // 灰色 - 建筑群

    // 边界线颜色
    const val BORDER = "#333333"       // 黑色细线 - 边界

    // 河流颜色
    const val RIVER = "#1E90FF"       // 蓝色粗线 - 河流

    // 防御工事颜色
    const val FENCE = "#8B5E3C"       // 褐色小叉线 - 栅栏
    const val EARTHWALL = "#757575"   // 灰色小叉线 - 土墙
    const val STONEWALL = "#FFFFFF"   // 白色小叉线 - 石墙

    /**
     * 获取地形对应的颜色值
     */
    fun getTerrainColor(terrain: TerrainType): String {
        return when (terrain) {
            TerrainType.PLAIN -> PLAIN
            TerrainType.FOREST -> FOREST
            TerrainType.HILL -> HILL
            TerrainType.MOUNTAIN -> MOUNTAIN
            TerrainType.URBAN -> URBAN
        }
    }

    /**
     * 获取工事对应的颜色值
     */
    fun getFortColor(fort: FortType): String {
        return when (fort) {
            FortType.NONE -> ""
            FortType.FENCE -> FENCE
            FortType.EARTHWALL -> EARTHWALL
            FortType.STONEWALL -> STONEWALL
        }
    }
}