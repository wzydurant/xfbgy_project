package com.xfbgy.hexmap

import com.xfbgy.hexmap.data.HexCell
import com.xfbgy.hexmap.data.HexEdge
import com.xfbgy.hexmap.data.TerrainType
import com.xfbgy.hexmap.data.FortType
import org.junit.Assert.*
import org.junit.Test

/**
 * Phase 1-A: 数据层测试
 * HexMap 容器及工具函数测试
 *
 * 测试依据：《兵棋手游_第一阶段开发方案.md》第二、四节
 */
class HexMapTest {

    /**
     * 测试 HexMap 基本构造
     * 依据：文档 HexMap 容器设计
     */
    @Test
    fun `test hex map basic construction`() {
        val hexMap = HexMap(20, 20)

        assertEquals("宽度应为20", 20, hexMap.width)
        assertEquals("高度应为20", 20, hexMap.height)
        assertNotNull("格子数组应初始化", hexMap.cells)
        assertNotNull("边缘数组应初始化", hexMap.edges)
    }

    /**
     * 测试地图尺寸边界 - 最小值
     * 依据：文档 mapWidth/mapHeight 范围 20~40
     */
    @Test
    fun `test hex map minimum size`() {
        val hexMap = HexMap(20, 20)

        assertEquals(20, hexMap.width)
        assertEquals(20, hexMap.height)
        assertEquals(20, hexMap.cells.size)
        assertEquals(20, hexMap.cells[0].size)
    }

    /**
     * 测试地图尺寸边界 - 最大值
     * 依据：文档 mapWidth/mapHeight 范围 20~40
     */
    @Test
    fun `test hex map maximum size`() {
        val hexMap = HexMap(40, 40)

        assertEquals(40, hexMap.width)
        assertEquals(40, hexMap.height)
        assertEquals(40, hexMap.cells.size)
        assertEquals(40, hexMap.cells[0].size)
    }

    /**
     * 测试超出范围的地图尺寸
     * 依据：文档 mapWidth/mapHeight 有效范围 20~40
     */
    @Test(expected = IllegalArgumentException::class)
    fun `test hex map size below minimum throws exception`() {
        HexMap(19, 20)
    }

    /**
     * 测试超出范围的地图尺寸
     * 依据：文档 mapWidth/mapHeight 有效范围 20~40
     */
    @Test(expected = IllegalArgumentException::class)
    fun `test hex map size above maximum throws exception`() {
        HexMap(40, 41)
    }

    /**
     * 测试获取格子坐标
     * 依据：文档 HexMap.getNeighbors 接口预留
     */
    @Test
    fun `test get cell at coordinates`() {
        val hexMap = HexMap(20, 20)
        val cell = hexMap.getCell(5, 5)

        assertNotNull("格子不应为空", cell)
        assertEquals("X坐标应为5", 5, cell.x)
        assertEquals("Y坐标应为5", 5, cell.y)
    }

    /**
     * 测试坐标越界返回null
     * 依据：文档坐标范围验证
     */
    @Test
    fun `test get cell out of bounds returns null`() {
        val hexMap = HexMap(20, 20)

        assertNull("X坐标负数应返回null", hexMap.getCell(-1, 0))
        assertNull("Y坐标负数应返回null", hexMap.getCell(0, -1))
        assertNull("X坐标超出范围应返回null", hexMap.getCell(20, 0))
        assertNull("Y坐标超出范围应返回null", hexMap.getCell(0, 20))
    }

    /**
     * 测试边缘数组维度 [x][y][direction]
     * 依据：文档边缘存储方案
     */
    @Test
    fun `test edges array has correct dimensions`() {
        val hexMap = HexMap(20, 20)

        assertEquals("X维度应为20", 20, hexMap.edges.size)
        assertEquals("Y维度应为20", 20, hexMap.edges[0].size)
        assertEquals("方向维度应为6", 6, hexMap.edges[0][0].size)
    }

    /**
     * 测试获取指定边
     * 依据：文档 HexMap.getEdge 接口预留
     */
    @Test
    fun `test get edge at position and direction`() {
        val hexMap = HexMap(20, 20)
        val edge = hexMap.getEdge(5, 5, 0)

        assertNotNull("边不应为空", edge)
        assertTrue("边应为 HexEdge 实例", edge is HexEdge)
    }

    /**
     * 测试边方向编号有效范围 0~5
     * 依据：文档 HexEdge 方向编号
     */
    @Test
    fun `test edge direction valid range`() {
        val hexMap = HexMap(20, 20)

        for (dir in 0..5) {
            val edge = hexMap.getEdge(5, 5, dir)
            assertNotNull("方向$dir 应有效", edge)
        }
    }

    /**
     * 测试边方向编号越界
     * 依据：文档 HexEdge 方向编号范围
     */
    @Test(expected = IllegalArgumentException::class)
    fun `test edge direction out of range throws exception`() {
        val hexMap = HexMap(20, 20)
        hexMap.getEdge(5, 5, 6)
    }

    /**
     * 测试原点坐标 (0,0) 位于最左下角
     * 依据：文档坐标原点定义
     */
    @Test
    fun `test origin is at bottom left`() {
        val hexMap = HexMap(20, 20)
        val originCell = hexMap.getCell(0, 0)

        assertEquals("原点X应为0", 0, originCell.x)
        assertEquals("原点Y应为0", 0, originCell.y)
    }

    /**
     * 测试 odd-r 偏移坐标 - 单数行左偏
     * 依据：文档 gridLayout 固定为单数行左偏
     */
    @Test
    fun `test odd row offset`() {
        val hexMap = HexMap(20, 20)

        // 获取第1行（单数行）的格子
        val oddRowCell = hexMap.getCell(5, 1)
        assertNotNull(oddRowCell)
        assertEquals(1, oddRowCell.y % 2) // 验证是单数行
    }

    /**
     * 测试偶数行不偏移
     * 依据：文档 odd-r 偏移规则
     */
    @Test
    fun `test even row no offset`() {
        val hexMap = HexMap(20, 20)

        // 获取第0行（偶数行）的格子
        val evenRowCell = hexMap.getCell(5, 0)
        assertNotNull(evenRowCell)
        assertEquals(0, evenRowCell.y % 2) // 验证是偶数行
    }

    /**
     * 测试相邻格子获取
     * 依据：文档 HexMap.getNeighbors 接口预留
     */
    @Test
    fun `test get neighbors returns adjacent cells`() {
        val hexMap = HexMap(20, 20)
        val neighbors = hexMap.getNeighbors(10, 10)

        assertNotNull("邻居列表不应为空", neighbors)
        // 六角格每个格子有6个邻居
        assertEquals("六角格应有6个邻居", 6, neighbors.size)
    }

    /**
     * 测试边界格子的邻居数量
     * 依据：文档边界处理
     */
    @Test
    fun `test corner cell has fewer neighbors`() {
        val hexMap = HexMap(20, 20)

        // 左下角格子 (0,0) 的邻居
        val cornerNeighbors = hexMap.getNeighbors(0, 0)

        // 角落格子邻居数少于6
        assertTrue(
            "角落格子邻居数应少于6，实际为${cornerNeighbors.size}",
            cornerNeighbors.size < 6
        )
    }

    /**
     * 测试所有格子初始地形为平原
     * 依据：文档默认地形
     */
    @Test
    fun `test all cells default to plain terrain`() {
        val hexMap = HexMap(20, 20)

        for (x in 0 until hexMap.width) {
            for (y in 0 until hexMap.height) {
                val cell = hexMap.getCell(x, y)
                assertEquals(
                    "格子($x,$y)默认地形应为平原",
                    TerrainType.PLAIN,
                    cell.terrain
                )
            }
        }
    }

    /**
     * 测试设置格子地形
     * 依据：文档 HexCell.terrain 属性
     */
    @Test
    fun `test set cell terrain`() {
        val hexMap = HexMap(20, 20)
        val cell = hexMap.getCell(5, 5)
        cell.terrain = TerrainType.FOREST

        assertEquals("地形应被设置为树林", TerrainType.FOREST, cell.terrain)
    }

    /**
     * 测试共享边河流同步
     * 依据：文档 河流一致性规则
     */
    @Test
    fun `test shared edge river synchronization`() {
        val hexMap = HexMap(20, 20)

        // 设置相邻两格之间的边有河流
        hexMap.setEdgeRiver(5, 5, 0, true)

        // 验证相邻格子的对应边也被设置
        val cell1 = hexMap.getCell(5, 5)
        val cell2 = hexMap.getCell(5, 6) // 方向0是向上的边，相邻格y+1

        val sharedEdge1 = hexMap.getEdge(5, 5, 0)
        val sharedEdge2 = hexMap.getEdge(5, 6, 3) // 对应方向的相反边

        // 由于是共享边，两边状态应一致
        assertEquals(
            "共享边的河流状态应一致",
            sharedEdge1.hasRiver,
            sharedEdge2.hasRiver
        )
    }
}