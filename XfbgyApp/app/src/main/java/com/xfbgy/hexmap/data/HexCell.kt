package com.xfbgy.hexmap.data

import com.xfbgy.hexmap.data.FortType
import com.xfbgy.hexmap.data.TerrainType
import com.xfbgy.hexmap.data.Unit as GameUnit

/**
 * 六角格内部属性数据类
 *
 * @param x 列坐标
 * @param y 行坐标
 * @param terrain 地形枚举
 * @param units 单位列表（后续模块预留）
 * @param movementCost 移动力消耗（叠加计算）
 * @param zoc ZOC位掩码：bit0=阵营1, bit1=阵营2, bit2=阵营3
 */
data class HexCell(
    val x: Int,
    val y: Int,
    var terrain: TerrainType,
    var units: MutableList<GameUnit> = mutableListOf(),
    var movementCost: Int = 0,
    var zoc: Int = 0b000
) {
    /**
     * 查询某格是否被某阵营控制
     * @param faction 阵营编号（1~8）
     * @return 是否被控制
     */
    fun isControlledBy(faction: Int): Boolean {
        require(faction in 1..8) { "阵营编号必须在1~8之间" }
        return (zoc and (1 shl (faction - 1))) != 0
    }

    /**
     * 标记某格被某阵营控制
     * @param faction 阵营编号（1~8）
     */
    fun setZOC(faction: Int) {
        require(faction in 1..8) { "阵营编号必须在1~8之间" }
        zoc = zoc or (1 shl (faction - 1))
    }

    /**
     * 清除某格某阵营的ZOC
     * @param faction 阵营编号（1~8）
     */
    fun clearZOC(faction: Int) {
        require(faction in 1..8) { "阵营编号必须在1~8之间" }
        zoc = zoc and (1 shl (faction - 1)).inv()
    }

    /**
     * 获取ZOC描述
     * @return ZOC状态描述字符串
     */
    fun getZOCDescription(): String {
        if (zoc == 0) return "无控制"
        val descriptions = mutableListOf<String>()
        for (i in 1..8) {
            if (isControlledBy(i)) {
                descriptions.add("阵营$i")
            }
        }
        return descriptions.joinToString("、")
    }

    /**
     * 清除所有ZOC
     */
    fun clearAllZOC() {
        zoc = 0b000
    }

    /**
     * 应用地形效果到6条边
     * @param edges 6条边的数组
     */
    fun applyTerrainEffects(edges: Array<HexEdge>) {
        when (terrain) {
            TerrainType.PLAIN -> {
                movementCost = 1
            }
            TerrainType.FOREST -> {
                movementCost = 1
                for (edge in edges) {
                    edge.movementPenalty += 1
                    edge.defenseConditions.addAll(listOf("骑兵", "对攻", "远射", "炮击"))
                }
            }
            TerrainType.HILL -> {
                movementCost = 2
                for (edge in edges) {
                    edge.movementPenalty += 1
                    edge.attackBonus += 1
                    edge.defenseBonus += 1
                }
            }
            TerrainType.MOUNTAIN -> {
                movementCost = 20
            }
            TerrainType.URBAN -> {
                movementCost = 1
                for (edge in edges) {
                    edge.movementPenalty += 1
                    edge.defenseConditions.addAll(listOf("骑兵", "远射"))
                }
            }
        }
    }
}