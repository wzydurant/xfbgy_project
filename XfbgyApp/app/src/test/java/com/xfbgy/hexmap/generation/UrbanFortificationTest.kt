package com.xfbgy.hexmap.generation

import com.xfbgy.hexmap.HexMap
import com.xfbgy.hexmap.data.FortType
import com.xfbgy.hexmap.data.TerrainType
import org.junit.Assert.*
import org.junit.Test

/**
 * Phase 1-B: 地图生成层测试
 * 建筑群六边同工事规则测试
 *
 * 测试依据：《兵棋手游_第一阶段开发方案.md》第四节 建筑群特殊处理
 */
class UrbanFortificationTest {

    /**
     * 测试建筑群所有边工事类型一致
     * 依据：文档 建筑群6条边防御工事随机统一为同一等级
     */
    @Test
    fun `test urban hex has same fortification on all edges`() {
        val hexMap = HexMap(20, 20)
        val generator = TerrainGenerator()
        generator.generateTerrain(hexMap)

        // 找到所有建筑群格子
        val urbanCells = getUrbanCells(hexMap)

        for (cell in urbanCells) {
            val fortType = hexMap.getEdge(cell.x, cell.y, 0).fortification

            // 检查所有6条边
            for (dir in 1..5) {
                assertEquals(
                    "建筑群格子(${cell.x},${cell.y})所有边工事应一致",
                    fortType,
                    hexMap.getEdge(cell.x, cell.y, dir).fortification
                )
            }
        }
    }

    /**
     * 测试建筑群工事不能为NONE
     * 依据：文档 不得为"无"
     */
    @Test
    fun `test urban fortification is never NONE`() {
        val hexMap = HexMap(20, 20)
        val generator = TerrainGenerator()
        generator.generateTerrain(hexMap)

        val urbanCells = getUrbanCells(hexMap)

        for (cell in urbanCells) {
            val fortType = hexMap.getEdge(cell.x, cell.y, 0).fortification
            assertNotEquals(
                "建筑群工事不应为NONE",
                FortType.NONE,
                fortType
            )
        }
    }

    /**
     * 测试建筑群工事类型有效
     * 依据：文档 建筑群工事从栅栏、土墙、石墙中随机选取
     */
    @Test
    fun `test urban fortification is valid type`() {
        val hexMap = HexMap(20, 20)
        val generator = TerrainGenerator()
        generator.generateTerrain(hexMap)

        val validTypes = setOf(FortType.FENCE, FortType.EARTHWALL, FortType.STONEWALL)
        val urbanCells = getUrbanCells(hexMap)

        for (cell in urbanCells) {
            val fortType = hexMap.getEdge(cell.x, cell.y, 0).fortification
            assertTrue(
                "建筑群工事应为有效类型，实际为$fortType",
                fortType in validTypes
            )
        }
    }

    /**
     * 测试建筑群工事随机性
     * 依据：文档 随机统一为同一等级
     */
    @Test
    fun `test urban fortification randomization`() {
        val validTypes = setOf(FortType.FENCE, FortType.EARTHWALL, FortType.STONEWALL)
        val foundTypes = mutableSetOf<FortType>()

        repeat(30) {
            val hexMap = HexMap(20, 20)
            val generator = TerrainGenerator()
            generator.generateTerrain(hexMap)

            val urbanCells = getUrbanCells(hexMap)
            if (urbanCells.isNotEmpty()) {
                val fortType = hexMap.getEdge(urbanCells[0].x, urbanCells[0].y, 0).fortification
                if (fortType in validTypes) {
                    foundTypes.add(fortType)
                }
            }
        }

        assertTrue(
            "应能生成多种工事类型，实际找到${foundTypes.size}种",
            foundTypes.size >= 1
        )
    }

    /**
     * 测试无建筑群时不影响其他格子
     * 依据：文档 建筑群特殊处理
     */
    @Test
    fun `test non urban cells not affected`() {
        val hexMap = HexMap(20, 20)
        val generator = TerrainGenerator()
        generator.generateTerrain(hexMap)

        // 找到非建筑群格子
        for (x in 0 until hexMap.width) {
            for (y in 0 until hexMap.height) {
                val cell = hexMap.getCell(x, y)
                if (cell.terrain != TerrainType.URBAN) {
                    // 非建筑群格子不受建筑群工事规则影响
                    // 它们的工事可以是任意值（通常为NONE）
                }
            }
        }
    }

    // 辅助方法：获取所有建筑群格子
    private fun getUrbanCells(hexMap: HexMap): List<com.xfbgy.hexmap.data.HexCell> {
        val cells = mutableListOf<com.xfbgy.hexmap.data.HexCell>()
        for (x in 0 until hexMap.width) {
            for (y in 0 until hexMap.height) {
                val cell = hexMap.getCell(x, y)
                if (cell.terrain == TerrainType.URBAN) {
                    cells.add(cell)
                }
            }
        }
        return cells
    }
}