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
     * 地形目标比例（新算法）
     * 平原60%, 山地15%, 森林10%, 高山5%, 建筑群16%
     * 
     * 建筑群分布规则：
     * - 聚团：占总格子数的8%，每个聚团2-3格
     * - 离散格子：占总格子数的8%，互相不相邻也不与聚团相邻
     * - 高山必须与山地相邻才能存在
     */
    private val terrainProportions = mapOf(
        TerrainType.PLAIN to 0.60f,
        TerrainType.FOREST to 0.10f,
        TerrainType.HILL to 0.15f,
        TerrainType.MOUNTAIN to 0.05f,
        TerrainType.URBAN to 0.16f
    )

    /**
     * 建筑群比例常数
     */
    private const val URBAN_CLUSTER_RATIO = 0.04f    // 聚团占4%
    private const val URBAN_DISCRETE_RATIO = 0.08f   // 离散格子占8%

    /**
     * 高山团块大小范围
     */
    private const val MOUNTAIN_CLUSTER_MIN = 2
    private const val MOUNTAIN_CLUSTER_MAX = 5

    /**
     * 建筑群聚团大小范围（2-3格）
     */
    private const val URBAN_CLUSTER_MIN = 2
    private const val URBAN_CLUSTER_MAX = 3

    /**
     * 聚团距离阈值（相对于地图边长的比例）
     */
    private const val CLUSTER_SPAWN_ZONE = 1.0 / 6.0    // x < (1/6)n 区域放置聚团1
    private const val CLUSTER_EXCLUSION_ZONE = 2.0 / 3.0  // 距离 < (2/3)n 的区域禁止放置新聚团

    /**
     * 山地/森林团块大小范围
     */
    private const val TERRAIN_CLUSTER_MIN = 2
    private const val TERRAIN_CLUSTER_MAX = 10

    /**
     * 山地/森林聚团扩展概率
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
     * 建筑群生成算法（新算法）
     * 
     * 算法步骤：
     * 1. 在x < (1/6)n区域放置聚团1
     * 2. 计算禁入区域（距聚团<(2/3)n的点）
     * 3. 在空图集中放置聚团2
     * 4. 根据1:5比例计算聚团目标数：在空图集中放置聚团直到满足比例
     * 5. 在空图集中放置离散建筑群格子
     * 
     * 比例控制：每5个离散格子对应1个聚团
     * 
     * @param hexMap 地图对象
     * @param totalCells 地图总格子数
     * @param random 随机数生成器
     */
    private fun assignInitialUrbanTerrains(
        hexMap: HexMap,
        totalCells: Int,
        random: Random
    ) {
        val mapSize = maxOf(hexMap.width, hexMap.height)
        
        // 计算离散格子目标数（占8%）
        val discreteTargetCount = (totalCells * URBAN_DISCRETE_RATIO).toInt()
        
        // 初始化：所有格子都在空图集
        val emptySet = mutableSetOf<Pair<Int, Int>>()
        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                emptySet.add(Pair(x, y))
            }
        }
        
        // 聚团集：存放所有聚团的坐标
        val clusterSet = mutableSetOf<Pair<Int, Int>>()
        
        // ====== 步骤1：在 x < (1/6)n 区域放置聚团1 ======
        val spawnZoneWidth = (mapSize * CLUSTER_SPAWN_ZONE).toInt()
        
        // 在指定区域找一个有效位置作为聚团1的种子
        var cluster1Seed: Pair<Int, Int>? = null
        repeat(1000) {
            val x = random.nextInt(spawnZoneWidth)
            val y = random.nextInt(hexMap.height)
            if (hexMap.cells[x][y].terrain == TerrainType.PLAIN) {
                cluster1Seed = Pair(x, y)
                return@repeat
            }
        }
        
        // 如果找不到合适位置，放宽搜索
        if (cluster1Seed == null) {
            repeat(1000) {
                val x = random.nextInt(hexMap.width)
                val y = random.nextInt(hexMap.height)
                if (hexMap.cells[x][y].terrain == TerrainType.PLAIN) {
                    cluster1Seed = Pair(x, y)
                    return@repeat
                }
            }
        }
        
        // 生长聚团1（2-3格）
        cluster1Seed?.let { seed ->
            val clusterSize = random.nextInt(URBAN_CLUSTER_MIN, URBAN_CLUSTER_MAX + 1)
            val tiles = growUrbanCluster(hexMap, seed.first, seed.second, clusterSize, random)
            clusterSet.addAll(tiles)
        }
        
        // ====== 步骤2：从空图集移除聚团集和禁入区域 ======
        emptySet.removeAll(clusterSet)
        
        // 计算禁入区域：距离所有聚团 < (2/3)n 的点
        val exclusionZone = mutableSetOf<Pair<Int, Int>>()
        val exclusionDistance = mapSize * CLUSTER_EXCLUSION_ZONE
        
        for (pos in emptySet.toList()) {
            var minDistToCluster = Double.MAX_VALUE
            for (clusterPos in clusterSet) {
                val dist = hexDistance(pos.first, pos.second, clusterPos.first, clusterPos.second, hexMap)
                minDistToCluster = minOf(minDistToCluster, dist)
            }
            if (minDistToCluster < exclusionDistance) {
                exclusionZone.add(pos)
            }
        }
        
        emptySet.removeAll(exclusionZone)
        
        // ====== 步骤3：在空图集中放置聚团2 ======
        if (emptySet.isNotEmpty()) {
            val cluster2Seed = emptySet.random(random)
            val clusterSize = random.nextInt(URBAN_CLUSTER_MIN, URBAN_CLUSTER_MAX + 1)
            val tiles = growUrbanCluster(hexMap, cluster2Seed.first, cluster2Seed.second, clusterSize, random)
            clusterSet.addAll(tiles)
            
            // 更新空图集
            emptySet.removeAll(clusterSet)
        }
        
        // ====== 步骤4：继续生成聚团直到达到目标格子数 ======
        // 聚团目标格子数 = 总格子数 * 4%
        val clusterTargetCount = (totalCells * URBAN_CLUSTER_RATIO).toInt()
        
        // 继续生成聚团直到达到目标格子数
        while (clusterSet.size < clusterTargetCount && emptySet.isNotEmpty()) {
            // 找一个不与聚团集相邻的位置
            var newSeed: Pair<Int, Int>? = null
            
            // 优先在远离聚团的区域找
            val candidates = emptySet.filter { pos ->
                var adjacentToCluster = false
                for (clusterPos in clusterSet) {
                    if (hexDistance(pos.first, pos.second, clusterPos.first, clusterPos.second, hexMap) < 1.5) {
                        adjacentToCluster = true
                        break
                    }
                }
                !adjacentToCluster
            }
            
            if (candidates.isNotEmpty()) {
                newSeed = candidates.random(random)
            } else if (emptySet.isNotEmpty()) {
                // 放宽条件：允许与聚团相邻但距离聚团足够远
                val relaxedCandidates = emptySet.filter { pos ->
                    var tooClose = false
                    for (clusterPos in clusterSet) {
                        if (hexDistance(pos.first, pos.second, clusterPos.first, clusterPos.second, hexMap) < 2.0) {
                            tooClose = true
                            break
                        }
                    }
                    !tooClose
                }
                if (relaxedCandidates.isNotEmpty()) {
                    newSeed = relaxedCandidates.random(random)
                } else {
                    newSeed = emptySet.random(random)
                }
            }
            
            newSeed?.let { seed ->
                val clusterSize = random.nextInt(URBAN_CLUSTER_MIN, URBAN_CLUSTER_MAX + 1)
                val tiles = growUrbanCluster(hexMap, seed.first, seed.second, clusterSize, random)
                clusterSet.addAll(tiles)
                
                // 更新空图集
                emptySet.removeAll(clusterSet)
            } ?: break
        }
        
        // ====== 步骤5：放置离散建筑群格子 ======
        val placedDiscretes = mutableSetOf<Pair<Int, Int>>()
        var remainingDiscrete = discreteTargetCount
        
        // 第一轮：严格要求（不与聚团相邻，离散之间不相邻）
        repeat(5000) {
            if (remainingDiscrete <= 0 || emptySet.isEmpty()) return@repeat
            
            val candidates = emptySet.filter { pos ->
                // 不与任何聚团相邻
                var adjacentToCluster = false
                for (clusterPos in clusterSet) {
                    if (hexDistance(pos.first, pos.second, clusterPos.first, clusterPos.second, hexMap) < 1.5) {
                        adjacentToCluster = true
                        break
                    }
                }
                !adjacentToCluster
            }.filter { pos ->
                // 不与其他离散格子相邻
                var adjacentToDiscrete = false
                for (discretePos in placedDiscretes) {
                    if (hexDistance(pos.first, pos.second, discretePos.first, discretePos.second, hexMap) < 1.5) {
                        adjacentToDiscrete = true
                        break
                    }
                }
                !adjacentToDiscrete
            }
            
            if (candidates.isNotEmpty()) {
                val chosen = candidates.random(random)
                hexMap.cells[chosen.first][chosen.second].terrain = TerrainType.URBAN
                placedDiscretes.add(chosen)
                emptySet.remove(chosen)
                remainingDiscrete--
            }
        }
        
        // 第二轮：放宽约束（只要求不与离散格子相邻）
        if (remainingDiscrete > 0) {
            repeat(2000) {
                if (remainingDiscrete <= 0 || emptySet.isEmpty()) return@repeat
                
                val candidates = emptySet.filter { pos ->
                    var adjacentToDiscrete = false
                    for (discretePos in placedDiscretes) {
                        if (hexDistance(pos.first, pos.second, discretePos.first, discretePos.second, hexMap) < 1.5) {
                            adjacentToDiscrete = true
                            break
                        }
                    }
                    !adjacentToDiscrete
                }
                
                if (candidates.isNotEmpty()) {
                    val chosen = candidates.random(random)
                    hexMap.cells[chosen.first][chosen.second].terrain = TerrainType.URBAN
                    placedDiscretes.add(chosen)
                    emptySet.remove(chosen)
                    remainingDiscrete--
                }
            }
        }
        
        // 第三轮：最后兜底
        if (remainingDiscrete > 0) {
            repeat(1000) {
                if (remainingDiscrete <= 0 || emptySet.isEmpty()) return@repeat
                
                val chosen = emptySet.random(random)
                hexMap.cells[chosen.first][chosen.second].terrain = TerrainType.URBAN
                emptySet.remove(chosen)
                remainingDiscrete--
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
     * 从坐标集合中识别聚团（不修改地图）
     * @param cellSet 格子坐标集合
     * @param hexMap 地图对象
     * @return List of clusters, each cluster is a list of cell coordinates
     */
    private fun identifyClustersFromSet(cellSet: Set<Pair<Int, Int>>, hexMap: HexMap): List<List<Pair<Int, Int>>> {
        val visited = mutableSetOf<Pair<Int, Int>>()
        val clusters = mutableListOf<List<Pair<Int, Int>>>()

        for (pos in cellSet) {
            if (pos in visited) continue

            // BFS找聚团
            val cluster = mutableListOf<Pair<Int, Int>>()
            val queue = ArrayDeque<Pair<Int, Int>>()
            queue.add(pos)

            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                if (current in visited) continue
                if (current !in cellSet) continue

                visited.add(current)
                cluster.add(current)

                val neighbors = hexMap.getNeighborCoords(current.first, current.second)
                for (neighbor in neighbors) {
                    if (neighbor !in visited && neighbor in cellSet) {
                        queue.add(neighbor)
                    }
                }
            }

            if (cluster.isNotEmpty()) {
                clusters.add(cluster)
            }
        }

        return clusters
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
