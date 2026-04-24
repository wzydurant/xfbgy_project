package com.xfbgy.hexmap.generator

import com.xfbgy.hexmap.data.HexMap
import kotlin.random.Random

/**
 * 河流生成器
 *
 * 使用随机游走（Random Walk）算法生成河流：
 * 1. 在地图边缘随机选取1~3个出发点
 * 2. 从边缘出发向内蜿蜒前进，形成主干河道
 * 3. 允许分支（概率约20%），但须保持与主干相连
 * 4. 每次标记一条边时，同步标记相邻格的对应共享边
 * 5. 生成后执行连通性检验，不连通则重新生成
 *
 * 河流必须形成连通图，且至少延伸到地图边缘一个出口
 */
object RiverGenerator {

    /**
     * 河流方向偏移量（用于随机游走）
     * 0=上, 1=右上, 2=右下, 3=下, 4=左下, 5=左上
     */
    private val directionOffsets = mapOf(
        0 to Pair(0, -1),   // 上
        1 to Pair(1, -1),   // 右上
        2 to Pair(1, 0),    // 右下
        3 to Pair(0, 1),    // 下
        4 to Pair(-1, 0),   // 左下
        5 to Pair(-1, -1)   // 左上
    )

    /**
     * 对面方向映射
     */
    private val oppositeDirections = mapOf(
        0 to 3, 1 to 4, 2 to 5, 3 to 0, 4 to 1, 5 to 2
    )

    /**
     * 分支概率
     */
    private const val BRANCH_PROBABILITY = 0.2

    /**
     * 最大尝试次数（防止无限循环）
     */
    private const val MAX_ATTEMPTS = 50

    /**
     * 单条河流最大长度
     */
    private const val MAX_RIVER_LENGTH = 100

    /**
     * 生成完整地图的河流系统
     * @param hexMap 地图对象
     * @param minSources 最少源头数（默认1）
     * @param maxSources 最大源头数（默认3）
     * @param seed 随机种子（可选）
     * @return 是否成功生成河流
     */
    fun generateRivers(
        hexMap: HexMap,
        minSources: Int = 1,
        maxSources: Int = 3,
        seed: Long? = null
    ): Boolean {
        val random = if (seed != null) Random(seed) else Random

        repeat(MAX_ATTEMPTS) {
            // 清除现有河流
            hexMap.clearAllEdges()

            // 随机选择源头数量
            val sourceCount = random.nextInt(minSources, maxSources + 1)

            // 获取所有可用的边缘边
            val availableEdges = getAvailableEdgeCells(hexMap)
            if (availableEdges.size < sourceCount) return false

            // 随机选择源头
            val sources = availableEdges.shuffled(random).take(sourceCount)

            // 为每个源头生成河流
            val rivers = mutableListOf<List<Pair<Pair<Int, Int>, Int>>>()
            var success = true

            for (source in sources) {
                val river = generateSingleRiver(hexMap, source, random)
                if (river.isEmpty()) {
                    success = false
                } else {
                    rivers.add(river)
                }
            }

            if (success && rivers.isNotEmpty()) {
                // 验证河流连通性
                if (validateRiverConnectivity(hexMap, rivers)) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * 生成单条河流（随机游走）
     * @return 河流经过的边列表，每项为Pair(格子坐标, 方向)
     */
    private fun generateSingleRiver(
        hexMap: HexMap,
        startEdge: Triple<Int, Int, Int>, // x, y, direction
        random: Random
    ): List<Pair<Pair<Int, Int>, Int>> {
        val riverEdges = mutableListOf<Pair<Pair<Int, Int>, Int>>()

        var currentX = startEdge.first
        var currentY = startEdge.second
        var enteringDir = startEdge.third // 从哪个方向进入当前格

        // 河流入口方向的反方向作为第一条边的方向
        val firstDir = oppositeDirections[enteringDir] ?: 0

        var step = 0
        while (step < MAX_RIVER_LENGTH) {
            step++
            // 确定当前格的河流边方向
            val riverDir = if (riverEdges.isEmpty()) firstDir else chooseNextDirection(random)

            // 检查边界
            if (!isValidEdge(hexMap, currentX, currentY, riverDir)) {
                break
            }

            // 添加河流边（从当前格看河流流向的方向）
            riverEdges.add(Pair(Pair(currentX, currentY), riverDir))

            // 设置河流
            hexMap.setRiver(currentX, currentY, riverDir, true)

            // 移动到下一个格子
            val offset = directionOffsets[riverDir] ?: Pair(0, 0)
            val nextX = currentX + offset.first
            val nextY = currentY + offset.second

            // 检查是否到达地图边缘
            if (!hexMap.isValidCell(nextX, nextY)) {
                break
            }

            currentX = nextX
            currentY = nextY

            // 20%概率生成支流
            if (random.nextFloat() < BRANCH_PROBABILITY && riverEdges.size > 3) {
                generateBranch(hexMap, currentX, currentY, riverDir, random)
            }
        }

        return riverEdges
    }

    /**
     * 生成支流
     */
    private fun generateBranch(
        hexMap: HexMap,
        x: Int,
        y: Int,
        parentDir: Int,
        random: Random
    ) {
        // 从父方向的两侧选择支流方向
        val possibleDirs = listOf(
            (parentDir + 1) % 6,
            (parentDir + 5) % 6
        )

        val branchDir = possibleDirs.random(random)

        if (isValidEdge(hexMap, x, y, branchDir)) {
            // 标记支流（较短）
            hexMap.setRiver(x, y, branchDir, true)

            // 支流可以再延伸一小段
            val offset = directionOffsets[branchDir] ?: Pair(0, 0)
            val nextX = x + offset.first
            val nextY = y + offset.second

            if (hexMap.isValidCell(nextX, nextY)) {
                val extendDir = chooseNextDirection(random)
                if (isValidEdge(hexMap, nextX, nextY, extendDir)) {
                    hexMap.setRiver(nextX, nextY, extendDir, true)
                }
            }
        }
    }

    /**
     * 选择下一步方向（偏向继续前进，有一定随机性）
     */
    private fun chooseNextDirection(random: Random): Int {
        // 大部分情况继续前进，小部分情况转向
        return if (random.nextFloat() < 0.7f) {
            // 继续当前方向或稍微偏移
            listOf(0, 1, 2, 3, 4, 5).random(random)
        } else {
            // 随机方向
            random.nextInt(0, 6)
        }
    }

    /**
     * 检查边是否有效（不会穿过高山）
     */
    private fun isValidEdge(hexMap: HexMap, x: Int, y: Int, direction: Int): Boolean {
        if (!hexMap.isValidCell(x, y)) return false

        val cell = hexMap.cells[x][y]
        // 河流不能穿过高山
        if (cell.terrain == com.xfbgy.hexmap.data.TerrainType.MOUNTAIN) {
            return false
        }

        // 检查相邻格子
        val offset = directionOffsets[direction] ?: return false
        val neighborX = x + offset.first
        val neighborY = y + offset.second

        if (!hexMap.isValidCell(neighborX, neighborY)) return true // 边界也算有效

        val neighborCell = hexMap.cells[neighborX][neighborY]
        return neighborCell.terrain != com.xfbgy.hexmap.data.TerrainType.MOUNTAIN
    }

    /**
     * 获取可用的边缘边（用于放置源头）
     * @return List<Triple<x, y, direction>> 表示边缘边的位置和方向
     */
    private fun getAvailableEdgeCells(hexMap: HexMap): List<Triple<Int, Int, Int>> {
        val available = mutableListOf<Triple<Int, Int, Int>>()

        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                val cell = hexMap.cells[x][y]
                // 跳过高山
                if (cell.terrain == com.xfbgy.hexmap.data.TerrainType.MOUNTAIN) continue

                // 检查是否为边缘格子，并确定河流进入方向
                when {
                    y == 0 -> available.add(Triple(x, y, 0)) // 上边缘，河流从上往下
                    y == hexMap.height - 1 -> available.add(Triple(x, y, 3)) // 下边缘
                    x == 0 -> {
                        if (y % 2 == 0) {
                            available.add(Triple(x, y, 5)) // 左上
                        } else {
                            available.add(Triple(x, y, 4)) // 左下
                        }
                    }
                    x == hexMap.width - 1 -> {
                        if (y % 2 == 0) {
                            available.add(Triple(x, y, 1)) // 右上
                        } else {
                            available.add(Triple(x, y, 2)) // 右下
                        }
                    }
                }
            }
        }

        return available
    }

    /**
     * 验证河流连通性
     */
    private fun validateRiverConnectivity(hexMap: HexMap, rivers: List<List<Pair<Pair<Int, Int>, Int>>>): Boolean {
        if (rivers.isEmpty()) return false

        // 收集所有有河流的边
        val riverEdges = mutableSetOf<Pair<Int, Int>>()
        for (river in rivers) {
            for ((cell, _) in river) {
                riverEdges.add(cell)
            }
        }

        if (riverEdges.isEmpty()) return false

        // 从第一条河的第一个格子开始BFS
        val firstRiver = rivers.first()
        if (firstRiver.isEmpty()) return false

        val startCell = firstRiver.first().first
        val visited = mutableSetOf<Pair<Int, Int>>()
        val queue = ArrayDeque<Pair<Int, Int>>()
        queue.add(startCell)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current in visited) continue
            if (!hexMap.isValidCell(current.first, current.second)) continue

            visited.add(current)

            // 检查当前格子的所有河流边
            val cellEdges = hexMap.edges[current.first][current.second]
            for (dir in 0..5) {
                if (cellEdges[dir].hasRiver) {
                    // 获取相邻格子
                    val neighbors = hexMap.getNeighborCoords(current.first, current.second)
                    if (dir in neighbors.indices) {
                        val neighbor = neighbors[dir]
                        if (hexMap.isValidCell(neighbor.first, neighbor.second) &&
                            neighbor !in visited && neighbor in riverEdges) {
                            queue.add(neighbor)
                        }
                    }
                }
            }
        }

        // 所有河流边都应该被访问过
        return visited.size == riverEdges.size
    }

    /**
     * 检查河流是否至少有一个出口在地图边缘
     */
    private fun hasOutletToEdge(hexMap: HexMap): Boolean {
        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                val cellEdges = hexMap.edges[x][y]
                for (dir in 0..5) {
                    if (cellEdges[dir].hasRiver) {
                        // 检查相邻是否在地图外
                        val neighbors = hexMap.getNeighborCoords(x, y)
                        if (dir in neighbors.indices) {
                            val neighbor = neighbors[dir]
                            if (!hexMap.isValidCell(neighbor.first, neighbor.second)) {
                                return true
                            }
                        }
                    }
                }
            }
        }
        return false
    }
}