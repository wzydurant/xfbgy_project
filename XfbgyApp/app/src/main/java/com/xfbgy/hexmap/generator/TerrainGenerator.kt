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
     * 平原55%, 树林15%, 山地11.25%, 高山2.5%, 建筑群2.0%
     * 注：高山比例已降低至原来的0.5倍，山地比例已提高至原来的1.5倍
     * 高山必须与山地相连才能存在
     */
    private val terrainProportions = mapOf(
        TerrainType.PLAIN to 0.55f,
        TerrainType.FOREST to 0.15f,
        TerrainType.HILL to 0.1125f,
        TerrainType.MOUNTAIN to 0.025f,
        TerrainType.URBAN to 0.020f
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
     * 根据 1:5 比例计算，范围 [2, 6]
     */
    private const val MIN_URBAN_CLUSTERS = 2
    private const val MAX_URBAN_CLUSTERS = 6

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

        // 第一步：分配建筑群（先分配，避免被其他地形覆盖）
        val initialUrbanCount = terrainCounts[TerrainType.URBAN] ?: 0
        assignInitialUrbanTerrains(hexMap, initialUrbanCount, random)

        // 第二步：分配山地（高山必须依附于山地存在）
        assignDistributedTerrains(hexMap, TerrainType.HILL, terrainCounts[TerrainType.HILL] ?: 0,
            TERRAIN_CLUSTER_MIN, TERRAIN_CLUSTER_MAX, random)

        // 第三步：在山地旁边分配高山（高山必须与山地相连）
        assignMountainsAdjacentToHills(hexMap, terrainCounts[TerrainType.MOUNTAIN] ?: 0,
            MOUNTAIN_CLUSTER_MIN, MOUNTAIN_CLUSTER_MAX, random)

        // 第四步：分配森林
        assignDistributedTerrains(hexMap, TerrainType.FOREST, terrainCounts[TerrainType.FOREST] ?: 0,
            TERRAIN_CLUSTER_MIN, TERRAIN_CLUSTER_MAX, random)

        // 第五步：检查并调整建筑群聚团数量
        adjustUrbanClusters(hexMap, random)

        // 第六步：为建筑群设置防御工事
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
     * 在山地旁边分配高山
     * 高山必须与山地相连才能存在，不能独立存在
     */
    private fun assignMountainsAdjacentToHills(
        hexMap: HexMap,
        count: Int,
        clusterMin: Int,
        clusterMax: Int,
        random: Random
    ) {
        if (count <= 0) return

        // 收集所有山地格子作为高山生成的候选位置
        val hillCells = mutableListOf<Pair<Int, Int>>()
        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                if (hexMap.cells[x][y].terrain == TerrainType.HILL) {
                    hillCells.add(Pair(x, y))
                }
            }
        }

        if (hillCells.isEmpty()) {
            // 没有山地时，无法生成高山
            return
        }

        var remaining = count

        while (remaining > 0) {
            // 从随机一个山地格子开始
            val seedHill = hillCells.random(random)
            val seedX = seedHill.first
            val seedY = seedHill.second

            // 获取该山地格子周围可用的平原格子
            val neighbors = hexMap.getNeighborCoords(seedX, seedY)
            val availableNeighbors = neighbors.filter { (nx, ny) ->
                hexMap.isValidCell(nx, ny) && hexMap.cells[nx][ny].terrain == TerrainType.PLAIN
            }

            if (availableNeighbors.isEmpty()) {
                // 如果没有可用邻居，尝试其他山地格子
                continue
            }

            // 在邻居中随机选择一些作为高山
            val clusterSize = min(random.nextInt(clusterMin, clusterMax + 1), remaining)
            val selectedNeighbors = availableNeighbors.shuffled(random).take(clusterSize)

            for ((x, y) in selectedNeighbors) {
                if (hexMap.cells[x][y].terrain == TerrainType.PLAIN) {
                    hexMap.cells[x][y].terrain = TerrainType.MOUNTAIN
                    remaining--
                }
                if (remaining <= 0) break
            }
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
     * 
     * 算法要求：
     * 1. 聚团数 : 离散格子数 ≈ 1:5
     * 2. 最少2个聚团，最多6个
     * 3. 每个聚团 1-3 格
     * 4. 聚团之间距离 ≥ 2/3 地图边长
     * 5. 离散格子之间不能相互相邻
     */
    private fun assignInitialUrbanTerrains(
        hexMap: HexMap,
        totalCount: Int,
        random: Random
    ) {
        if (totalCount <= 0) return

        // 计算聚团数量
        // 根据 1:5 比例：聚团数 C，离散格子数 D ≈ 5*C
        // 总格子数 N ≈ C * avgClusterSize + 5*C ≈ C * 7
        // 所以 C ≈ N / 7，范围 [2, 6]
        val clusterCount = (totalCount / 7.0).toInt().coerceIn(2, 6)

        // 确保有足够的格子来形成聚团
        val minTilesForClusters = clusterCount
        
        if (totalCount < minTilesForClusters) {
            // 格子太少，全部作为离散建筑群（但保证不相邻）
            val placedDiscretes = mutableListOf<Pair<Int, Int>>()
            for (i in 0 until totalCount) {
                var attempts = 0
                while (attempts < 200) {
                    val x = random.nextInt(hexMap.width)
                    val y = random.nextInt(hexMap.height)
                    if (hexMap.cells[x][y].terrain == TerrainType.PLAIN) {
                        // 检查是否与已放置的离散格子相邻
                        var adjacent = false
                        for (pos in placedDiscretes) {
                            if (hexDistance(x, y, pos.first, pos.second, hexMap) < 1.5) {
                                adjacent = true
                                break
                            }
                        }
                        if (!adjacent) {
                            hexMap.cells[x][y].terrain = TerrainType.URBAN
                            placedDiscretes.add(Pair(x, y))
                            break
                        }
                    }
                    attempts++
                }
            }
            return
        }

        // 计算每个聚团的大小（1-3格随机）
        val clusterTileCounts = mutableListOf<Int>()
        var clusterTilesTotal = 0
        repeat(clusterCount) {
            val size = random.nextInt(URBAN_CLUSTER_MIN, URBAN_CLUSTER_MAX + 1)
            clusterTileCounts.add(size)
            clusterTilesTotal += size
        }
        
        // 计算离散格子数量 = 总数 - 聚团格子数
        val discreteCount = totalCount - clusterTilesTotal

        // 计算聚团之间的最小距离：2/3 地图边长
        val minClusterDistance = min(hexMap.width, hexMap.height) * 2.0 / 3.0

        // 第一步：放置聚团（贪心放置，确保距离约束）
        val clusterCenters = mutableListOf<Pair<Int, Int>>()
        val placedClusterTiles = mutableSetOf<Pair<Int, Int>>()
        
        repeat(clusterCount) { clusterIndex ->
            val clusterSize = clusterTileCounts.getOrElse(clusterIndex) { 1 }
            var placed = false
            var attempts = 0
            val maxAttempts = 1000

            while (!placed && attempts < maxAttempts) {
                attempts++
                
                val centerX = random.nextInt(hexMap.width)
                val centerY = random.nextInt(hexMap.height)
                
                // 逐渐放宽距离约束
                val distanceThreshold = when {
                    attempts > 500 -> min(hexMap.width, hexMap.height) / 3.0
                    attempts > 200 -> min(hexMap.width, hexMap.height) / 2.0
                    else -> minClusterDistance
                }
                
                // 检查距离其他聚团中心
                var validPosition = true
                for (existingCenter in clusterCenters) {
                    val dist = hexDistance(centerX, centerY, existingCenter.first, existingCenter.second, hexMap)
                    if (dist < distanceThreshold) {
                        validPosition = false
                        break
                    }
                }
                
                if (!validPosition) continue
                if (hexMap.cells[centerX][centerY].terrain != TerrainType.PLAIN) continue
                
                // 生长聚团
                val clusterTiles = growUrbanCluster(hexMap, centerX, centerY, clusterSize, random)
                
                if (clusterTiles.isNotEmpty()) {
                    clusterCenters.add(Pair(centerX, centerY))
                    placedClusterTiles.addAll(clusterTiles)
                    placed = true
                }
            }
            
            // 如果无法放置完整聚团，放置单格
            if (!placed) {
                var placedSingle = false
                for (attempt in 0 until 200) {
                    val x = random.nextInt(hexMap.width)
                    val y = random.nextInt(hexMap.height)
                    
                    if (hexMap.cells[x][y].terrain != TerrainType.PLAIN) continue
                    
                    val distThreshold = min(hexMap.width, hexMap.height) / 3.0
                    var valid = true
                    for (center in clusterCenters) {
                        if (hexDistance(x, y, center.first, center.second, hexMap) < distThreshold) {
                            valid = false
                            break
                        }
                    }
                    
                    if (valid) {
                        hexMap.cells[x][y].terrain = TerrainType.URBAN
                        clusterCenters.add(Pair(x, y))
                        placedClusterTiles.add(Pair(x, y))
                        placedSingle = true
                        break
                    }
                }
                
                // 强制放置
                if (!placedSingle) {
                    for (attempt in 0 until 100) {
                        val x = random.nextInt(hexMap.width)
                        val y = random.nextInt(hexMap.height)
                        if (hexMap.cells[x][y].terrain == TerrainType.PLAIN) {
                            hexMap.cells[x][y].terrain = TerrainType.URBAN
                            clusterCenters.add(Pair(x, y))
                            placedClusterTiles.add(Pair(x, y))
                            break
                        }
                    }
                }
            }
        }
        
        // 强制保证最少2个聚团
        if (clusterCenters.size < 2) {
            var toAdd = 2 - clusterCenters.size
            while (toAdd > 0) {
                for (attempt in 0 until 200) {
                    val x = random.nextInt(hexMap.width)
                    val y = random.nextInt(hexMap.height)
                    if (hexMap.cells[x][y].terrain == TerrainType.PLAIN) {
                        hexMap.cells[x][y].terrain = TerrainType.URBAN
                        clusterCenters.add(Pair(x, y))
                        placedClusterTiles.add(Pair(x, y))
                        toAdd--
                        break
                    }
                    if (toAdd <= 0) break
                }
                break
            }
        }

        // 第二步：放置离散建筑群
        // 离散格子之间不能相邻，且应远离聚团
        val placedDiscretes = mutableSetOf<Pair<Int, Int>>()
        var remainingDiscrete = discreteCount
        
        // 第一轮：严格要求（远离聚团 2.5 格，离散之间距离 1.5 格）
        repeat(2000) {
            if (remainingDiscrete <= 0) return@repeat
            
            val x = random.nextInt(hexMap.width)
            val y = random.nextInt(hexMap.height)
            
            if (hexMap.cells[x][y].terrain != TerrainType.PLAIN) return@repeat
            
            // 检查是否距离聚团足够远
            var nearCluster = false
            for (center in clusterCenters) {
                if (hexDistance(x, y, center.first, center.second, hexMap) < 2.5) {
                    nearCluster = true
                    break
                }
            }
            if (nearCluster) return@repeat
            
            // 检查是否与其他离散格子相邻
            var adjacentToDiscrete = false
            for (pos in placedDiscretes) {
                if (hexDistance(x, y, pos.first, pos.second, hexMap) < 1.5) {
                    adjacentToDiscrete = true
                    break
                }
            }
            if (adjacentToDiscrete) return@repeat
            
            hexMap.cells[x][y].terrain = TerrainType.URBAN
            placedDiscretes.add(Pair(x, y))
            remainingDiscrete--
        }
        
        // 第二轮：放宽约束（只要求不与离散格子相邻）
        if (remainingDiscrete > 0) {
            repeat(2000) {
                if (remainingDiscrete <= 0) return@repeat
                
                val x = random.nextInt(hexMap.width)
                val y = random.nextInt(hexMap.height)
                
                if (hexMap.cells[x][y].terrain != TerrainType.PLAIN) return@repeat
                
                // 只检查是否与其他离散格子相邻
                var adjacentToDiscrete = false
                for (pos in placedDiscretes) {
                    if (hexDistance(x, y, pos.first, pos.second, hexMap) < 1.5) {
                        adjacentToDiscrete = true
                        break
                    }
                }
                if (adjacentToDiscrete) return@repeat
                
                hexMap.cells[x][y].terrain = TerrainType.URBAN
                placedDiscretes.add(Pair(x, y))
                remainingDiscrete--
            }
        }
        
        // 第三轮：放到任意空白位置（尽量保持分散）
        if (remainingDiscrete > 0) {
            repeat(1000) {
                if (remainingDiscrete <= 0) return@repeat
                
                val x = random.nextInt(hexMap.width)
                val y = random.nextInt(hexMap.height)
                
                if (hexMap.cells[x][y].terrain == TerrainType.PLAIN) {
                    // 即使放宽条件，也尽量不与离散格子完全相邻
                    var adjacentToDiscrete = false
                    for (pos in placedDiscretes) {
                        if (hexDistance(x, y, pos.first, pos.second, hexMap) < 1.0) {
                            adjacentToDiscrete = true
                            break
                        }
                    }
                    if (!adjacentToDiscrete) {
                        hexMap.cells[x][y].terrain = TerrainType.URBAN
                        placedDiscretes.add(Pair(x, y))
                        remainingDiscrete--
                    }
                }
            }
        }
        
        // 第四轮：最后兜底，放到任何空白位置
        if (remainingDiscrete > 0) {
            repeat(500) {
                if (remainingDiscrete <= 0) return@repeat
                
                val x = random.nextInt(hexMap.width)
                val y = random.nextInt(hexMap.height)
                
                if (hexMap.cells[x][y].terrain == TerrainType.PLAIN) {
                    hexMap.cells[x][y].terrain = TerrainType.URBAN
                    remainingDiscrete--
                }
            }
        }
    }

    /**
     * 六边形格子距离计算
     * 使用偏移坐标的近似距离
     */
    private fun hexDistance(x1: Int, y1: Int, x2: Int, y2: Int, hexMap: HexMap): Double {
        // 转换为轴向坐标
        val ax1 = x1 - (y1 - (y1 and 1)) / 2
        val az1 = y1
        val ax2 = x2 - (y2 - (y2 and 1)) / 2
        val az2 = y2
        
        // 转换后的 q, r 坐标用于距离计算
        val q1 = ax1
        val r1 = az1 - (ax1 + az1) / 2
        val q2 = ax2
        val r2 = az2 - (ax2 + az2) / 2
        
        // 六边形曼哈顿距离
        val dq = kotlin.math.abs(q1 - q2)
        val dr = kotlin.math.abs(r1 - r2)
        val ds = kotlin.math.abs((q1 + r1) - (q2 + r2))
        
        return maxOf(dq, dr, ds).toDouble()
    }

    /**
     * 生长建筑群聚团（1-3格）
     */
    private fun growUrbanCluster(
        hexMap: HexMap,
        startX: Int,
        startY: Int,
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
            hexMap.cells[current.first][current.second].terrain = TerrainType.URBAN

            // 建筑群聚团使用中等概率扩展
            val neighbors = hexMap.getNeighborCoords(current.first, current.second)
            val shuffledNeighbors = neighbors.shuffled(random)
            for (neighbor in shuffledNeighbors) {
                if (!hexMap.isValidCell(neighbor.first, neighbor.second)) continue
                if (neighbor in visited) continue
                if (hexMap.cells[neighbor.first][neighbor.second].terrain != TerrainType.PLAIN) continue
                // 降低邻接概率以控制聚团大小，防止聚团过大
                if (random.nextFloat() < 0.5f) {
                    queue.add(neighbor)
                }
            }
        }

        return cluster
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
     * 添加随机建筑群聚团（在与现有建筑群距离足够远的位置）
     */
    private fun addRandomUrbanCluster(hexMap: HexMap, random: Random) {
        // 找到所有现有建筑群格子
        val existingUrbans = mutableListOf<Pair<Int, Int>>()
        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                if (hexMap.cells[x][y].terrain == TerrainType.URBAN) {
                    existingUrbans.add(Pair(x, y))
                }
            }
        }

        // 找到距离所有现有建筑群足够远的位置
        val minDistThreshold = maxOf(3, minOf(hexMap.width, hexMap.height) / 2)
        val candidates = mutableListOf<Pair<Int, Int>>()

        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                if (hexMap.cells[x][y].terrain != TerrainType.PLAIN) continue

                // 检查与所有现有建筑群格子的距离
                var tooClose = false
                for (urban in existingUrbans) {
                    if (hexDistance(x, y, urban.first, urban.second, hexMap) < minDistThreshold) {
                        tooClose = true
                        break
                    }
                }

                if (!tooClose) {
                    candidates.add(Pair(x, y))
                }
            }
        }

        if (candidates.isEmpty()) {
            // 如果没有合适位置，尝试放宽条件
            for (y in 0 until hexMap.height) {
                for (x in 0 until hexMap.width) {
                    if (hexMap.cells[x][y].terrain == TerrainType.PLAIN) {
                        hexMap.cells[x][y].terrain = TerrainType.URBAN
                        return
                    }
                }
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
     * 1. 只有建筑群地形格有防御工事，其他地形（平原、森林、山地、高山）6条边均无防御工事
     * 2. 离散建筑群（不与其他建筑群格子相邻）：6条边防御工事统一为"栅栏"或"土墙"，比例2:1
     * 3. 聚团建筑群：6条边防御工事统一为"石墙"
     * 4. 聚团内相邻格子之间的边：防御工事改为"无"
     */
    private fun assignUrbanFortifications(hexMap: HexMap, random: Random) {
        // 第一步：重置所有格子的防御工事为NONE
        // 只对建筑群地形设置防御工事，其他地形保持NONE
        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                // 非建筑群地形：6条边均为无
                if (hexMap.cells[x][y].terrain != TerrainType.URBAN) {
                    for (dir in 0..5) {
                        hexMap.setFortification(x, y, dir, FortType.NONE)
                    }
                }
            }
        }

        // 第二步：识别所有建筑群聚团
        val clusters = identifyUrbanClusters(hexMap)
        
        // 构建聚团ID映射：格子坐标 -> 聚团索引
        val clusterIdMap = mutableMapOf<Pair<Int, Int>, Int>()
        clusters.forEachIndexed { index, cluster ->
            cluster.forEach { pos -> clusterIdMap[pos] = index }
        }

        // 统计离散格子数量（用于栅栏/土墙比例控制）
        var discreteCount = 0
        var clusterTileCount = 0
        for (cluster in clusters) {
            if (cluster.size == 1) {
                discreteCount++
            } else {
                clusterTileCount += cluster.size
            }
        }

        // 第三步：为离散建筑群设置栅栏/土墙（2:1比例）
        // 先收集所有离散格子
        val discreteCells = mutableListOf<Pair<Int, Int>>()
        for ((pos, clusterId) in clusterIdMap) {
            if (clusters[clusterId].size == 1) {
                discreteCells.add(pos)
            }
        }

        // 按2:1比例分配栅栏和土墙
        val fenceCount = (discreteCount * 2.0 / 3.0).toInt() // 栅栏约占2/3
        val shuffledDiscretes = discreteCells.shuffled(random)
        
        // 前fenceCount个为栅栏，其余为土墙
        shuffledDiscretes.forEachIndexed { index, pos ->
            val fortType = if (index < fenceCount) FortType.FENCE else FortType.EARTHWALL
            val (x, y) = pos
            for (dir in 0..5) {
                hexMap.setFortification(x, y, dir, fortType)
            }
        }

        // 第四步：为聚团建筑群设置石墙
        for (cluster in clusters) {
            if (cluster.size > 1) {
                // 聚团格子：6条边设置为石墙
                for (cell in cluster) {
                    val (x, y) = cell
                    for (dir in 0..5) {
                        hexMap.setFortification(x, y, dir, FortType.STONEWALL)
                    }
                }
            }
        }

        // 第五步：擦除聚团内相邻格子之间的防御工事
        for (cluster in clusters) {
            if (cluster.size > 1) {
                for (cell in cluster) {
                    val (x, y) = cell
                    val neighbors = hexMap.getNeighborCoords(x, y)
                    
                    for (dir in 0..5) {
                        val neighbor = neighbors.getOrNull(dir) ?: continue
                        
                        // 如果邻居在同一个聚团内，清除防御工事
                        if (clusterIdMap[neighbor] == clusterIdMap[Pair(x, y)]) {
                            hexMap.setFortification(x, y, dir, FortType.NONE)
                        }
                    }
                }
            }
        }
    }
}
