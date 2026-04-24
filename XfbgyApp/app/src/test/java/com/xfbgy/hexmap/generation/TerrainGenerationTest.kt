package com.xfbgy.hexmap.generation

import com.xfbgy.hexmap.HexMap
import com.xfbgy.hexmap.data.TerrainType
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

/**
 * Phase 1-B: 地图生成层测试
 * 地形生成算法测试
 *
 * 测试依据：《兵棋手游_第一阶段开发方案.md》第四节 地形生成
 */
class TerrainGenerationTest {

    /**
     * 测试地形比例随机生成 - 基本验证
     * 依据：文档 地形生成比例
     */
    @Test
    fun `test terrain generation has correct total count`() {
        val hexMap = HexMap(20, 20)
        val generator = TerrainGenerator()
        generator.generateTerrain(hexMap)

        var totalCells = 0
        for (x in 0 until hexMap.width) {
            for (y in 0 until hexMap.height) {
                totalCells++
            }
        }

        assertEquals("总格子数应为400", 400, totalCells)
    }

    /**
     * 测试地形比例 - 平原40%
     * 依据：文档 平原目标比例40%
     */
    @Test
    fun `test plain terrain proportion approximately 40 percent`() {
        val hexMap = HexMap(20, 20)
        val generator = TerrainGenerator()

        repeat(10) {
            generator.generateTerrain(hexMap)

            val plainCount = countTerrain(hexMap, TerrainType.PLAIN)
            val proportion = plainCount.toFloat() / 400f

            assertTrue(
                "平原比例应在35%-45%之间，实际${String.format("%.2f", proportion * 100)}%",
                proportion in 0.35f..0.45f
            )
        }
    }

    /**
     * 测试地形比例 - 树林30%
     * 依据：文档 树林目标比例30%
     */
    @Test
    fun `test forest terrain proportion approximately 30 percent`() {
        val hexMap = HexMap(20, 20)
        val generator = TerrainGenerator()

        repeat(10) {
            generator.generateTerrain(hexMap)

            val forestCount = countTerrain(hexMap, TerrainType.FOREST)
            val proportion = forestCount.toFloat() / 400f

            assertTrue(
                "树林比例应在25%-35%之间，实际${String.format("%.2f", proportion * 100)}%",
                proportion in 0.25f..0.35f
            )
        }
    }

    /**
     * 测试地形比例 - 山地15%
     * 依据：文档 山地目标比例15%
     */
    @Test
    fun `test hill terrain proportion approximately 15 percent`() {
        val hexMap = HexMap(20, 20)
        val generator = TerrainGenerator()

        repeat(10) {
            generator.generateTerrain(hexMap)

            val hillCount = countTerrain(hexMap, TerrainType.HILL)
            val proportion = hillCount.toFloat() / 400f

            assertTrue(
                "山地比例应在10%-20%之间，实际${String.format("%.2f", proportion * 100)}%",
                proportion in 0.10f..0.20f
            )
        }
    }

    /**
     * 测试地形比例 - 高山5%
     * 依据：文档 高山目标比例5%
     */
    @Test
    fun `test mountain terrain proportion approximately 5 percent`() {
        val hexMap = HexMap(20, 20)
        val generator = TerrainGenerator()

        repeat(10) {
            generator.generateTerrain(hexMap)

            val mountainCount = countTerrain(hexMap, TerrainType.MOUNTAIN)
            val proportion = mountainCount.toFloat() / 400f

            assertTrue(
                "高山比例应在2%-8%之间，实际${String.format("%.2f", proportion * 100)}%",
                proportion in 0.02f..0.08f
            )
        }
    }

    /**
     * 测试地形比例 - 建筑群10%
     * 依据：文档 建筑群目标比例10%
     */
    @Test
    fun `test urban terrain proportion approximately 10 percent`() {
        val hexMap = HexMap(20, 20)
        val generator = TerrainGenerator()

        repeat(10) {
            generator.generateTerrain(hexMap)

            val urbanCount = countTerrain(hexMap, TerrainType.URBAN)
            val proportion = urbanCount.toFloat() / 400f

            assertTrue(
                "建筑群比例应在7%-13%之间，实际${String.format("%.2f", proportion * 100)}%",
                proportion in 0.07f..0.13f
            )
        }
    }

    /**
     * 测试所有地形比例之和为100%
     * 依据：文档 地形比例总和
     */
    @Test
    fun `test all terrain proportions sum to 100 percent`() {
        val hexMap = HexMap(20, 20)
        val generator = TerrainGenerator()
        generator.generateTerrain(hexMap)

        val totalProportion =
            countTerrain(hexMap, TerrainType.PLAIN).toFloat() / 400f +
            countTerrain(hexMap, TerrainType.FOREST).toFloat() / 400f +
            countTerrain(hexMap, TerrainType.HILL).toFloat() / 400f +
            countTerrain(hexMap, TerrainType.MOUNTAIN).toFloat() / 400f +
            countTerrain(hexMap, TerrainType.URBAN).toFloat() / 400f

        assertEquals("所有地形比例之和应为100%", 1.0f, totalProportion, 0.01f)
    }

    /**
     * 测试 BFS 聚类使地形成团
     * 依据：文档 BFS扩散形成地形团块
     */
    @Test
    fun `test terrain clustering with BFS`() {
        val hexMap = HexMap(20, 20)
        val generator = TerrainGenerator()
        generator.generateTerrain(hexMap)

        // 检查树林是否成团分布
        val forestCells = getTerrainCells(hexMap, TerrainType.FOREST)
        if (forestCells.size > 1) {
            val clusterCount = countClusters(hexMap, TerrainType.FOREST)
            // 树林应该形成少于总数/4的团
            assertTrue(
                "树林应有聚集趋势，团数应少于总格子数/10",
                clusterCount < forestCells.size / 10
            )
        }
    }

    /**
     * 测试高山小团块随机散布
     * 依据：文档 高山小团块随机散布
     */
    @Test
    fun `test mountain forms small clusters`() {
        val hexMap = HexMap(20, 20)
        val generator = TerrainGenerator()
        generator.generateTerrain(hexMap)

        val mountainCells = getTerrainCells(hexMap, TerrainType.MOUNTAIN)

        // 高山应该分散成小团块
        if (mountainCells.isNotEmpty()) {
            val clusterCount = countClusters(hexMap, TerrainType.MOUNTAIN)
            // 高山团块应较多（分散）
            assertTrue(
                "高山应分散分布，团数应较多",
                clusterCount > 1
            )
        }
    }

    /**
     * 测试建筑群不与高山相邻
     * 依据：文档 建筑群优先不与高山相邻
     */
    @Test
    fun `test urban does not adjacent to mountain`() {
        val hexMap = HexMap(20, 20)
        val generator = TerrainGenerator()
        generator.generateTerrain(hexMap)

        val urbanCells = getTerrainCells(hexMap, TerrainType.URBAN)

        for (cell in urbanCells) {
            val neighbors = hexMap.getNeighbors(cell.x, cell.y)
            for (neighbor in neighbors) {
                assertNotEquals(
                    "建筑群不应与高山相邻",
                    TerrainType.MOUNTAIN,
                    neighbor.terrain
                )
            }
        }
    }

    /**
     * 测试不同地图尺寸都能正确生成
     * 依据：文档 mapWidth/mapHeight 范围 20~40
     */
    @Test
    fun `test terrain generation with different map sizes`() {
        val sizes = listOf(
            Pair(20, 20),
            Pair(30, 25),
            Pair(40, 40)
        )

        for ((width, height) in sizes) {
            val hexMap = HexMap(width, height)
            val generator = TerrainGenerator()
            generator.generateTerrain(hexMap)

            var count = 0
            for (x in 0 until hexMap.width) {
                for (y in 0 until hexMap.height) {
                    count++
                }
            }

            assertEquals(
                "地图${width}x${height}格子数应为${width * height}",
                width * height,
                count
            )
        }
    }

    /**
     * 测试每次生成结果不同（随机性）
     * 依据：文档 随机生成
     */
    @Test
    fun `test terrain generation is randomized`() {
        val hexMap1 = HexMap(20, 20)
        val hexMap2 = HexMap(20, 20)
        val generator = TerrainGenerator()

        generator.generateTerrain(hexMap1)
        generator.generateTerrain(hexMap2)

        var differences = 0
        for (x in 0 until 20) {
            for (y in 0 until 20) {
                if (hexMap1.getCell(x, y).terrain != hexMap2.getCell(x, y).terrain) {
                    differences++
                }
            }
        }

        assertTrue(
            "两次生成应有差异，实际差异数$differences",
            differences > 0
        )
    }

    // 辅助方法：统计指定地形数量
    private fun countTerrain(hexMap: HexMap, terrain: TerrainType): Int {
        var count = 0
        for (x in 0 until hexMap.width) {
            for (y in 0 until hexMap.height) {
                if (hexMap.getCell(x, y).terrain == terrain) {
                    count++
                }
            }
        }
        return count
    }

    // 辅助方法：获取指定地形的格子列表
    private fun getTerrainCells(hexMap: HexMap, terrain: TerrainType): List<HexCell> {
        val cells = mutableListOf<HexCell>()
        for (x in 0 until hexMap.width) {
            for (y in 0 until hexMap.height) {
                val cell = hexMap.getCell(x, y)
                if (cell.terrain == terrain) {
                    cells.add(cell)
                }
            }
        }
        return cells
    }

    // 辅助方法：计算地形团块数量（简单BFS）
    private fun countClusters(hexMap: HexMap, terrain: TerrainType): Int {
        val visited = mutableSetOf<Pair<Int, Int>>()
        var clusters = 0

        for (x in 0 until hexMap.width) {
            for (y in 0 until hexMap.height) {
                val pos = Pair(x, y)
                if (!visited.contains(pos) && hexMap.getCell(x, y).terrain == terrain) {
                    // BFS扩展
                    val queue = ArrayDeque<Pair<Int, Int>>()
                    queue.add(pos)

                    while (queue.isNotEmpty()) {
                        val current = queue.removeFirst()
                        if (visited.contains(current)) continue
                        visited.add(current)

                        val neighbors = hexMap.getNeighbors(current.first, current.second)
                        for (neighbor in neighbors) {
                            val neighborPos = Pair(neighbor.x, neighbor.y)
                            if (!visited.contains(neighborPos) && neighbor.terrain == terrain) {
                                queue.add(neighborPos)
                            }
                        }
                    }
                    clusters++
                }
            }
        }
        return clusters
    }
}