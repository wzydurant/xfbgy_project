package com.xfbgy.hexmap.generator

import com.xfbgy.hexmap.data.FortType
import com.xfbgy.hexmap.data.HexMap
import com.xfbgy.hexmap.data.TerrainType
import kotlin.math.min
import kotlin.random.Random

/**
 * 地形生成器
 * 
 * 防御工事生成规则：
 * 1. 为每个建筑群聚团设置统一的防御工事类型
 *    - 孤立格子（1格聚团）：栅栏或土墙，比例2:1
 *    - 多格聚团：石墙
 * 2. 每个建筑群格子六面全包围防御（6条边都设置防御工事）
 * 3. 不同聚团相邻的边：防御工事等级降为"无"
 * 4. 同一聚团内相邻的边：防御工事等级降为"无"
 */
object TerrainGenerator {

    /**
     * 地形目标比例
     * 平原58.5%, 树林15%, 山地7.5%, 高山5%, 建筑群3.3%
     */
    private val terrainProportions = mapOf(
        TerrainType.PLAIN to 0.585f,
        TerrainType.FOREST to 0.15f,
        TerrainType.HILL to 0.075f,
        TerrainType.MOUNTAIN to 0.05f,
        TerrainType.URBAN to 0.033f
    )

    /**
     * 高山团块大小范围
     */
    private const val MOUNTAIN_CLUSTER_MIN = 2
    private const val MOUNTAIN_CLUSTER_MAX = 5

    /**
     * 建筑群团块大小范围
     */
    private const val URBAN_CLUSTER_MIN = 1
    private const val URBAN_CLUSTER_MAX = 3

    /**
     * 目标建筑群聚团数量范围
     */
    private const val MIN_URBAN_CLUSTERS = 8
    private const val MAX_URBAN_CLUSTERS = 15

    /**
     * 山地/森林团块大小范围
     */
    private const val TERRAIN_CLUSTER_MIN = 2
    private const val TERRAIN_CLUSTER_MAX = 10

    /**
     * 聚团概率
     */
    private const val CLUSTER_PROBABILITY = 0.6f

    /**
     * 栅栏概率（2:1比例）
     */
    private const val FENCE_RATIO = 0.67f

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

        // 重置所有格子为平原
        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                hexMap.cells[x][y].terrain = TerrainType.PLAIN
            }
        }

        // 第一步：分配高山（整个地图随机分布，2-5格小聚团）
        assignDistributedTerrains(hexMap, TerrainType.MOUNTAIN, terrainCounts[TerrainType.MOUNTAIN] ?: 0,
            MOUNTAIN_CLUSTER_MIN, MOUNTAIN_CLUSTER_MAX, random)

        // 第二步：分配建筑群（先分配，再检查调整）
        val initialUrbanCount = terrainCounts[TerrainType.URBAN] ?: 0
        assignInitialUrbanTerrains(hexMap, initialUrbanCount, random)

        // 第三步：分配山地和森林
        assignDistributedTerrains(hexMap, TerrainType.FOREST, terrainCounts[TerrainType.FOREST] ?: 0,
            TERRAIN_CLUSTER_MIN, TERRAIN_CLUSTER_MAX, random)
        assignDistributedTerrains(hexMap, TerrainType.HILL, terrainCounts[TerrainType.HILL] ?: 0,
            TERRAIN_CLUSTER_MIN, TERRAIN_CLUSTER_MAX, random)

        // 第四步：检查并调整建筑群聚团数量
        adjustUrbanClusters(hexMap, random)

        // 第五步：为建筑群设置防御工事
        assignUrbanFortifications(hexMap, random)
    }

    /**
     * 计算各地形目标数量
     */
    private fun calculateTerrainCounts(totalCells: Int): Map<TerrainType, Int> {
        val counts = mutableMapOf<TerrainType, Int>()
        var remaining = totalCells

        for (terrain in terrainProportions.keys) {
            val proportion = terrainProportions[terrain] ?: 0.0f
            val count = if (terrain == TerrainType.PLAIN) {
                remaining
            } else {
                (totalCells.toFloat() * proportion).toInt().coerceAtLeast(1)
            }
            counts[terrain] = count
            remaining -= count
        }

        counts[TerrainType.PLAIN] = remaining.coerceAtLeast(0)
        return counts
    }

    /**
     * 分配在整个地图随机分布的地形（带小规模聚团）
     */
    private fun assignDistributedTerrains(
        hexMap: HexMap,
        terrain: TerrainType,
        count: Int,
        clusterMin: Int,
        clusterMax: Int,
        random: Random
    ) {
        if (count <= 0) return

        var remaining = count

        while (remaining > 0) {
            val seedX = random.nextInt(hexMap.width)
            val seedY = random.nextInt(hexMap.height)

            if (hexMap.cells[seedX][seedY].terrain != TerrainType.PLAIN) {
                continue
            }

            val clusterSize = random.nextInt(clusterMin, clusterMax + 1)
            val actualSize = min(clusterSize, remaining)

            val cluster = growCluster(hexMap, seedX, seedY, terrain, actualSize, random)
            remaining -= cluster.size
        }
    }

    /**
     * 生长小规模聚团
     */
    private fun growCluster(
        hexMap: HexMap,
        startX: Int,
        startY: Int,
        terrain: TerrainType,
        maxSize: Int,
        random: Random
    ): List<Pair<Int, Int>> {
        val cluster = mutableListOf<Pair<Int, Int>>()
        val queue = ArrayDeque<Pair<Int, Int>>()
        val visited = mutableSetOf<Pair<Int, Int>>()
        queue.add(Pair(startX, startY))

        while (queue.isNotEmpty() && cluster.size < maxSize) {
            val current = queue.removeFirst()

            if (!hexMap.isValidCell(current.first, current.second)) continue
            if (current in visited) continue
            if (hexMap.cells[current.first][current.second].terrain != TerrainType.PLAIN) continue

            visited.add(current)
            cluster.add(current)
            hexMap.cells[current.first][current.second].terrain = terrain

            val neighbors = hexMap.getNeighborCoords(current.first, current.second)
            for (neighbor in neighbors) {
                if (!hexMap.isValidCell(neighbor.first, neighbor.second)) continue
                if (neighbor in visited) continue
                if (hexMap.cells[neighbor.first][neighbor.second].terrain != TerrainType.PLAIN) continue
                if (random.nextFloat() < CLUSTER_PROBABILITY) {
                    queue.add(neighbor)
                }
            }
        }

        return cluster
    }

    /**
     * 初始分配建筑群
     */
    private fun assignInitialUrbanTerrains(
        hexMap: HexMap,
        totalCount: Int,
        random: Random
    ) {
        if (totalCount <= 0) return

        var remaining = totalCount

        while (remaining > 0) {
            val seedX = random.nextInt(hexMap.width)
            val seedY = random.nextInt(hexMap.height)

            if (hexMap.cells[seedX][seedY].terrain != TerrainType.PLAIN) {
                continue
            }

            val clusterSize = random.nextInt(URBAN_CLUSTER_MIN, min(URBAN_CLUSTER_MAX + 1, remaining + 1))
            val actualSize = min(clusterSize, remaining)

            val cluster = growCluster(hexMap, seedX, seedY, TerrainType.URBAN, actualSize, random)
            remaining -= cluster.size
        }
    }

    /**
     * 检查并调整建筑群聚团数量
     */
    private fun adjustUrbanClusters(hexMap: HexMap, random: Random) {
        // 识别所有建筑群聚团
        val clusters = identifyUrbanClusters(hexMap)

        val currentCount = clusters.size

        if (currentCount < MIN_URBAN_CLUSTERS) {
            // 需要添加更多建筑群聚团
            val toAdd = MIN_URBAN_CLUSTERS - currentCount
            for (i in 0 until toAdd) {
                addRandomUrbanCluster(hexMap, random)
            }
        } else if (currentCount > MAX_URBAN_CLUSTERS) {
            // 需要减少建筑群聚团
            val toRemove = currentCount - MAX_URBAN_CLUSTERS
            val removableClusters = clusters.filter { it.size <= 2 }  // 只移除小聚团
            val toRemoveClusters = removableClusters.shuffled(random).take(toRemove)

            for (cluster in toRemoveClusters) {
                for (cell in cluster) {
                    // 替换为平原
                    hexMap.cells[cell.first][cell.second].terrain = TerrainType.PLAIN
                }
            }
        }
    }

    /**
     * 识别所有建筑群聚团
     * @return List of clusters, each cluster is a list of cell coordinates
     */
    private fun identifyUrbanClusters(hexMap: HexMap): List<List<Pair<Int, Int>>> {
        val visited = mutableSetOf<Pair<Int, Int>>()
        val clusters = mutableListOf<List<Pair<Int, Int>>>()

        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                val pos = Pair(x, y)
                if (pos in visited) continue
                if (hexMap.cells[x][y].terrain != TerrainType.URBAN) continue

                // BFS找聚团
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

    /**
     * 添加随机建筑群聚团（在小聚团身边）
     */
    private fun addRandomUrbanCluster(hexMap: HexMap, random: Random) {
        // 找到现有建筑群附近的可放置位置
        val candidates = mutableListOf<Pair<Int, Int>>()

        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                if (hexMap.cells[x][y].terrain != TerrainType.PLAIN) continue

                // 检查是否与现有建筑群相邻
                val neighbors = hexMap.getNeighborCoords(x, y)
                val hasUrbanNeighbor = neighbors.any { (nx, ny) ->
                    hexMap.isValidCell(nx, ny) &&
                    hexMap.cells[nx][ny].terrain == TerrainType.URBAN
                }

                if (hasUrbanNeighbor) {
                    candidates.add(Pair(x, y))
                }
            }
        }

        if (candidates.isEmpty()) {
            // 如果没有合适位置，随机放置
            var attempts = 0
            while (attempts < 100) {
                val x = random.nextInt(hexMap.width)
                val y = random.nextInt(hexMap.height)
                if (hexMap.cells[x][y].terrain == TerrainType.PLAIN) {
                    hexMap.cells[x][y].terrain = TerrainType.URBAN
                    return
                }
                attempts++
            }
            return
        }

        // 添加一个建筑群格子
        val pos = candidates.random(random)
        hexMap.cells[pos.first][pos.second].terrain = TerrainType.URBAN
    }

    /**
     * 为建筑群设置防御工事
     * 
     * 规则：
     * 1. 每个建筑群聚团有统一的防御工事类型
     *    - 孤立格子（1格聚团）：栅栏或土墙，比例2:1
     *    - 多格聚团：石墙
     * 2. 每个建筑群格子六面全包围（6条边都设置防御工事）
     * 3. 同一聚团内相邻格子之间的边：防御工事降为"无"
     * 4. 不同聚团相邻格子之间的边：防御工事降为"无"
     */
    private fun assignUrbanFortifications(hexMap: HexMap, random: Random) {
        // 识别所有建筑群聚团
        val clusters = identifyUrbanClusters(hexMap)
        
        // 构建聚团ID映射：格子坐标 -> 聚团索引
        val clusterIdMap = mutableMapOf<Pair<Int, Int>, Int>()
        clusters.forEachIndexed { index, cluster ->
            cluster.forEach { pos -> clusterIdMap[pos] = index }
        }

        // 为每个聚团随机选择防御工事类型
        val clusterFortTypes = mutableMapOf<Int, FortType>()
        for ((clusterId, _) in clusters.withIndex()) {
            val cluster = clusters[clusterId]
            val fortType = if (cluster.size == 1) {
                // 孤立建筑群：栅栏或土墙，比例2:1
                if (random.nextFloat() < FENCE_RATIO) FortType.FENCE else FortType.EARTHWALL
            } else {
                // 多格聚团：石墙
                FortType.STONEWALL
            }
            clusterFortTypes[clusterId] = fortType
        }

        // 第一步：为所有建筑群格子设置六面全包围防御工事
        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                if (hexMap.cells[x][y].terrain != TerrainType.URBAN) continue

                val cellPos = Pair(x, y)
                val currentClusterId = clusterIdMap[cellPos] ?: continue
                val fortType = clusterFortTypes[currentClusterId] ?: continue

                // 设置6条边为聚团的防御工事类型
                for (dir in 0..5) {
                    hexMap.setFortification(x, y, dir, fortType)
                }
            }
        }

        // 第二步：同一聚团内相邻格子之间的边降为"无"
        // 遍历每个聚团内部的格子对
        for ((clusterId, cluster) in clusters.withIndex()) {
            // 遍历聚团内的每个格子
            for (cell in cluster) {
                val (x, y) = cell
                val neighbors = hexMap.getNeighborCoords(x, y)
                
                // 检查6个方向的邻居
                for (dir in 0..5) {
                    val neighbor = neighbors.getOrNull(dir) ?: continue
                    
                    // 检查邻居是否在同一聚团
                    if (clusterIdMap[neighbor] == clusterId) {
                        // 同一聚团内相邻格子之间的边，清除防御工事
                        hexMap.setFortification(x, y, dir, FortType.NONE)
                    }
                }
            }
        }

        // 第三步：不同聚团相邻格子之间的边降为"无"
        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                if (hexMap.cells[x][y].terrain != TerrainType.URBAN) continue

                val cellPos = Pair(x, y)
                val currentClusterId = clusterIdMap[cellPos] ?: continue

                val neighbors = hexMap.getNeighborCoords(x, y)

                for (dir in 0..5) {
                    val neighbor = neighbors.getOrNull(dir) ?: continue
                    val (nx, ny) = neighbor

                    if (!hexMap.isValidCell(nx, ny)) continue
                    if (hexMap.cells[nx][ny].terrain != TerrainType.URBAN) continue

                    val neighborClusterId = clusterIdMap[neighbor] ?: continue

                    // 如果是不同聚团，清除防御工事（使用setFortification同步两边）
                    if (currentClusterId != neighborClusterId) {
                        hexMap.setFortification(x, y, dir, FortType.NONE)
                    }
                }
            }
        }
    }
}
