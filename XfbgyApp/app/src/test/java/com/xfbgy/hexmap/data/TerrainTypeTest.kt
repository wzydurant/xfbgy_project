package com.xfbgy.hexmap.data

import org.junit.Assert.*
import org.junit.Test

/**
 * Phase 1-A: 数据层测试
 * TerrainType 枚举测试
 *
 * 测试依据：《兵棋手游_第一阶段开发方案.md》第二节 地形枚举定义
 */
class TerrainTypeTest {

    /**
     * 测试地形枚举数量
     * 依据：文档规定5种地形 PLAIN, FOREST, HILL, MOUNTAIN, URBAN
     */
    @Test
    fun `test terrain type count is five`() {
        assertEquals("地形类型数量应为5种", 5, TerrainType.entries.size)
    }

    /**
     * 测试平原属性
     * 依据：文档 Table 地形枚举 - PLAIN
     */
    @Test
    fun `test plain terrain properties`() {
        val plain = TerrainType.PLAIN
        assertEquals("平原", plain.chineseName)
        assertEquals("#A8D5A2", plain.colorHex)
        assertEquals(1, plain.movementCost)
    }

    /**
     * 测试树林属性
     * 依据：文档 Table 地形枚举 - FOREST
     */
    @Test
    fun `test forest terrain properties`() {
        val forest = TerrainType.FOREST
        assertEquals("树林", forest.chineseName)
        assertEquals("#2D6A4F", forest.colorHex)
        assertEquals(1, forest.movementCost)
    }

    /**
     * 测试山地属性
     * 依据：文档 Table 地形枚举 - HILL
     */
    @Test
    fun `test hill terrain properties`() {
        val hill = TerrainType.HILL
        assertEquals("山地", hill.chineseName)
        assertEquals("#8B5E3C", hill.colorHex)
        assertEquals(2, hill.movementCost)
    }

    /**
     * 测试高山属性
     * 依据：文档 Table 地形枚举 - MOUNTAIN
     */
    @Test
    fun `test mountain terrain properties`() {
        val mountain = TerrainType.MOUNTAIN
        assertEquals("高山", mountain.chineseName)
        assertEquals("#222222", mountain.colorHex)
        assertEquals(20, mountain.movementCost)
    }

    /**
     * 测试建筑群属性
     * 依据：文档 Table 地形枚举 - URBAN
     */
    @Test
    fun `test urban terrain properties`() {
        val urban = TerrainType.URBAN
        assertEquals("建筑群", urban.chineseName)
        assertEquals("#9E9E9E", urban.colorHex)
        assertEquals(1, urban.movementCost)
    }

    /**
     * 测试 fromOrdinal 有效范围
     * 依据：文档 TerrainType.fromOrdinal 方法
     */
    @Test
    fun `test fromOrdinal valid range`() {
        assertEquals(TerrainType.PLAIN, TerrainType.fromOrdinal(0))
        assertEquals(TerrainType.FOREST, TerrainType.fromOrdinal(1))
        assertEquals(TerrainType.HILL, TerrainType.fromOrdinal(2))
        assertEquals(TerrainType.MOUNTAIN, TerrainType.fromOrdinal(3))
        assertEquals(TerrainType.URBAN, TerrainType.fromOrdinal(4))
    }

    /**
     * 测试 fromOrdinal 超出范围时返回平原默认值
     * 依据：文档 fromOrdinal 异常处理
     */
    @Test
    fun `test fromOrdinal out of range returns plain`() {
        assertEquals(TerrainType.PLAIN, TerrainType.fromOrdinal(-1))
        assertEquals(TerrainType.PLAIN, TerrainType.fromOrdinal(5))
        assertEquals(TerrainType.PLAIN, TerrainType.fromOrdinal(100))
    }

    /**
     * 测试所有地形颜色Hex格式正确（6位）
     * 依据：文档颜色规范 - 地形颜色
     */
    @Test
    fun `test all terrain colors are valid_hex_format`() {
        for (terrain in TerrainType.entries) {
            assertTrue(
                "${terrain.name} 颜色应为6位Hex",
                terrain.colorHex.matches(Regex("^#[0-9A-Fa-f]{6}$"))
            )
        }
    }

    /**
     * 测试移动力消耗值非负
     * 依据：文档移动力消耗范围
     */
    @Test
    fun `test all movement costs are non_negative`() {
        for (terrain in TerrainType.entries) {
            assertTrue(
                "${terrain.name} 移动力消耗应非负",
                terrain.movementCost >= 0
            )
        }
    }

    /**
     * 测试高山移动力消耗显著高于其他地形
     * 依据：文档高山移动力消耗 +20
     */
    @Test
    fun `test mountain has significantly higher movement cost`() {
        for (terrain in TerrainType.entries) {
            if (terrain != TerrainType.MOUNTAIN) {
                assertTrue(
                    "高山移动力消耗应显著高于其他地形",
                    TerrainType.MOUNTAIN.movementCost > terrain.movementCost
                )
            }
        }
    }
}