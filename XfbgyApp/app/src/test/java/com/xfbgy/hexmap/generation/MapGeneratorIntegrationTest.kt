package com.xfbgy.hexmap.generation

import com.xfbgy.hexmap.HexMap
import com.xfbgy.hexmap.data.TerrainType
import org.junit.Assert.*
import org.junit.Test

/**
 * Phase 1-B: 地图生成层测试
 * 完整地图生成集成测试
 *
 * 测试依据：《兵棋手游_第一阶段开发方案.md》第四、五节
 */
class MapGeneratorIntegrationTest {

    /**
     * 测试完整地图生成流程
     * 依据：文档 地图生成算法完整流程
     */
    @Test
    fun `test complete map generation flow`() {
        val hexMap = HexMap(20, 20)
        val terrainGenerator = TerrainGenerator()
        val riverGenerator = RiverGenerator()

        // 执行完整生成
        terrainGenerator.generateTerrain(hexMap)
        riverGenerator.generateRivers(hexMap)

        // 验证所有格子都已初始化
        var cellCount = 0
        for (x in 0 until hexMap.width) {
            for (y in 0 until hexMap.height) {
                val cell = hexMap.getCell(x, y)
                assertNotNull("格子($x,$y)不应为空", cell)
                cellCount++
            }
        }
        assertEquals("总格子数应为400", 400, cellCount)
    }

    /**
     * 测试所有地形类型都有生成
     * 依据：文档 地形生成覆盖所有类型
     */
    @Test
    fun `test all terrain types are generated`() {
        val hexMap = HexMap(30, 30)
        val terrainGenerator = TerrainGenerator()
        terrainGenerator.generateTerrain(hexMap)

        var plainCount = 0
        var forestCount = 0
        var hillCount = 0
        var mountainCount = 0
        var urbanCount = 0

        for (x in 0 until hexMap.width) {
            for (y in 0 until hexMap.height) {
                when (hexMap.getCell(x, y).terrain) {
                    TerrainType.PLAIN -> plainCount++
                    TerrainType.FOREST -> forestCount++
                    TerrainType.HILL -> hillCount++
                    TerrainType.MOUNTAIN -> mountainCount++
                    TerrainType.URBAN -> urbanCount++
                }
            }
        }

        assertTrue("应生成平原", plainCount > 0)
        assertTrue("应生成树林", forestCount > 0)
        assertTrue("应生成山地", hillCount > 0)
        assertTrue("应生成高山", mountainCount > 0)
        assertTrue("应生成建筑群", urbanCount > 0)
    }

    /**
     * 测试地图边界正确处理
     * 依据：文档 坐标范围 0~mapWidth-1, 0~mapHeight-1
     */
    @Test
    fun `test map boundaries handled correctly`() {
        val hexMap = HexMap(20, 20)
        val terrainGenerator = TerrainGenerator()
        terrainGenerator.generateTerrain(hexMap)

        // 边界格子应该存在
        assertNotNull("左上角格子应存在", hexMap.getCell(0, 0))
        assertNotNull("右上角格子应存在", hexMap.getCell(19, 0))
        assertNotNull("左下角格子应存在", hexMap.getCell(0, 19))
        assertNotNull("右下角格子应存在", hexMap.getCell(19, 19))

        // 边界外应返回null
        assertNull("X=-1应返回null", hexMap.getCell(-1, 0))
        assertNull("Y=-1应返回null", hexMap.getCell(0, -1))
        assertNull("X=20应返回null", hexMap.getCell(20, 0))
        assertNull("Y=20应返回null", hexMap.getCell(0, 20))
    }

    /**
     * 测试生成后属性计算正确
     * 依据：文档 初始属性计算规则
     */
    @Test
    fun `test attribute calculation after generation`() {
        val hexMap = HexMap(20, 20)
        val terrainGenerator = TerrainGenerator()
        terrainGenerator.generateTerrain(hexMap)

        // 验证地形属性
        val cell = hexMap.getCell(10, 10)
        val expectedMovementCost = when (cell.terrain) {
            TerrainType.PLAIN -> 1
            TerrainType.FOREST -> 1
            TerrainType.HILL -> 2
            TerrainType.MOUNTAIN -> 20
            TerrainType.URBAN -> 1
        }

        assertEquals(
            "移动力消耗应与地形匹配",
            expectedMovementCost,
            cell.movementCost
        )
    }

    /**
     * 测试多次生成结果一致性（相同种子）
     * 依据：文档 随机生成可复现性
     */
    @Test
    fun `test deterministic generation with same seed`() {
        val hexMap1 = HexMap(20, 20)
        val hexMap2 = HexMap(20, 20)
        val terrainGenerator1 = TerrainGenerator()
        val terrainGenerator2 = TerrainGenerator()

        // 使用相同种子
        terrainGenerator1.generateTerrain(hexMap1, seed = 12345)
        terrainGenerator2.generateTerrain(hexMap2, seed = 12345)

        // 结果应完全相同
        for (x in 0 until 20) {
            for (y in 0 until 20) {
                assertEquals(
                    "相同种子生成的地形应相同",
                    hexMap1.getCell(x, y).terrain,
                    hexMap2.getCell(x, y).terrain
                )
            }
        }
    }

    /**
     * 测试不同种子产生不同结果
     * 依据：文档 随机生成
     */
    @Test
    fun `test different seeds produce different results`() {
        val hexMap1 = HexMap(20, 20)
        val hexMap2 = HexMap(20, 20)
        val terrainGenerator1 = TerrainGenerator()
        val terrainGenerator2 = TerrainGenerator()

        terrainGenerator1.generateTerrain(hexMap1, seed = 12345)
        terrainGenerator2.generateTerrain(hexMap2, seed = 67890)

        var differences = 0
        for (x in 0 until 20) {
            for (y in 0 until 20) {
                if (hexMap1.getCell(x, y).terrain != hexMap2.getCell(x, y).terrain) {
                    differences++
                }
            }
        }

        assertTrue("不同种子应产生不同结果，差异数$differences", differences > 0)
    }

    /**
     * 测试边缘格子邻居数量正确
     * 依据：文档 边界处理
     */
    @Test
    fun `test edge cells have correct neighbor count`() {
        val hexMap = HexMap(20, 20)
        val terrainGenerator = TerrainGenerator()
        terrainGenerator.generateTerrain(hexMap)

        // 角落格子应有更少邻居
        val cornerNeighbors = hexMap.getNeighbors(0, 0)
        assertTrue(
            "角落格子邻居数应少于6，实际${cornerNeighbors.size}",
            cornerNeighbors.size < 6
        )

        // 边缘非角落格子
        val edgeNeighbors = hexMap.getNeighbors(10, 0)
        assertTrue(
            "上边缘格子邻居数应少于6，实际${edgeNeighbors.size}",
            edgeNeighbors.size < 6
        )

        // 中心格子应有6个邻居
        val centerNeighbors = hexMap.getNeighbors(10, 10)
        assertEquals(
            "中心格子应有6个邻居",
            6,
            centerNeighbors.size
        )
    }

    /**
     * 测试河流与地形生成不冲突
     * 依据：文档 河流生成与地形生成独立
     */
    @Test
    fun `test river generation does not interfere with terrain`() {
        val hexMap = HexMap(20, 20)
        val terrainGenerator = TerrainGenerator()
        val riverGenerator = RiverGenerator()

        terrainGenerator.generateTerrain(hexMap)

        // 记录生成前的地形
        val terrainBeforeRiver = Array(20) { arrayOfNulls<TerrainType>(20) }
        for (x in 0 until 20) {
            for (y in 0 until 20) {
                terrainBeforeRiver[x][y] = hexMap.getCell(x, y).terrain
            }
        }

        riverGenerator.generateRivers(hexMap)

        // 验证地形未被修改
        for (x in 0 until 20) {
            for (y in 0 until 20) {
                assertEquals(
                    "河流生成不应修改地形",
                    terrainBeforeRiver[x][y],
                    hexMap.getCell(x, y).terrain
                )
            }
        }
    }
}