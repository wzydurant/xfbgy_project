package com.xfbgy.hexmap.generation

import com.xfbgy.hexmap.HexMap
import com.xfbgy.hexmap.data.FortType
import com.xfbgy.hexmap.data.TerrainType
import kotlin.random.Random

/**
 * 地形生成器
 * 采用比例随机 + BFS聚类平滑策略
 */
class TerrainGenerator(private var seed: Long = System.currentTimeMillis()) {

    /** 目标地形比例 */
    private val targetProportions = mapOf(
        TerrainType.PLAIN to 0.40f,
        TerrainType.FOREST to 0.30f,
        TerrainType.HILL to 0.15f,
        TerrainType.MOUNTAIN to 0.05f,
        TerrainType.URBAN to 0.10f
    )

    /**
     * 生成地形
     * @param hexMap 目标地图
     * @param seed 随机种子（可选，用于可复现生成）
     */
    fun generateTerrain(hexMap: HexMap, seed: Long? = null) {
        val random = Random(seed ?: this.seed)
        val totalCells = hexMap.width * hexMap.height

        // Step 1: 按比例预分配各地形数量
        val terrainCounts = mutableMapOf<TerrainType, Int>()
        var remaining = totalCells

        for ((terrain, proportion) in targetProportions) {
            if (terrain == TerrainType.MOUNTAIN || terrain == TerrainType.URBAN) {
                // 高山和建筑群保持精确比例
                terrainCounts[terrain] = (totalCells * proportion).toInt()
                remaining -= terrainCounts[terrain]!!
            }
        }

        // 剩余格子按平原:树林:山地 = 40:30:15 比例分配
        val plainForestHillTotal = targetProportions[TerrainType.PLAIN]!! +
                targetProportions[TerrainType.FOREST]!! +
                targetProportions[TerrainType.HILL]!!
        terrainCounts[TerrainType.PLAIN] = ((remaining * targetProportions[TerrainType.PLAIN]!! / plainForestHillTotal)).toInt()
        terrainCounts[TerrainType.FOREST] = ((remaining * targetProportions[TerrainType.FOREST]!! / plainForestHillTotal)).toInt()
        terrainCounts[TerrainType.HILL] = remaining - terrainCounts[TerrainType.PLAIN]!! - terrainCounts[TerrainType.FOREST]!!

        // Step 2: 随机放置地形种子
        val terrainCells = mutableMapOf<TerrainType, MutableList<Pair<Int, Int>>>()

        for ((terrain, count) in terrainCounts) {
            terrainCells[terrain] = mutableListOf()
            repeat(count) {
                var x: Int
                var y: Int
                do {
                    x = random.nextInt(hexMap.width)
                    y = random.nextInt(hexMap.height)
                } while (terrainCells[terrain]!!.contains(Pair(x, y)))
                terrainCells[terrain]!!.add(Pair(x, y))
            }
        }

        // Step 3: BFS扩散形成地形团块
        val assigned = mutableSetOf<Pair<Int, Int>>()

        for ((terrain, seeds) in terrainCells) {
            for ((sx, sy) in seeds) {
                if (!assigned.contains(Pair(sx, sy))) {
                    bfsExpand(hexMap, sx, sy, terrain, random, assigned)
                }
            }
        }

        // Step 4: 填充未分配格子（使用平原）
        for (x in 0 until hexMap.width) {
            for (y in 0 until hexMap.height) {
                if (!assigned.contains(Pair(x, y))) {
                    hexMap.getCell(x, y)!!.terrain = TerrainType.PLAIN
                }
            }
        }

        // Step 5: 建筑群特殊处理：6条边统一工事
        applyUrbanFortification(hexMap, random)
    }

    /**
     * BFS扩散填充地形
     */
    private fun bfsExpand(
        hexMap: HexMap,
        startX: Int,
        startY: Int,
        terrain: TerrainType,
        random: Random,
        assigned: MutableSet<Pair<Int, Int>>
    ) {
        val queue = ArrayDeque<Pair<Int, Int>>()
        queue.add(Pair(startX, startY))

        val maxClusterSize = when (terrain) {
            TerrainType.MOUNTAIN -> 5
            TerrainType.URBAN -> 8
            else -> 30
        }

        while (queue.isNotEmpty()) {
            val (x, y) = queue.removeFirst()

            if (assigned.contains(Pair(x, y))) continue
            if (x !in 0 until hexMap.width || y !in 0 until hexMap.height) continue

            // 高山限制团块大小
            if (terrain == TerrainType.MOUNTAIN && assigned.count { it.first == x } >= maxClusterSize) {
                continue
            }

            // 建筑群不与高山相邻
            if (terrain == TerrainType.URBAN) {
                val neighbors = hexMap.getNeighbors(x, y)
                if (neighbors.any { it.terrain == TerrainType.MOUNTAIN }) {
                    continue
                }
            }

            assigned.add(Pair(x, y))
            hexMap.getCell(x, y)!!.terrain = terrain

            // 随机决定是否向邻居扩散
            if (random.nextFloat() < 0.7f) {
                val neighbors = hexMap.getNeighbors(x, y)
                for (neighbor in neighbors) {
                    if (!assigned.contains(Pair(neighbor.x, neighbor.y))) {
                        queue.add(Pair(neighbor.x, neighbor.y))
                    }
                }
            }
        }
    }

    /**
     * 建筑群6条边统一工事类型
     */
    private fun applyUrbanFortification(hexMap: HexMap, random: Random) {
        for (x in 0 until hexMap.width) {
            for (y in 0 until hexMap.height) {
                val cell = hexMap.getCell(x, y) ?: continue
                if (cell.terrain == TerrainType.URBAN) {
                    val fortType = FortType.randomFortification()
                    for (dir in 0..5) {
                        hexMap.getEdge(x, y, dir)?.fortification = fortType
                    }
                }
            }
        }
    }
}