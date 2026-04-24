package com.xfbgy.hexmap.generator

import com.xfbgy.hexmap.data.FortType
import com.xfbgy.hexmap.data.HexMap
import com.xfbgy.hexmap.data.TerrainType
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * 河流生成器 - 简化版
 *
 * 生成规则：
 * 1. 从地图一侧边缘延伸河流到对侧边缘
 * 2. 至少一条河流要延伸到地图中央1/5范围
 * 3. 河流UI表现为交界线上的蓝色粗线（在HexMapView中绘制）
 */
object RiverGenerator {

    /**
     * 获取指定方向的偏移量（考虑奇偶行偏移）
     * 0=上, 1=右上, 2=右下, 3=下, 4=左下, 5=左上
     */
    private fun getDirectionOffset(y: Int, direction: Int): Pair<Int, Int> {
        // odd-r 布局：奇数行向右偏移半个格宽
        return if (y % 2 == 1) {
            // 奇数行（右偏）
            when (direction) {
                0 -> Pair(0, -1)   // 上
                1 -> Pair(0, -1)   // 右上
                2 -> Pair(0, 1)    // 右下
                3 -> Pair(0, 1)    // 下
                4 -> Pair(-1, 0)   // 左下
                5 -> Pair(-1, 0)   // 左上
                else -> Pair(0, 0)
            }
        } else {
            // 偶数行
            when (direction) {
                0 -> Pair(0, -1)   // 上
                1 -> Pair(-1, -1)  // 右上
                2 -> Pair(1, 0)    // 右下
                3 -> Pair(0, 1)    // 下
                4 -> Pair(-1, 0)   // 左下
                5 -> Pair(-1, -1)  // 左上
                else -> Pair(0, 0)
            }
        }
    }

    /**
     * 最大尝试次数（防止无限循环）
     */
    private const val MAX_ATTEMPTS = 50

    /**
     * 单条河流最大长度
     */
    private const val MAX_RIVER_LENGTH = 150

    /**
     * 计算地图中央区域半径（地图短边的1/5）
     */
    private fun getCentralRadius(hexMap: HexMap): Int {
        return minOf(hexMap.width, hexMap.height) / 5
    }

    /**
     * 生成完整地图的河流系统
     * @param hexMap 地图对象
     * @param seed 随机种子（可选）
     * @return 是否成功生成河流
     */
    fun generateRivers(hexMap: HexMap, seed: Long? = null): Boolean {
        val random = if (seed != null) Random(seed) else Random

        // 清除所有现有河流（不清除防御工事）
        clearAllRivers(hexMap)

        // 计算地图中央
        val centerX = hexMap.width / 2
        val centerY = hexMap.height / 2
        val centralRadius = getCentralRadius(hexMap)

        val riverCells = mutableSetOf<Pair<Int, Int>>()

        // 生成2-3条从边缘到边缘的河流
        val numRivers = random.nextInt(2, 4)  // 2-3条

        // 边缘对：上下、左右
        val edgePairs = listOf(
            Triple("top", "bottom", listOf(3, 0)),  // 方向：进入向下，离开向上
            Triple("left", "right", listOf(4, 5, 1, 2))  // 方向：进入向左/右，离开向右/左
        )

        var riversReachingCenter = 0

        for ((startSide, endSide, validDirs) in edgePairs) {
            if (riverCells.size >= numRivers) break

            val startCells = getEdgeCellsOnSide(hexMap, startSide, validDirs)
            val endCells = getEdgeCellsOnSide(hexMap, endSide, validDirs)

            if (startCells.isEmpty() || endCells.isEmpty()) continue

            // 选择起始和结束格子
            val start = startCells.random(random)
            val end = endCells.random(random)

            // 生成河流路径
            val path = generateRiverPath(hexMap, start, end, centerX, centerY, centralRadius, random)

            if (path.isNotEmpty()) {
                riverCells.addAll(path)

                // 检查是否到达中央
                val reachesCenter = path.any { (x, y) ->
                    val dist = sqrt(((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY)).toDouble())
                    dist <= centralRadius
                }
                if (reachesCenter) riversReachingCenter++
            }
        }

        // 如果没有河流到达中央，强制生成一条穿过中央的河流
        if (riversReachingCenter == 0) {
            generateCentralRiver(hexMap, centerX, centerY, centralRadius, random, riverCells)
        }

        // 生成支流
        generateTributaries(hexMap, riverCells.toList(), random)

        // 生成后验证和修复
        validateAndFixRivers(hexMap, centerX, centerY, centralRadius, random)

        return riverCells.size >= 3
    }

    /**
     * 清除所有河流但保留防御工事
     */
    private fun clearAllRivers(hexMap: HexMap) {
        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                for (dir in 0..5) {
                    hexMap.edges[x][y][dir].hasRiver = false
                }
            }
        }
    }

    /**
     * 获取指定边缘的格子
     */
    private fun getEdgeCellsOnSide(hexMap: HexMap, side: String, validDirs: List<Int>): List<Triple<Int, Int, Int>> {
        val cells = mutableListOf<Triple<Int, Int, Int>>()

        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                val cell = hexMap.cells[x][y]
                // 跳过高山和建筑群
                if (cell.terrain == TerrainType.MOUNTAIN) continue
                if (cell.terrain == TerrainType.URBAN) continue

                when (side) {
                    "top" -> if (y == 0) cells.add(Triple(x, y, 3))  // 进入方向朝下
                    "bottom" -> if (y == hexMap.height - 1) cells.add(Triple(x, y, 0))  // 进入方向朝上
                    "left" -> if (x == 0) {
                        val dir = if (y % 2 == 0) 2 else 1
                        if (dir in validDirs) cells.add(Triple(x, y, dir))
                    }
                    "right" -> if (x == hexMap.width - 1) {
                        val dir = if (y % 2 == 0) 4 else 5
                        if (dir in validDirs) cells.add(Triple(x, y, dir))
                    }
                }
            }
        }
        return cells
    }

    /**
     * 生成河流路径（从一边缘到对侧边缘）
     */
    private fun generateRiverPath(
        hexMap: HexMap,
        start: Triple<Int, Int, Int>,  // x, y, enterDir
        end: Triple<Int, Int, Int>,
        centerX: Int, centerY: Int, centralRadius: Int,
        random: Random
    ): List<Pair<Int, Int>> {
        val path = mutableListOf<Pair<Int, Int>>()

        var currentX = start.first
        var currentY = start.second
        var prevDir = (start.third + 3) % 6  // 反向作为初始方向

        // 添加起点
        path.add(Pair(currentX, currentY))

        var steps = 0
        while (steps < MAX_RIVER_LENGTH) {
            steps++

            // 选择方向（优先向目标方向）
            val dir = chooseDirection(currentX, currentY, end.first, end.second, prevDir, random)

            val offset = getDirectionOffset(currentY, dir)
            val nextX = currentX + offset.first
            val nextY = currentY + offset.second

            // 检查是否到达目标
            if (nextX == end.first && nextY == end.second) {
                // 添加最后一条边
                if (isValidEdge(hexMap, currentX, currentY, dir)) {
                    hexMap.setRiver(currentX, currentY, dir, true)
                }
                break
            }

            // 检查是否在地图范围内
            if (!hexMap.isValidCell(nextX, nextY)) {
                // 河流流出地图，停止
                break
            }

            // 检查地形
            val nextCell = hexMap.cells[nextX][nextY]
            if (nextCell.terrain == TerrainType.MOUNTAIN || nextCell.terrain == TerrainType.URBAN) {
                break
            }

            // 检查边是否有效
            if (!isValidEdge(hexMap, currentX, currentY, dir)) {
                break
            }

            // 添加河流边
            hexMap.setRiver(currentX, currentY, dir, true)
            path.add(Pair(nextX, nextY))

            // 移动到下一个格子
            currentX = nextX
            currentY = nextY
            prevDir = (dir + 3) % 6
        }

        return path
    }

    /**
     * 生成穿过中央的河流
     */
    private fun generateCentralRiver(
        hexMap: HexMap,
        centerX: Int, centerY: Int, centralRadius: Int,
        random: Random,
        riverCells: MutableSet<Pair<Int, Int>>
    ) {
        // 随机选择水平或垂直穿越
        if (random.nextBoolean()) {
            // 水平穿越（左右边缘）
            val y = centerY.coerceIn(0, hexMap.height - 1)
            val startX = 0
            val endX = hexMap.width - 1

            for (x in startX until endX) {
                val cell = hexMap.cells[x][y]
                if (cell.terrain == TerrainType.MOUNTAIN || cell.terrain == TerrainType.URBAN) continue

                val nextX = x + 1
                if (nextX >= hexMap.width) break

                val nextCell = hexMap.cells[nextX][y]
                if (nextCell.terrain == TerrainType.MOUNTAIN || nextCell.terrain == TerrainType.URBAN) continue

                hexMap.setRiver(x, y, 2, true)  // 向右
                riverCells.add(Pair(x, y))
            }
        } else {
            // 垂直穿越（上下边缘）
            val x = centerX.coerceIn(0, hexMap.width - 1)
            val startY = 0
            val endY = hexMap.height - 1

            for (y in startY until endY) {
                val cell = hexMap.cells[x][y]
                if (cell.terrain == TerrainType.MOUNTAIN || cell.terrain == TerrainType.URBAN) continue

                hexMap.setRiver(x, y, 3, true)  // 向下
                riverCells.add(Pair(x, y))
            }
        }
    }

    /**
     * 生成支流
     */
    private fun generateTributaries(
        hexMap: HexMap,
        riverCells: List<Pair<Int, Int>>,
        random: Random
    ) {
        if (riverCells.size < 3) return

        // 选择2-4个节点生成支流
        val numBranches = random.nextInt(2, 5)
        val candidates = riverCells.filterIndexed { index, _ ->
            index > 0 && index < riverCells.size - 1
        }

        for (i in 0 until minOf(numBranches, candidates.size)) {
            val cell = candidates.random(random)
            val branchDir = random.nextInt(0, 6)
            val branchLength = random.nextInt(2, 8)

            generateBranch(hexMap, cell.first, cell.second, branchDir, branchLength, random)
        }
    }

    /**
     * 生成单条支流
     */
    private fun generateBranch(
        hexMap: HexMap,
        startX: Int, startY: Int,
        direction: Int,
        maxLength: Int,
        random: Random
    ) {
        var currentX = startX
        var currentY = startY
        var prevDir = direction

        for (step in 0 until maxLength) {
            val dir = if (random.nextFloat() < 0.85f) {
                prevDir
            } else {
                if (random.nextBoolean()) (prevDir + 1) % 6 else (prevDir + 5) % 6
            }

            val offset = getDirectionOffset(currentY, dir)
            val nextX = currentX + offset.first
            val nextY = currentY + offset.second

            if (!hexMap.isValidCell(nextX, nextY)) break

            val nextCell = hexMap.cells[nextX][nextY]
            if (nextCell.terrain == TerrainType.MOUNTAIN || nextCell.terrain == TerrainType.URBAN) break

            if (!isValidEdge(hexMap, currentX, currentY, dir)) break

            hexMap.setRiver(currentX, currentY, dir, true)
            currentX = nextX
            currentY = nextY
            prevDir = (dir + 3) % 6
        }
    }

    /**
     * 选择方向
     */
    private fun chooseDirection(
        currentX: Int, currentY: Int,
        targetX: Int, targetY: Int,
        prevDir: Int,
        random: Random
    ): Int {
        val dx = targetX - currentX
        val dy = targetY - currentY

        val preferredDirs = mutableListOf<Int>()

        // 垂直方向
        if (dy < -1) preferredDirs.add(0)  // 向上
        else if (dy > 1) preferredDirs.add(3)  // 向下

        // 水平方向（根据奇偶行）
        if (dx < -1) {
            if (currentY % 2 == 0) preferredDirs.add(5) else preferredDirs.add(4)
        } else if (dx > 1) {
            if (currentY % 2 == 0) preferredDirs.add(2) else preferredDirs.add(1)
        }

        // 80%概率选择优先方向
        if (preferredDirs.isNotEmpty() && random.nextFloat() < 0.8f) {
            return preferredDirs.random(random)
        }

        // 惯性方向
        return if (random.nextFloat() < 0.85f) {
            listOf(prevDir, (prevDir + 1) % 6, (prevDir + 5) % 6).random(random)
        } else {
            listOf((prevDir + 2) % 6, (prevDir + 4) % 6).random(random)
        }
    }

    /**
     * 检查边是否有效
     */
    private fun isValidEdge(hexMap: HexMap, x: Int, y: Int, direction: Int): Boolean {
        if (!hexMap.isValidCell(x, y)) return false

        val cell = hexMap.cells[x][y]
        if (cell.terrain == TerrainType.MOUNTAIN || cell.terrain == TerrainType.URBAN) return false

        val offset = getDirectionOffset(y, direction)
        val neighborX = x + offset.first
        val neighborY = y + offset.second

        if (!hexMap.isValidCell(neighborX, neighborY)) return true

        val neighborCell = hexMap.cells[neighborX][neighborY]
        return neighborCell.terrain != TerrainType.MOUNTAIN && neighborCell.terrain != TerrainType.URBAN
    }

    /**
     * 验证并修复河流
     * 1. 确保河流两端都在边缘
     * 2. 确保至少一条河流到达中央
     */
    private fun validateAndFixRivers(
        hexMap: HexMap,
        centerX: Int, centerY: Int, centralRadius: Int,
        random: Random
    ) {
        // 找到所有河流边
        val riverEdges = mutableListOf<Triple<Int, Int, Int>>()  // x, y, dir

        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                for (dir in 0..5) {
                    if (hexMap.edges[x][y][dir].hasRiver) {
                        riverEdges.add(Triple(x, y, dir))
                    }
                }
            }
        }

        if (riverEdges.isEmpty()) return

        // 检查是否有河流到达中央
        var hasCentralRiver = false
        for ((x, y, _) in riverEdges) {
            val dist = sqrt(((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY)).toDouble())
            if (dist <= centralRadius) {
                hasCentralRiver = true
                break
            }
        }

        // 如果没有中央河流，从最近的河流延伸一条到中央
        if (!hasCentralRiver) {
            extendToCenter(hexMap, centerX, centerY, centralRadius, random)
        }

        // 检查河流是否两端都在边缘，如果不是则补全
        extendToEdges(hexMap, random)
    }

    /**
     * 延伸河流到中央
     */
    private fun extendToCenter(
        hexMap: HexMap,
        centerX: Int, centerY: Int, centralRadius: Int,
        random: Random
    ) {
        // 找到离中央最近的河流格子
        var closestCell: Pair<Int, Int>? = null
        var minDist = Double.MAX_VALUE

        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                for (dir in 0..5) {
                    if (hexMap.edges[x][y][dir].hasRiver) {
                        val dist = sqrt(((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY)).toDouble())
                        if (dist < minDist) {
                            minDist = dist
                            closestCell = Pair(x, y)
                        }
                    }
                }
            }
        }

        if (closestCell == null) return

        // 从该格子延伸一条河流到中央
        var currentX = closestCell.first
        var currentY = closestCell.second
        var prevDir = 0

        var steps = 0
        while (steps < 50) {
            steps++

            val dir = chooseDirection(currentX, currentY, centerX, centerY, prevDir, random)
            val offset = getDirectionOffset(currentY, dir)
            val nextX = currentX + offset.first
            val nextY = currentY + offset.second

            val dist = sqrt(((nextX - centerX) * (nextX - centerX) + (nextY - centerY) * (nextY - centerY)).toDouble())
            if (dist <= centralRadius) {
                // 到达中央
                if (isValidEdge(hexMap, currentX, currentY, dir)) {
                    hexMap.setRiver(currentX, currentY, dir, true)
                }
                break
            }

            if (!hexMap.isValidCell(nextX, nextY)) break

            val nextCell = hexMap.cells[nextX][nextY]
            if (nextCell.terrain == TerrainType.MOUNTAIN || nextCell.terrain == TerrainType.URBAN) break

            if (!isValidEdge(hexMap, currentX, currentY, dir)) break

            hexMap.setRiver(currentX, currentY, dir, true)
            currentX = nextX
            currentY = nextY
            prevDir = (dir + 3) % 6
        }
    }

    /**
     * 延伸河流到地图边缘
     * 找到所有河流端点（只有一个方向有河流的格子），然后延伸到地图边缘
     */
    private fun extendToEdges(hexMap: HexMap, random: Random) {
        // 找到所有河流端点（只有一个方向有河流的格子）
        val endpoints = mutableListOf<Triple<Int, Int, Int>>()  // x, y, exitDir

        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                val riverDirs = mutableListOf<Int>()
                for (dir in 0..5) {
                    if (hexMap.edges[x][y][dir].hasRiver) {
                        riverDirs.add(dir)
                    }
                }

                // 端点：只有一个方向有河流
                if (riverDirs.size == 1) {
                    val exitDir = riverDirs[0]
                    val offset = getDirectionOffset(y, exitDir)
                    val neighborX = x + offset.first
                    val neighborY = y + offset.second

                    // 如果邻居不在地图内，这个端点已经在边缘了
                    if (!hexMap.isValidCell(neighborX, neighborY)) {
                        continue
                    }

                    endpoints.add(Triple(x, y, exitDir))
                }
            }
        }

        // 延伸每个端点到地图边缘
        for ((startX, startY, startDir) in endpoints) {
            extendRiverEndpoint(hexMap, startX, startY, startDir, random)
        }
    }

    /**
     * 延伸单个河流端点到地图边缘
     */
    private fun extendRiverEndpoint(
        hexMap: HexMap,
        startX: Int,
        startY: Int,
        startDir: Int,
        random: Random
    ) {
        var currentX = startX
        var currentY = startY
        var prevDir = startDir

        // 沿着开始方向继续延伸
        var dir = startDir

        for (step in 0 until 50) {
            val offset = getDirectionOffset(currentY, dir)
            val nextX = currentX + offset.first
            val nextY = currentY + offset.second

            // 检查是否到达地图边缘
            if (!hexMap.isValidCell(nextX, nextY)) {
                // 已经到达地图边缘，设置河流边
                if (isValidEdge(hexMap, currentX, currentY, dir)) {
                    hexMap.setRiver(currentX, currentY, dir, true)
                }
                break
            }

            // 检查地形
            val nextCell = hexMap.cells[nextX][nextY]
            if (nextCell.terrain == TerrainType.MOUNTAIN || nextCell.terrain == TerrainType.URBAN) {
                break
            }

            // 检查边是否有效
            if (!isValidEdge(hexMap, currentX, currentY, dir)) {
                break
            }

            // 添加河流边
            hexMap.setRiver(currentX, currentY, dir, true)

            // 检查下一个格子是否是端点（只有一个方向有河流）
            val nextRiverDirs = mutableListOf<Int>()
            for (d in 0..5) {
                if (hexMap.edges[nextX][nextY][d].hasRiver) {
                    nextRiverDirs.add(d)
                }
            }

            // 如果下一个格子已经有多条河流边，不再继续延伸
            if (nextRiverDirs.size > 1) {
                break
            }

            // 继续延伸
            currentX = nextX
            currentY = nextY
            prevDir = (dir + 3) % 6

            // 偶尔改变方向
            if (random.nextFloat() < 0.3f) {
                dir = if (random.nextBoolean()) (prevDir + 1) % 6 else (prevDir + 5) % 6
            }
        }
    }
}
