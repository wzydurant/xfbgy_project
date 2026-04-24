package com.xfbgy.hexmap.data

import org.junit.Assert.*
import org.junit.Test
import java.awt.Point

/**
 * Phase 1-A: 数据层测试
 * HexCell 六角格数据类测试
 *
 * 测试依据：《兵棋手游_第一阶段开发方案.md》第二节 HexCell定义
 */
class HexCellTest {

    /**
     * 测试 HexCell 基本构造
     * 依据：文档 HexCell 数据类字段定义
     */
    @Test
    fun `test hex cell basic construction`() {
        val cell = HexCell(5, 10, TerrainType.PLAIN)

        assertEquals("X坐标应为5", 5, cell.x)
        assertEquals("Y坐标应为10", 10, cell.y)
        assertEquals("地形应为平原", TerrainType.PLAIN, cell.terrain)
        assertNotNull("单位列表应初始化", cell.units)
        assertTrue("单位列表应为空", cell.units.isEmpty())
        assertEquals("移动力消耗初始为0", 0, cell.movementCost)
        assertEquals("ZOC初始为0", 0b000, cell.zoc)
    }

    /**
     * 测试 HexCell 构造时移动力消耗默认值
     * 依据：文档 HexCell movementCost 默认值为0
     */
    @Test
    fun `test hex cell movement cost defaults to zero`() {
        val cell = HexCell(0, 0, TerrainType.HILL)
        assertEquals("移动力消耗默认应为0", 0, cell.movementCost)
    }

    /**
     * 测试 HexCell 构造时ZOC默认值
     * 依据：文档 HexCell zoc 默认值为 0b000
     */
    @Test
    fun `test hex cell zoc defaults to zero`() {
        val cell = HexCell(0, 0, TerrainType.PLAIN)
        assertEquals("ZOC默认应为0b000", 0b000, cell.zoc)
    }

    /**
     * 测试所有地形类型都能创建HexCell
     * 依据：文档 TerrainType 枚举
     */
    @Test
    fun `test hex cell creation with all terrain types`() {
        for (terrain in TerrainType.entries) {
            val cell = HexCell(0, 0, terrain)
            assertEquals("地形应匹配", terrain, cell.terrain)
        }
    }

    /**
     * 测试 HexCell 坐标边界值
     * 依据：文档 mapWidth/mapHeight 范围 20~40
     */
    @Test
    fun `test hex cell coordinates within map range`() {
        // 最小边界
        val minCell = HexCell(0, 0, TerrainType.PLAIN)
        assertEquals(0, minCell.x)
        assertEquals(0, minCell.y)

        // 最大边界 (假设地图40x40)
        val maxCell = HexCell(39, 39, TerrainType.PLAIN)
        assertEquals(39, maxCell.x)
        assertEquals(39, maxCell.y)
    }

    /**
     * 测试单元列表可以添加单位
     * 依据：文档 HexCell.units 预留接口
     */
    @Test
    fun `test units list can be modified`() {
        val cell = HexCell(0, 0, TerrainType.PLAIN)
        assertTrue("单位列表应可添加", cell.units.add("mockUnit"))
        assertEquals("单位列表应有一个元素", 1, cell.units.size)
    }

    /**
     * 测试 ZOC 位掩码设置和获取
     * 依据：文档 ZOC 接口设计
     */
    @Test
    fun `test zoc bitmask operations`() {
        val cell = HexCell(0, 0, TerrainType.PLAIN)

        // 设置阵营1控制
        cell.zoc = cell.zoc or (1 shl (1 - 1))
        assertTrue("阵营1应被控制", (cell.zoc and (1 shl 0)) != 0)

        // 设置阵营2控制
        cell.zoc = cell.zoc or (1 shl (2 - 1))
        assertTrue("阵营2应被控制", (cell.zoc and (1 shl 1)) != 0)

        // 验证阵营3未被控制
        assertFalse("阵营3应未被控制", (cell.zoc and (1 shl 2)) != 0)
    }

    /**
     * 测试 ZOC 多阵营同时控制
     * 依据：文档 ZOC 位掩码设计说明
     */
    @Test
    fun `test zoc multi faction control`() {
        val cell = HexCell(0, 0, TerrainType.PLAIN)

        // 同时设置三个阵营
        cell.zoc = 0b111

        assertTrue("阵营1应被控制", (cell.zoc and (1 shl 0)) != 0)
        assertTrue("阵营2应被控制", (cell.zoc and (1 shl 1)) != 0)
        assertTrue("阵营3应被控制", (cell.zoc and (1 shl 2)) != 0)
    }

    /**
     * 测试 ZOC 清除单一阵营
     * 依据：文档 ZOC 接口 - clearZOC
     */
    @Test
    fun `test zoc clear single faction`() {
        val cell = HexCell(0, 0, TerrainType.PLAIN)
        cell.zoc = 0b111

        // 清除阵营2
        cell.zoc = cell.zoc and (1 shl (2 - 1)).inv()

        assertTrue("阵营1应仍被控制", (cell.zoc and (1 shl 0)) != 0)
        assertFalse("阵营2应已清除", (cell.zoc and (1 shl 1)) != 0)
        assertTrue("阵营3应仍被控制", (cell.zoc and (1 shl 2)) != 0)
    }

    /**
     * 测试 ZOC 8位掩码支持
     * 依据：文档 ZOC 多阵营支持 - 预留至少8位
     */
    @Test
    fun `test zoc supports up to 8 factions`() {
        val cell = HexCell(0, 0, TerrainType.PLAIN)

        // 测试所有8个阵营都可以设置
        for (faction in 1..8) {
            cell.zoc = cell.zoc or (1 shl (faction - 1))
            assertTrue(
                "阵营$faction 应能被设置",
                (cell.zoc and (1 shl (faction - 1))) != 0
            )
        }
    }
}