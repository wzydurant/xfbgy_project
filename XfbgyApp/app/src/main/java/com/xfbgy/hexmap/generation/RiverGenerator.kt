package com.xfbgy.hexmap.generation

import com.xfbgy.hexmap.HexMap
import kotlin.random.Random

/**
 * 河流生成器
 * 使用随机游走算法生成河流
 */
class RiverGenerator(private var seed: Long = System.currentTimeMillis()) {

    /**
     * 生成河流
     * @param hexMap 目标地图
     * @param seed 随机种子（可选）
     */
    fun generateRivers(hexMap: HexMap, seed: Long? = null) {
        val random = Random(seed ?: this.seed)

        // 在地图边缘随机选取1~3个出发点
        val entryPoints = generateEntryPoints(hexMap, random)

        for ((startX, startY, startDir) in entryPoints) {
            randomWalk(hexMap, startX, startY, startDir, random)
        }

        // 验证连通性（如果存在河流）
        if (!validateConnectivity(hexMap)) {
            // 不连通则重新生成（简化处理：清除河流后重试一次）
            clearRivers(hexMap)
            generateRivers(hexMap, random.nextLong())
        }
    }

    /**
     * 生成河流入口点
     */
    private fun generateEntryPoints(hexMap: HexMap, random: Random): List<Triple<Int, Int, Int>> {
        val entryPoints = mutableListOf<Triple<Int, Int, Int>>()
        val numEntries = random.nextInt(1, 4) // 1~3个入口

        repeat(numEntries) {
            val edge = random.nextInt(4) // 0=上, 1=右, 2=下, 3=左
            when (edge) {
                0 -> { // 上边缘
                    val x = random.nextInt(hexMap.width)
                    entryPoints.add(Triple(x, 0, 2)) // 向下流
                }
                1 -> { // 右边缘
                    val y = random.nextInt(hexMap.height)
                    entryPoints.add(Triple(hexMap.width - 1, y, 4)) // 向左流
                }
                2 -> { // 下边缘
                    val x = random.nextInt(hexMap.width)
                    entryPoints.add(Triple(x, hexMap.height - 1, 0)) // 向上流
                }
                3 -> { // 左边缘
                    val y = random.nextInt(hexMap.height)
                    entryPoints.add(Triple(0, y, 1)) // 向右上流
                }
            }
        }

        return entryPoints
    }

    /**
     * 随机游走生成河流
     */
    private fun randomWalk(hexMap: HexMap, startX: Int, startY: Int, direction: Int, random: Random) {
        var x = startX
        var y = startY
        var currentDir = direction
        var steps = 0
        val maxSteps = hexMap.width * hexMap.height

        while (steps < maxSteps) {
            if (x !in 0 until hexMap.width || y !in 0 until hexMap.height) {
                break // 流出地图
            }

            // 设置河流
            hexMap.setEdgeRiver(x, y, currentDir, true)

            // 决定下一步方向
            // 主干方向有较高概率，分支约20%
            currentDir = if (random.nextFloat() < 0.2f) {
                // 分支：选择主方向的相邻方向
                val alternatives = listOf(
                    (currentDir + 1) % 6,
                    (currentDir + 5) % 6
                )
                alternatives[random.nextInt(alternatives.size)]
            } else {
                // 继续主干：略微随机
                (currentDir + random.nextInt(-1, 2) + 6) % 6
            }

            // 移动到下一个格子
            val offsets = getNeighborOffsets(y % 2 == 1)
            val offset = offsets[currentDir]
            x += offset[0]
            y += offset[1]

            steps++
        }
    }

    /**
     * 获取邻居偏移
     */
    private fun getNeighborOffsets(isOddRow: Boolean): Array<IntArray> {
        return if (isOddRow) {
            arrayOf(
                intArrayOf(0, -1),   // 上
                intArrayOf(1, -1),   // 右上
                intArrayOf(1, 0),    // 右下
                intArrayOf(0, 1),    // 下
                intArrayOf(-1, 0),   // 左下
                intArrayOf(-1, -1)   // 左上
            )
        } else {
            arrayOf(
                intArrayOf(0, -1),   // 上
                intArrayOf(1, 0),    // 右上
                intArrayOf(1, 1),    // 右下
                intArrayOf(0, 1),    // 下
                intArrayOf(-1, 1),   // 左下
                intArrayOf(-1, 0)    // 左上
            )
        }
    }

    /**
     * 验证河流连通性
     */
    private fun validateConnectivity(hexMap: HexMap): Boolean {
        val riverEdges = getAllRiverEdges(hexMap)
        if (riverEdges.isEmpty()) return true

        // BFS检查连通性
        val visited = mutableSetOf<String>()
        val queue = ArrayDeque<Pair<Int, Int>>()
        queue.add(riverEdges.first())

        while (queue.isNotEmpty()) {
            val (x, y) = queue.removeFirst()
            val key = "$x,$y"
            if (visited.contains(key)) continue
            visited.add(key)

            for (dir in 0..5) {
                if (hexMap.getEdge(x, y, dir).hasRiver) {
                    val (nx, ny) = getNeighborCoords(x, y, dir, hexMap)
                    if (nx in 0 until hexMap.width && ny in 0 until hexMap.height) {
                        queue.add(Pair(nx, ny))
                    }
                }
            }
        }

        return visited.size == riverEdges.size
    }

    /**
     * 获取所有河流边
     */
    private fun getAllRiverEdges(hexMap: HexMap): Set<Pair<Int, Int>> {
        val edges = mutableSetOf<Pair<Int, Int>>()
        for (x in 0 until hexMap.width) {
            for (y in 0 until hexMap.height) {
                for (dir in 0..5) {
                    if (hexMap.getEdge(x, y, dir).hasRiver) {
                        edges.add(Pair(x, y))
                    }
                }
            }
        }
        return edges
    }

    /**
     * 获取相邻格子坐标
     */
    private fun getNeighborCoords(x: Int, y: Int, dir: Int, hexMap: HexMap): Pair<Int, Int> {
        val isOddRow = y % 2 == 1
        val offsets = if (isOddRow) {
            arrayOf(
                intArrayOf(0, -1),
                intArrayOf(1, -1),
                intArrayOf(1, 0),
                intArrayOf(0, 1),
                intArrayOf(-1, 0),
                intArrayOf(-1, -1)
            )
        } else {
            arrayOf(
                intArrayOf(0, -1),
                intArrayOf(1, 0),
                intArrayOf(1, 1),
                intArrayOf(0, 1),
                intArrayOf(-1, 1),
                intArrayOf(-1, 0)
            )
        }
        return Pair(x + offsets[dir][0], y + offsets[dir][1])
    }

    /**
     * 清除所有河流
     */
    private fun clearRivers(hexMap: HexMap) {
        for (x in 0 until hexMap.width) {
            for (y in 0 until hexMap.height) {
                for (dir in 0..5) {
                    hexMap.getEdge(x, y, dir).hasRiver = false
                }
            }
        }
    }
}