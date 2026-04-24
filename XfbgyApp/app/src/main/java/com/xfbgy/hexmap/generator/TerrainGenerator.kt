package com.xfbgy.hexmap.generator

import com.xfbgy.hexmap.data.FortType
import com.xfbgy.hexmap.data.HexMap
import com.xfbgy.hexmap.data.TerrainType
import kotlin.random.Random

/**
 * 地形生成器
 *
 * 采用比例随机 + BFS 聚类平滑策略：
 * 1. 按比例预分配地形总数
 * 2. 随机选取种子格，使用 BFS 扩散形成地形团块
 * 3. 高山与建筑群小团块随机散布，建筑群优先不与高山相邻
 * 4. 建筑群特殊处理：6条边防御工事随机统一为同一等级
 *
 * 目标比例：平原40%, 树林30%, 山地15%, 高山5%, 建筑群10%
 */
object TerrainGenerator {

    /**
     * 地形目标比例
     */
    private val terrainProportions = mapOf(
        TerrainType.PLAIN to 0.40,
        TerrainType.FOREST to 0.30,
        TerrainType.HILL to 0.15,
        TerrainType.MOUNTAIN to 0.05,
        TerrainType.URBAN to 0.10
    )

    /**
     * 聚类扩散的概率（用于BFS扩散时控制团块大小）
     */
    private const val CLUSTER_SPREAD_PROBABILITY = 0.7

    /**
     * 为建筑群生成统一的防御工事等级（不为NONE）
     */
    private val urbanFortTypes = listOf(FortType.FENCE, FortType.EARTHWALL, FortType.STONEWALL)

    /**
     * 生成完整地图地形
     * @param hexMap 地图对象
     * @param seed 随机种子（可选，用于可重现生成）
     */
    fun generateTerrain(hexMap: HexMap, seed: Long? = null) {
        val random = if (seed != null) Random(seed) else Random

        // 计算各地形目标数量
        val totalCells = hexMap.width * hexMap.height
        val terrainCounts = calculateTerrainCounts(totalCells)

        // 创建可用的坐标列表
        val availableCells = mutableListOf<Pair<Int, Int>>()
        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                availableCells.add(Pair(x, y))
            }
        }

        // 分配高山（优先边缘散布，避免聚集）
        assignMountainTerrains(hexMap, terrainCounts[TerrainType.MOUNTAIN] ?: 0, availableCells, random)

        // 分配建筑群（不与高山相邻）
        assignUrbanTerrains(hexMap, terrainCounts[TerrainType.URBAN] ?: 0, availableCells, random)

        // 分配剩余地形（使用BFS聚类扩散）
        assignClusteredTerrains(hexMap, terrainCounts, availableCells, random)

        // 为建筑群设置防御工事
        assignUrbanFortifications(hexMap, random)
    }

    /**
     * 计算各地形目标数量
     */
    private fun calculateTerrainCounts(totalCells: Int): Map<TerrainType, Int> {
        val counts = mutableMapOf<TerrainType, Int>()
        var remaining = totalCells

        // 按优先级计算（高山和建筑群先确定）
        for (terrain in terrainProportions.keys) {
            val proportion = terrainProportions[terrain] ?: 0.0
            val count = if (terrain == TerrainType.PLAIN) {
                // 平原最后处理，使用剩余数量
                remaining
            } else {
                (totalCells * proportion).toInt().coerceAtLeast(1)
            }
            counts[terrain] = count
            remaining -= count
        }

        // 平原使用剩余数量
        counts[TerrainType.PLAIN] = remaining.coerceAtLeast(0)

        return counts
    }

    /**
     * 分配高山（边缘散布）
     */
    private fun assignMountainTerrains(
        hexMap: HexMap,
        count: Int,
        availableCells: MutableList<Pair<Int, Int>>,
        random: Random
    ) {
        if (count <= 0) return

        // 获取边缘格子
        val edgeCells = getEdgeCells(hexMap)
        val selectedCells = mutableListOf<Pair<Int, Int>>()

        // 随机选择边缘位置作为高山种子
        val shuffledEdges = edgeCells.shuffled(random)
        for (cell in shuffledEdges) {
            if (selectedCells.size >= count) break
            if (!isNearMountain(hexMap, cell.first, cell.second)) {
                selectedCells.add(cell)
            }
        }

        // 如果边缘不够，散布到内部
        if (selectedCells.size < count) {
            val internalCells = availableCells.filter { !isEdgeCell(it, hexMap) }
            for (cell in internalCells.shuffled(random)) {
                if (selectedCells.size >= count) break
                if (!isNearMountain(hexMap, cell.first, cell.second)) {
                    selectedCells.add(cell)
                }
            }
        }

        // 设置高山
        for ((x, y) in selectedCells) {
            hexMap.cells[x][y].terrain = TerrainType.MOUNTAIN
            availableCells.remove(Pair(x, y))
        }
    }

    /**
     * 分配建筑群（不与高山相邻）
     */
    private fun assignUrbanTerrains(
        hexMap: HexMap,
        count: Int,
        availableCells: MutableList<Pair<Int, Int>>,
        random: Random
    ) {
        if (count <= 0) return

        val selectedCells = mutableListOf<Pair<Int, Int>>()

        for (cell in availableCells.shuffled(random)) {
            if (selectedCells.size >= count) break
            val (x, y) = cell
            // 不与高山相邻
            if (!isAdjacentToTerrain(hexMap, x, y, TerrainType.MOUNTAIN)) {
                selectedCells.add(cell)
            }
        }

        // 设置建筑群
        for ((x, y) in selectedCells) {
            hexMap.cells[x][y].terrain = TerrainType.URBAN
            availableCells.remove(Pair(x, y))
        }
    }

    /**
     * 使用BFS聚类分配剩余地形
     */
    private fun assignClusteredTerrains(
        hexMap: HexMap,
        counts: Map<TerrainType, Int>,
        availableCells: MutableList<Pair<Int, Int>>,
        random: Random
    ) {
        // 需要分配的地形类型（按比例排序：树林>山地>平原）
        val terrainsToAssign = listOf(
            TerrainType.FOREST to (counts[TerrainType.FOREST] ?: 0),
            TerrainType.HILL to (counts[TerrainType.HILL] ?: 0),
            TerrainType.PLAIN to (counts[TerrainType.PLAIN] ?: 0)
        )

        for ((terrain, totalCount) in terrainsToAssign) {
            if (totalCount <= 0) continue

            val assigned = mutableSetOf<Pair<Int, Int>>()
            var remaining = totalCount

            // 随机选择种子点开始BFS扩散
            val seeds = availableCells.filter { it !in assigned }.shuffled(random)

            for (seed in seeds) {
                if (remaining <= 0) break
                if (seed in assigned) continue

                // BFS扩散形成团块
                val cluster = growCluster(hexMap, seed, terrain, remaining, assigned, random)
                assigned.addAll(cluster)
                remaining -= cluster.size
            }

            // 剩余的直接填充
            if (remaining > 0) {
                val unassigned = availableCells.filter { it !in assigned }
                for (cell in unassigned.shuffled(random)) {
                    if (remaining <= 0) break
                    hexMap.cells[cell.first][cell.second].terrain = terrain
                    assigned.add(cell)
                    remaining--
                }
            }
        }
    }

    /**
     * BFS扩散生长团块
     */
    private fun growCluster(
        hexMap: HexMap,
        start: Pair<Int, Int>,
        terrain: TerrainType,
        maxSize: Int,
        assigned: Set<Pair<Int, Int>>,
        random: Random
    ): List<Pair<Int, Int>> {
        val cluster = mutableListOf<Pair<Int, Int>>()
        val queue = ArrayDeque<Pair<Int, Int>>()
        queue.add(start)

        while (queue.isNotEmpty() && cluster.size < maxSize) {
            val current = queue.removeFirst()
            if (!hexMap.isValidCell(current.first, current.second)) continue
            if (current in assigned) continue
            if (current in cluster) continue

            // 添加到团块
            cluster.add(current)
            hexMap.cells[current.first][current.second].terrain = terrain

            // 探索相邻格子
            val neighbors = hexMap.getNeighborCoords(current.first, current.second)
            for (neighbor in neighbors) {
                if (!hexMap.isValidCell(neighbor.first, neighbor.second)) continue
                if (neighbor in assigned) continue
                if (neighbor in cluster) continue
                // 按概率决定是否扩展
                if (random.nextFloat() < CLUSTER_SPREAD_PROBABILITY) {
                    queue.add(neighbor)
                }
            }
        }

        return cluster
    }

    /**
     * 为建筑群设置防御工事（6条边统一类型）
     */
    private fun assignUrbanFortifications(hexMap: HexMap, random: Random) {
        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                if (hexMap.cells[x][y].terrain == TerrainType.URBAN) {
                    // 随机选择一种非NONE的工事类型
                    val fortType = urbanFortTypes.random(random)
                    // 应用到所有6条边
                    for (dir in 0..5) {
                        hexMap.setFortification(x, y, dir, fortType)
                    }
                }
            }
        }
    }

    /**
     * 检查是否与高山相邻
     */
    private fun isNearMountain(hexMap: HexMap, x: Int, y: Int): Boolean {
        return isAdjacentToTerrain(hexMap, x, y, TerrainType.MOUNTAIN)
    }

    /**
     * 检查是否与指定地形相邻
     */
    private fun isAdjacentToTerrain(hexMap: HexMap, x: Int, y: Int, terrain: TerrainType): Boolean {
        val neighbors = hexMap.getNeighbors(x, y)
        return neighbors.any { it.terrain == terrain }
    }

    /**
     * 获取地图边缘格子
     */
    private fun getEdgeCells(hexMap: HexMap): List<Pair<Int, Int>> {
        val edgeCells = mutableListOf<Pair<Int, Int>>()
        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                if (isEdgeCell(Pair(x, y), hexMap)) {
                    edgeCells.add(Pair(x, y))
                }
            }
        }
        return edgeCells
    }

    /**
     * 判断是否为边缘格子
     */
    private fun isEdgeCell(cell: Pair<Int, Int>, hexMap: HexMap): Boolean {
        val (x, y) = cell
        return x == 0 || x == hexMap.width - 1 || y == 0 || y == hexMap.height - 1
    }
}