package com.xfbgy.hexmap.generator

import com.xfbgy.hexmap.data.AttributeCalculator
import com.xfbgy.hexmap.data.HexMap

/**
 * 地图生成器整合类
 *
 * 整合地形生成器和河流生成器，提供完整的地图生成流程：
 * 1. 生成地形
 * 2. 生成河流
 * 3. 计算所有属性
 */
object MapGenerator {

    /**
     * 生成完整地图
     *
     * @param width 地图宽度（20-40）
     * @param height 地图高度（20-40）
     * @param seed 随机种子（可选）
     * @return 生成的 HexMap 实例
     */
    fun generateMap(width: Int = 30, height: Int = 30, seed: Long? = null): HexMap {
        // 创建地图
        val hexMap = HexMap(width, height)

        // 生成地形
        TerrainGenerator.generateTerrain(hexMap, seed)

        // 生成河流（带重试机制）
        RiverGenerator.generateRivers(hexMap, seed = seed)

        // 计算所有属性
        AttributeCalculator.calculateAll(hexMap)

        return hexMap
    }

    /**
     * 生成仅包含地形的地图（无河流）
     */
    fun generateTerrainOnly(width: Int = 30, height: Int = 30, seed: Long? = null): HexMap {
        val hexMap = HexMap(width, height)
        TerrainGenerator.generateTerrain(hexMap, seed)
        AttributeCalculator.calculateAll(hexMap)
        return hexMap
    }

    /**
     * 重新生成河流并计算属性
     */
    fun regenerateRivers(hexMap: HexMap, seed: Long? = null): Boolean {
        val success = RiverGenerator.generateRivers(hexMap, seed = seed)
        if (success) {
            AttributeCalculator.calculateAll(hexMap)
        }
        return success
    }
}