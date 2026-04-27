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

    /** 目标地形比例
     * 注：高山比例已降低至原来的0.5倍，山地比例已提高至原来的1.5倍
     * 高山必须与山地相连才能存在
     */
    private val targetProportions = mapOf(
        TerrainType.PLAIN to 0.37f,
        TerrainType.FOREST to 0.28f,
        TerrainType.HILL to 0.225f,
        TerrainType.MOUNTAIN to 0.025f,
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

        // 剩余格子按平原:树林:山地比例分配
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
        // 注意：先处理山地，再处理高山（高山必须在山地旁边）
        val assigned = mutableSetOf<Pair<Int, Int>>()

        // 3.1: 先让山地扩散
        val hillSeeds = terrainCells[TerrainType.HILL] ?: emptyList()
        for ((sx, sy) in hillSeeds) {
            if (!assigned.contains(Pair(sx, sy))) {
                bfsExpand(hexMap, sx, sy, TerrainType.HILL, random, assigned)
            }
        }

        // 3.2: 在山地旁边放置高山种子
        val mountainSeeds = mutableListOf<Pair<Int, Int>>()
        val hillCells = mutableListOf<Pair<Int, Int>>()
        
        // 收集所有已分配的山地格子
        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                if (hexMap.getCell(x, y)?.terrain == TerrainType.HILL) {
                    hillCells.add(Pair(x, y))
                }
            }
        }
        
        // 在山地格子周围随机放置高山种子
        val mountainCount = terrainCounts[TerrainType.MOUNTAIN] ?: 0
        if (hillCells.isNotEmpty() && mountainCount > 0) {
            repeat(mountainCount) {
                val hillCell = hillCells.random(random)
                val neighbors = hexMap.getNeighbors(hillCell.first, hillCell.second)
                val availableNeighbors = neighbors.filter { 
                    !assigned.contains(Pair(it.x, it.y)) && 
                    hexMap.getCell(it.x, it.y)?.terrain == TerrainType.PLAIN
                }
                if (availableNeighbors.isNotEmpty()) {
                    val neighbor = availableNeighbors.random(random)
                    mountainSeeds.add(Pair(neighbor.x, neighbor.y))
                }
            }
        }

        // 3.3: 让高山扩散（只能从山地旁边的种子开始）
        for ((sx, sy) in mountainSeeds) {
            if (!assigned.contains(Pair(sx, sy))) {
                bfsExpand(hexMap, sx, sy, TerrainType.MOUNTAIN, random, assigned, mustBeAdjacentToHill = true)
            }
        }

        // 3.4: 处理其他地形（平原、树林、建筑群）
        for ((terrain, seeds) in terrainCells) {
            if (terrain == TerrainType.HILL || terrain == TerrainType.MOUNTAIN) {
                continue // 已处理
            }
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
     * @param mustBeAdjacentToHill 高山必须与山地相邻才能存在的标记
     */
    private fun bfsExpand(
        hexMap: HexMap,
        startX: Int,
        startY: Int,
        terrain: TerrainType,
        random: Random,
        assigned: MutableSet<Pair<Int, Int>>,
        mustBeAdjacentToHill: Boolean = false
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

            // 高山必须与山地或已有高山相邻
            if (mustBeAdjacentToHill || terrain == TerrainType.MOUNTAIN) {
                val neighbors = hexMap.getNeighbors(x, y)
                val hasAdjacentHill = neighbors.any { 
                    it.terrain == TerrainType.HILL || 
                    (it.terrain == TerrainType.MOUNTAIN && !assigned.contains(Pair(it.x, it.y)))
                }
                // 允许初始种子通过（startX, startY），之后扩散的格子必须满足条件
                val isInitialSeed = (x == startX && y == startY)
                if (!isInitialSeed && !hasAdjacentHill) {
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
     * 建筑群防御工事规则：
     * 1. 只有建筑群地形格有防御工事，其他地形6条边均无防御工事
     * 2. 离散建筑群（不与其他建筑群格子相邻）：6条边防御工事统一为"栅栏"或"土墙"，比例2:1
     * 3. 聚团建筑群：6条边防御工事统一为"石墙"
     * 4. 聚团内相邻格子之间的边：防御工事改为"无"
     */
    private fun applyUrbanFortification(hexMap: HexMap, random: Random) {
        // 第一步：重置所有格子的防御工事为NONE
        for (x in 0 until hexMap.width) {
            for (y in 0 until hexMap.height) {
                for (dir in 0..5) {
                    hexMap.getEdge(x, y, dir)?.fortification = FortType.NONE
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

        // 第三步：收集离散格子
        val discreteCells = mutableListOf<Pair<Int, Int>>()
        for ((pos, clusterId) in clusterIdMap) {
            if (clusters[clusterId].size == 1) {
                discreteCells.add(pos)
            }
        }

        // 第四步：为离散建筑群设置栅栏/土墙（2:1比例）
        val fenceCount = (discreteCells.size * 2.0 / 3.0).toInt()
        val shuffledDiscretes = discreteCells.shuffled(random)
        
        shuffledDiscretes.forEachIndexed { index, (x, y) ->
            val fortType = if (index < fenceCount) FortType.FENCE else FortType.EARTHWALL
            for (dir in 0..5) {
                hexMap.getEdge(x, y, dir)?.fortification = fortType
            }
        }

        // 第五步：为聚团建筑群设置石墙
        for (cluster in clusters) {
            if (cluster.size > 1) {
                for (cell in cluster) {
                    val (x, y) = cell
                    for (dir in 0..5) {
                        hexMap.getEdge(x, y, dir)?.fortification = FortType.STONEWALL
                    }
                }
            }
        }

        // 第六步：擦除聚团内相邻格子之间的防御工事
        for (cluster in clusters) {
            if (cluster.size > 1) {
                for (cell in cluster) {
                    val (x, y) = cell
                    val neighborCoords = hexMap.getNeighborCoords(x, y)
                    
                    for (dir in 0..5) {
                        val (nx, ny) = neighborCoords[dir]
                        // 检查邻居坐标是否在地图范围内
                        if (nx !in 0 until hexMap.width || ny !in 0 until hexMap.height) continue
                        
                        val neighborPos = Pair(nx, ny)
                        if (clusterIdMap[neighborPos] == clusterIdMap[Pair(x, y)]) {
                            hexMap.getEdge(x, y, dir)?.fortification = FortType.NONE
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
                if (hexMap.getCell(x, y)?.terrain != TerrainType.URBAN) continue

                val cluster = mutableListOf<Pair<Int, Int>>()
                val queue = ArrayDeque<Pair<Int, Int>>()
                queue.add(pos)

                while (queue.isNotEmpty()) {
                    val current = queue.removeFirst()
                    if (current in visited) continue
                    if (current.first !in 0 until hexMap.width || current.second !in 0 until hexMap.height) continue
                    if (hexMap.getCell(current.first, current.second)?.terrain != TerrainType.URBAN) continue

                    visited.add(current)
                    cluster.add(current)

                    // 使用 getNeighbors 获取邻居
                    val neighbors = hexMap.getNeighbors(current.first, current.second)
                    for (neighbor in neighbors) {
                        val neighborPos = Pair(neighbor.x, neighbor.y)
                        if (neighborPos !in visited) {
                            queue.add(neighborPos)
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