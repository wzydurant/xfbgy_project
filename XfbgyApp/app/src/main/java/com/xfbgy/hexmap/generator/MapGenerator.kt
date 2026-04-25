package com.xfbgy.hexmap.generator

import com.xfbgy.hexmap.data.AttributeCalculator
import com.xfbgy.hexmap.data.FortType
import com.xfbgy.hexmap.data.HexMap
import com.xfbgy.hexmap.data.TerrainType

/**
 * 地图生成器整合类
 *
 * 整合地形生成器和河流生成器，提供完整的地图生成流程：
 * 1. 生成地形
 * 2. 生成河流
 * 3. 计算所有属性
 * 4. 最终验证和修复
 */
object MapGenerator {

    /**
     * 生成完整地图
     */
    fun generateMap(width: Int = 30, height: Int = 30, seed: Long? = null): HexMap {
        val hexMap = HexMap(width, height)

        // 先清除边
        hexMap.clearAllEdges()

        // 生成地形（会设置防御工事和基础边效果）
        TerrainGenerator.generateTerrain(hexMap, seed)

        // 生成河流（会设置河流标记）
        RiverGenerator.generateRivers(hexMap, seed = seed)

        // 计算所有效果（不清除河流和防御工事，只叠加计算）
        AttributeCalculator.calculateEffects(hexMap)

        // 最终验证和修复
        finalValidationAndFix(hexMap)

        return hexMap
    }

    /**
     * 生成仅包含地形的地图（无河流）
     */
    fun generateTerrainOnly(width: Int = 30, height: Int = 30, seed: Long? = null): HexMap {
        val hexMap = HexMap(width, height)
        hexMap.clearAllEdges()
        TerrainGenerator.generateTerrain(hexMap, seed)
        AttributeCalculator.calculateEffects(hexMap)
        finalValidationAndFix(hexMap)
        return hexMap
    }

    /**
     * 重新生成河流并计算属性
     */
    fun regenerateRivers(hexMap: HexMap, seed: Long? = null): Boolean {
        val success = RiverGenerator.generateRivers(hexMap, seed = seed)
        if (success) {
            AttributeCalculator.calculateEffects(hexMap)
        }
        finalValidationAndFix(hexMap)
        return success
    }

    /**
     * 最终验证和修复
     * 1. 清除非建筑群地形的防御工事
     * 2. 清除相邻不同建筑群之间的防御工事
     */
    private fun finalValidationAndFix(hexMap: HexMap) {
        val clusters = identifyUrbanClusters(hexMap)

        // 构建聚团ID映射
        val clusterIdMap = mutableMapOf<Pair<Int, Int>, Int>()
        clusters.forEachIndexed { index, cluster ->
            cluster.forEach { pos -> clusterIdMap[pos] = index }
        }

        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                val cell = hexMap.cells[x][y]
                val cellPos = Pair(x, y)
                val currentClusterId = clusterIdMap[cellPos]
                val neighbors = hexMap.getNeighborCoords(x, y)

                for (dir in 0..5) {
                    val edge = cell.edges[dir]
                    val neighbor = neighbors.getOrNull(dir) ?: continue
                    val (nx, ny) = neighbor

                    // 检查1：非建筑群格子不应该有防御工事
                    if (cell.terrain != TerrainType.URBAN) {
                        if (edge.fortification != FortType.NONE) {
                            edge.fortification = FortType.NONE
                            if (hexMap.isValidCell(nx, ny)) {
                                val oppositeDir = (dir + 3) % 6
                                hexMap.cells[nx][ny].edges[oppositeDir].fortification = FortType.NONE
                            }
                        }
                        continue
                    }

                    // 检查2：相邻不同建筑群之间不应该有防御工事
                    if (currentClusterId != null && hexMap.isValidCell(nx, ny) &&
                        hexMap.cells[nx][ny].terrain == TerrainType.URBAN) {
                        val neighborClusterId = clusterIdMap[neighbor]
                        if (neighborClusterId != null && neighborClusterId != currentClusterId) {
                            if (edge.fortification != FortType.NONE) {
                                edge.fortification = FortType.NONE
                                val oppositeDir = (dir + 3) % 6
                                hexMap.cells[nx][ny].edges[oppositeDir].fortification = FortType.NONE
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 识别所有建筑群聚团
     */
    private fun identifyUrbanClusters(hexMap: HexMap): List<List<Pair<Int, Int>>> {
        val visited = mutableSetOf<Pair<Int, Int>>()
        val clusters = mutableListOf<List<Pair<Int, Int>>>()

        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                val pos = Pair(x, y)
                if (pos in visited) continue
                if (hexMap.cells[x][y].terrain != TerrainType.URBAN) continue

                val cluster = mutableListOf<Pair<Int, Int>>()
                val queue = ArrayDeque<Pair<Int, Int>>()
                queue.add(pos)

                while (queue.isNotEmpty()) {
                    val current = queue.removeFirst()
                    if (current in visited) continue
                    if (!hexMap.isValidCell(current.first, current.second)) continue
                    if (hexMap.cells[current.first][current.second].terrain != TerrainType.URBAN) continue

                    visited.add(current)
                    cluster.add(current)

                    val neighbors = hexMap.getNeighborCoords(current.first, current.second)
                    for (neighbor in neighbors) {
                        if (neighbor !in visited) {
                            queue.add(neighbor)
                        }
                    }
                }

                if (cluster.isNotEmpty()) {
                    clusters.add(cluster)
                }
            }
        }

        return clusters
    }
}
