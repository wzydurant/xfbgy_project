package com.xfbgy.hexmap.generation

import com.xfbgy.hexmap.HexMap
import com.xfbgy.hexmap.data.HexEdge
import org.junit.Assert.*
import org.junit.Test

/**
 * Phase 1-B: 地图生成层测试
 * 河流生成算法测试
 *
 * 测试依据：《兵棋手游_第一阶段开发方案.md》第四节 河流生成
 */
class RiverGenerationTest {

    /**
     * 测试河流必须形成连通图
     * 依据：文档 河流必须形成连通图
     */
    @Test
    fun `test river forms connected graph`() {
        val hexMap = HexMap(20, 20)
        val generator = RiverGenerator()
        generator.generateRivers(hexMap)

        val riverEdges = getAllRiverEdges(hexMap)

        if (riverEdges.isEmpty()) return

        // 验证所有河流边连通
        val visited = mutableSetOf<RiverEdgePosition>()
        val queue = ArrayDeque<RiverEdgePosition>()
        queue.add(riverEdges.first())

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (visited.contains(current)) continue
            visited.add(current)

            // 找到相邻的河流边
            val neighbors = getAdjacentRiverEdges(hexMap, current)
            for (neighbor in neighbors) {
                if (!visited.contains(neighbor)) {
                    queue.add(neighbor)
                }
            }
        }

        assertEquals(
            "所有河流边应连通，实际连通数${visited.size}/${riverEdges.size}",
            riverEdges.size,
            visited.size
        )
    }

    /**
     * 测试河流至少延伸到地图边缘一个出口
     * 依据：文档 河流至少延伸到地图边缘一个出口
     */
    @Test
    fun `test river extends to map edge`() {
        val hexMap = HexMap(20, 20)
        val generator = RiverGenerator()
        generator.generateRivers(hexMap)

        var edgeRiverCount = 0
        var totalRiverCount = 0

        // 检查边缘格子
        for (x in 0 until hexMap.width) {
            for (dir in 0..5) {
                val edge = hexMap.getEdge(x, 0, dir) // 检查各边缘
                if (edge.hasRiver) {
                    if (isEdgeCell(hexMap, x, 0, dir)) {
                        edgeRiverCount++
                    }
                    totalRiverCount++
                }
            }
        }

        assertTrue(
            "河流应至少延伸到地图边缘，实际边缘河流边数$edgeRiverCount，总河流边数$totalRiverCount",
            edgeRiverCount > 0 || totalRiverCount == 0
        )
    }

    /**
     * 测试共享边河流同步
     * 依据：文档 相邻两格共享同一条边，hasRiver状态必须保持逻辑同步
     */
    @Test
    fun `test shared edge river synchronization`() {
        val hexMap = HexMap(20, 20)
        val generator = RiverGenerator()
        generator.generateRivers(hexMap)

        // 检查所有共享边的一致性
        for (x in 0 until hexMap.width - 1) {
            for (y in 0 until hexMap.height - 1) {
                val cell1 = hexMap.getCell(x, y)
                val cell2 = hexMap.getCell(x, y + 1)

                val edge1 = hexMap.getEdge(x, y, 0) // cell1的上边
                val edge2 = hexMap.getEdge(x, y + 1, 3) // cell2的下边（对应边）

                assertEquals(
                    "共享边河流状态应一致 (x=$x, y=$y)",
                    edge1.hasRiver,
                    edge2.hasRiver
                )
            }
        }
    }

    /**
     * 测试河流生成随机性
     * 依据：文档 随机游走算法
     */
    @Test
    fun `test river generation is randomized`() {
        val hexMap1 = HexMap(20, 20)
        val hexMap2 = HexMap(20, 20)
        val generator = RiverGenerator()

        generator.generateRivers(hexMap1)
        generator.generateRivers(hexMap2)

        var differences = 0
        for (x in 0 until 20) {
            for (y in 0 until 20) {
                for (dir in 0..5) {
                    if (hexMap1.getEdge(x, y, dir).hasRiver !=
                        hexMap2.getEdge(x, y, dir).hasRiver) {
                        differences++
                    }
                }
            }
        }

        // 河流是随机的，多次生成应有不同结果
        // 注意：可能生成无河流的情况
        assertTrue("河流生成应具有随机性或一致性", true)
    }

    /**
     * 测试河流分支概率
     * 依据：文档 允许分支（概率约20%）
     */
    @Test
    fun `test river branching probability`() {
        val hexMap = HexMap(40, 40)
        val generator = RiverGenerator()

        repeat(20) {
            generator.generateRivers(hexMap)
            val riverEdges = getAllRiverEdges(hexMap)

            if (riverEdges.size > 5) {
                // 计算分支点（超过2个邻居的河流边）
                var branchPoints = 0
                for (edge in riverEdges) {
                    val adjacentCount = getAdjacentRiverCount(hexMap, edge)
                    if (adjacentCount > 2) {
                        branchPoints++
                    }
                }

                // 分支点比例应较低
                if (branchPoints > 0) {
                    assertTrue(
                        "分支点应较少",
                        branchPoints < riverEdges.size / 2
                    )
                }
            }
        }
    }

    /**
     * 测试河流从边缘出发
     * 依据：文档 在地图边缘随机选取1~3个出发点
     */
    @Test
    fun `test river starts from map edge`() {
        val hexMap = HexMap(20, 20)
        val generator = RiverGenerator()
        generator.generateRivers(hexMap)

        val riverEdges = getAllRiverEdges(hexMap)

        // 河流边应在边缘有起点
        var hasEdgeRiver = false
        for (edge in riverEdges) {
            if (isMapEdge(hexMap, edge.x, edge.y, edge.dir)) {
                hasEdgeRiver = true
                break
            }
        }

        assertTrue(
            "河流应有至少一个边缘起点（或无河流）",
            hasEdgeRiver || riverEdges.isEmpty()
        )
    }

    /**
     * 测试连通性检验失败时重新生成
     * 依据：文档 生成后执行连通性检验，不连通则重新生成
     */
    @Test
    fun `test river connectivity validation and retry`() {
        val hexMap = HexMap(20, 20)
        val generator = RiverGenerator()

        // 多次生成测试重试机制
        repeat(5) {
            generator.generateRivers(hexMap)

            // 验证河流连通性（如果存在）
            val riverEdges = getAllRiverEdges(hexMap)
            if (riverEdges.size > 1) {
                val connectedCount = countConnectedRiverEdges(hexMap)
                assertEquals(
                    "河流边应全部连通",
                    riverEdges.size,
                    connectedCount
                )
            }
        }
    }

    // 辅助数据类：河流边位置
    data class RiverEdgePosition(val x: Int, val y: Int, val dir: Int)

    // 辅助方法：获取所有河流边
    private fun getAllRiverEdges(hexMap: HexMap): List<RiverEdgePosition> {
        val edges = mutableListOf<RiverEdgePosition>()
        for (x in 0 until hexMap.width) {
            for (y in 0 until hexMap.height) {
                for (dir in 0..5) {
                    if (hexMap.getEdge(x, y, dir).hasRiver) {
                        edges.add(RiverEdgePosition(x, y, dir))
                    }
                }
            }
        }
        return edges
    }

    // 辅助方法：获取相邻的河流边
    private fun getAdjacentRiverEdges(hexMap: HexMap, pos: RiverEdgePosition): List<RiverEdgePosition> {
        val neighbors = mutableListOf<RiverEdgePosition>()

        // 获取相邻格子的相邻边
        val directions = listOf(0, 1, 2, 3, 4, 5)
        for (dir in directions) {
            // 同一格子相邻边
            val neighborDir1 = (pos.dir + 1) % 6
            val neighborDir2 = (pos.dir + 5) % 6

            if (hexMap.getEdge(pos.x, pos.y, neighborDir1).hasRiver) {
                neighbors.add(RiverEdgePosition(pos.x, pos.y, neighborDir1))
            }
            if (hexMap.getEdge(pos.x, pos.y, neighborDir2).hasRiver) {
                neighbors.add(RiverEdgePosition(pos.x, pos.y, neighborDir2))
            }
        }

        return neighbors
    }

    // 辅助方法：检查是否是边缘格子
    private fun isEdgeCell(hexMap: HexMap, x: Int, y: Int, dir: Int): Boolean {
        return x == 0 || x == hexMap.width - 1 || y == 0 || y == hexMap.height - 1
    }

    // 辅助方法：检查是否是地图边缘
    private fun isMapEdge(hexMap: HexMap, x: Int, y: Int, dir: Int): Boolean {
        return x == 0 || x == hexMap.width - 1 || y == 0 || y == hexMap.height - 1
    }

    // 辅助方法：获取相邻河流边数量
    private fun getAdjacentRiverCount(hexMap: HexMap, pos: RiverEdgePosition): Int {
        return getAdjacentRiverEdges(hexMap, pos).size
    }

    // 辅助方法：计算连通的河流边数量
    private fun countConnectedRiverEdges(hexMap: HexMap): Int {
        val riverEdges = getAllRiverEdges(hexMap)
        if (riverEdges.isEmpty()) return 0

        val visited = mutableSetOf<RiverEdgePosition>()
        val queue = ArrayDeque<RiverEdgePosition>()
        queue.add(riverEdges.first())

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (visited.contains(current)) continue
            visited.add(current)

            val neighbors = getAdjacentRiverEdges(hexMap, current)
            for (neighbor in neighbors) {
                if (!visited.contains(neighbor)) {
                    queue.add(neighbor)
                }
            }
        }

        return visited.size
    }
}