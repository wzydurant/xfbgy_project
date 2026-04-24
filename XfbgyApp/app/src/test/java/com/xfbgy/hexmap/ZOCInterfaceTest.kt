package com.xfbgy.hexmap

import com.xfbgy.hexmap.data.HexCell
import org.junit.Assert.*
import org.junit.Test

/**
 * Phase 1-A: 数据层测试
 * ZOC 位掩码读写接口测试
 *
 * 测试依据：《兵棋手游_第一阶段开发方案.md》第五节 ZOC接口设计
 */
class ZOCInterfaceTest {

    /**
     * 测试 isControlledBy 阵营1
     * 依据：文档 isControlledBy 方法
     */
    @Test
    fun `test isControlledBy faction 1`() {
        val cell = HexCell(0, 0, com.xfbgy.hexmap.data.TerrainType.PLAIN)
        cell.zoc = 0b001  // 阵营1

        assertTrue("阵营1应被控制", cell.isControlledBy(1))
        assertFalse("阵营2应未控制", cell.isControlledBy(2))
        assertFalse("阵营3应未控制", cell.isControlledBy(3))
    }

    /**
     * 测试 isControlledBy 阵营2
     * 依据：文档 isControlledBy 方法
     */
    @Test
    fun `test isControlledBy faction 2`() {
        val cell = HexCell(0, 0, com.xfbgy.hexmap.data.TerrainType.PLAIN)
        cell.zoc = 0b010  // 阵营2

        assertFalse("阵营1应未控制", cell.isControlledBy(1))
        assertTrue("阵营2应被控制", cell.isControlledBy(2))
        assertFalse("阵营3应未控制", cell.isControlledBy(3))
    }

    /**
     * 测试 isControlledBy 阵营3
     * 依据：文档 isControlledBy 方法
     */
    @Test
    fun `test isControlledBy faction 3`() {
        val cell = HexCell(0, 0, com.xfbgy.hexmap.data.TerrainType.PLAIN)
        cell.zoc = 0b100  // 阵营3

        assertFalse("阵营1应未控制", cell.isControlledBy(1))
        assertFalse("阵营2应未控制", cell.isControlledBy(2))
        assertTrue("阵营3应被控制", cell.isControlledBy(3))
    }

    /**
     * 测试 isControlledBy 多阵营
     * 依据：文档 ZOC 位掩码支持多阵营
     */
    @Test
    fun `test isControlledBy multiple factions`() {
        val cell = HexCell(0, 0, com.xfbgy.hexmap.data.TerrainType.PLAIN)
        cell.zoc = 0b101  // 阵营1 + 阵营3

        assertTrue("阵营1应被控制", cell.isControlledBy(1))
        assertFalse("阵营2应未控制", cell.isControlledBy(2))
        assertTrue("阵营3应被控制", cell.isControlledBy(3))
    }

    /**
     * 测试 isControlledBy 无控制
     * 依据：文档 ZOC 初始状态
     */
    @Test
    fun `test isControlledBy no control`() {
        val cell = HexCell(0, 0, com.xfbgy.hexmap.data.TerrainType.PLAIN)
        cell.zoc = 0b000

        for (faction in 1..8) {
            assertFalse("无控制时任何阵营都不应被控制", cell.isControlledBy(faction))
        }
    }

    /**
     * 测试 setZOC 设置单一阵营
     * 依据：文档 setZOC 方法
     */
    @Test
    fun `test setZOC single faction`() {
        val cell = HexCell(0, 0, com.xfbgy.hexmap.data.TerrainType.PLAIN)
        cell.setZOC(1)

        assertTrue("阵营1应被控制", cell.isControlledBy(1))
        assertFalse("阵营2应未控制", cell.isControlledBy(2))
        assertFalse("阵营3应未控制", cell.isControlledBy(3))
    }

    /**
     * 测试 setZOC 叠加控制
     * 依据：文档 ZOC 叠加规则
     */
    @Test
    fun `test setZOC accumulation`() {
        val cell = HexCell(0, 0, com.xfbgy.hexmap.data.TerrainType.PLAIN)

        cell.setZOC(1)
        cell.setZOC(2)

        assertTrue("阵营1应被控制", cell.isControlledBy(1))
        assertTrue("阵营2应被控制", cell.isControlledBy(2))
        assertFalse("阵营3应未控制", cell.isControlledBy(3))
    }

    /**
     * 测试 setZOC 重复设置不影响
     * 依据：文档 ZOC 位掩码特性
     */
    @Test
    fun `test setZOC idempotent`() {
        val cell = HexCell(0, 0, com.xfbgy.hexmap.data.TerrainType.PLAIN)

        cell.setZOC(1)
        cell.setZOC(1)  // 重复设置

        assertTrue("阵营1应仍被控制", cell.isControlledBy(1))
        assertEquals("ZOC值应不变", 0b001, cell.zoc)
    }

    /**
     * 测试 clearZOC 清除单一阵营
     * 依据：文档 clearZOC 方法
     */
    @Test
    fun `test clearZOC single faction`() {
        val cell = HexCell(0, 0, com.xfbgy.hexmap.data.TerrainType.PLAIN)
        cell.zoc = 0b111

        cell.clearZOC(2)

        assertTrue("阵营1应仍被控制", cell.isControlledBy(1))
        assertFalse("阵营2应被清除", cell.isControlledBy(2))
        assertTrue("阵营3应仍被控制", cell.isControlledBy(3))
    }

    /**
     * 测试 clearZOC 清除已清除的阵营无影响
     * 依据：文档 clearZOC 幂等性
     */
    @Test
    fun `test clearZOC already cleared no effect`() {
        val cell = HexCell(0, 0, com.xfbgy.hexmap.data.TerrainType.PLAIN)
        cell.zoc = 0b001  // 只有阵营1

        cell.clearZOC(2)  // 清除阵营2（未设置）

        assertTrue("阵营1应仍被控制", cell.isControlledBy(1))
        assertEquals("ZOC值应不变", 0b001, cell.zoc)
    }

    /**
     * 测试 clearZOC 全清除
     * 依据：文档 clearZOC 完整清除
     */
    @Test
    fun `test clearZOC all factions`() {
        val cell = HexCell(0, 0, com.xfbgy.hexmap.data.TerrainType.PLAIN)
        cell.zoc = 0b111

        cell.clearZOC(1)
        cell.clearZOC(2)
        cell.clearZOC(3)

        assertFalse("所有阵营应已清除", cell.isControlledBy(1))
        assertFalse("所有阵营应已清除", cell.isControlledBy(2))
        assertFalse("所有阵营应已清除", cell.isControlledBy(3))
        assertEquals("ZOC应归零", 0b000, cell.zoc)
    }

    /**
     * 测试 8阵营位掩码支持
     * 依据：文档 ZOC 预留至少8位
     */
    @Test
    fun `test ZOC supports 8 factions`() {
        val cell = HexCell(0, 0, com.xfbgy.hexmap.data.TerrainType.PLAIN)

        // 测试所有8个阵营
        for (faction in 1..8) {
            cell.setZOC(faction)
            assertTrue("阵营$faction 应被控制", cell.isControlledBy(faction))
        }

        // 验证ZOC值为0xFF
        assertEquals("8个阵营全开应为0xFF", 0xFF, cell.zoc)
    }

    /**
     * 测试阵营编号有效性（1-8）
     * 依据：文档 8阵营支持
     */
    @Test(expected = IllegalArgumentException::class)
    fun `test invalid faction number throws exception`() {
        val cell = HexCell(0, 0, com.xfbgy.hexmap.data.TerrainType.PLAIN)
        cell.setZOC(0)  // 无效阵营编号
    }

    /**
     * 测试超出8的阵营编号
     * 依据：文档 8阵营上限
     */
    @Test(expected = IllegalArgumentException::class)
    fun `test faction number above 8 throws exception`() {
        val cell = HexCell(0, 0, com.xfbgy.hexmap.data.TerrainType.PLAIN)
        cell.setZOC(9)  // 超出8个阵营
    }
}