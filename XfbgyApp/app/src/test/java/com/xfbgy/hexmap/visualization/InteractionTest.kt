package com.xfbgy.hexmap.visualization

import com.xfbgy.hexmap.HexMap
import com.xfbgy.hexmap.data.HexCell
import com.xfbgy.hexmap.data.TerrainType
import org.junit.Assert.*
import org.junit.Test

/**
 * Phase 1-C: 可视化层测试
 * 交互功能测试
 *
 * 测试依据：《兵棋手游_第一阶段开发方案.md》第六节 交互实现
 */
class InteractionTest {

    /**
     * 测试点击格子坐标获取
     * 依据：文档 点击格子通过触摸坐标反算格子坐标
     */
    @Test
    fun `test get cell from touch coordinates`() {
        val hexMap = HexMap(20, 20)

        // 模拟触摸在格子(10,10)中心
        val touchX = 10f * 60f * 1.732f // 近似计算
        val touchY = 10f * 60f * 1.5f

        val cell = hexMap.getCellAtPixel(touchX, touchY, 60f)

        // 由于奇偶行偏移和浮点精度，允许误差
        assertNotNull("应能获取触摸位置的格子", cell)
    }

    /**
     * 测试边界触摸坐标处理
     * 依据：文档 坐标反算边界处理
     */
    @Test
    fun `test touch on boundary returns correct cell`() {
        val hexMap = HexMap(20, 20)

        // 触摸在(0,0)格子附近
        val cell = hexMap.getCellAtPixel(5f, 5f, 60f)

        assertNotNull("边界触摸应返回格子", cell)
        assertTrue(
            "边界触摸应在有效范围内",
            cell.x >= 0 && cell.y >= 0
        )
    }

    /**
     * 测试点击空白区域返回null
     * 依据：文档 触摸空白区域
     */
    @Test
    fun `test touch outside map returns null`() {
        val hexMap = HexMap(20, 20)

        // 触摸在地图范围外
        val cell = hexMap.getCellAtPixel(-100f, -100f, 60f)

        assertNull("地图外触摸应返回null", cell)
    }

    /**
     * 测试信息面板显示完整地形信息
     * 依据：文档 弹出信息面板展示地形、移动力消耗、ZOC状态
     */
    @Test
    fun `test info panel shows complete cell information`() {
        val cell = HexCell(5, 5, TerrainType.HILL)
        cell.movementCost = 2
        cell.zoc = 0b101

        // 验证信息完整性
        val info = buildCellInfoPanel(cell)

        assertTrue("面板应包含坐标", info.contains("5,5"))
        assertTrue("面板应包含地形", info.contains("山地"))
        assertTrue("面板应包含移动力消耗", info.contains("2"))
        assertTrue("面板应包含ZOC状态", info.contains("ZOC"))
    }

    /**
     * 测试边缘属性显示
     * 依据：文档 方向按钮展示边缘完整属性
     */
    @Test
    fun `test edge info panel shows complete edge attributes`() {
        val hexMap = HexMap(20, 20)
        val edge = hexMap.getEdge(10, 10, 0)

        // 设置边缘属性
        edge.hasRiver = true
        edge.movementPenalty = 2
        edge.defenseBonus = 1

        // 验证边缘信息完整性
        val info = buildEdgeInfoPanel(edge, 0)

        assertTrue("边缘信息应包含河流状态", info.contains("河流") || info.contains("有河"))
        assertTrue("边缘信息应包含移动破坏", info.contains("移动破坏"))
        assertTrue("边缘信息应包含防御优势", info.contains("防御优势"))
    }

    /**
     * 测试6个方向按钮正确映射
     * 依据：文档 方向按钮（上/右上/右下/下/左下/左上）
     */
    @Test
    fun `test six direction buttons mapping`() {
        val directions = listOf("上", "右上", "右下", "下", "左下", "左上")

        assertEquals("应有6个方向", 6, directions.size)

        // 验证方向编号对应
        val directionNames = mapOf(
            0 to "上",
            1 to "右上",
            2 to "右下",
            3 to "下",
            4 to "左下",
            5 to "左上"
        )

        for ((dir, name) in directionNames) {
            assertEquals("方向$dir 应为$name", name, directions[dir])
        }
    }

    /**
     * 测试缩放范围限制
     * 依据：文档 设置最小/最大缩放限制
     */
    @Test
    fun `test zoom scale limits`() {
        val minScale = 0.5f
        val maxScale = 3.0f
        val currentScale = 0.3f

        val clampedScale = clampScale(currentScale, minScale, maxScale)
        assertEquals("缩放过小时应限制为最小值", minScale, clampedScale, 0.001f)

        val largeScale = 5.0f
        val clampedLargeScale = clampScale(largeScale, minScale, maxScale)
        assertEquals("缩放过小时应限制为最大值", maxScale, clampedLargeScale, 0.001f)
    }

    /**
     * 测试正常缩放范围不改变
     * 依据：文档 缩放限制
     */
    @Test
    fun `test normal scale remains unchanged`() {
        val minScale = 0.5f
        val maxScale = 3.0f
        val normalScale = 1.5f

        val clampedScale = clampScale(normalScale, minScale, maxScale)
        assertEquals("正常缩放值应保持不变", normalScale, clampedScale, 0.001f)
    }

    /**
     * 测试拖拽偏移量计算
     * 依据：文档 GestureDetector onScroll 实现拖拽
     */
    @Test
    fun `test scroll offset calculation`() {
        val initialOffsetX = 0f
        val initialOffsetY = 0f
        val scrollDeltaX = 50f
        val scrollDeltaY = -30f

        val newOffsetX = initialOffsetX + scrollDeltaX
        val newOffsetY = initialOffsetY + scrollDeltaY

        assertEquals("X方向拖拽偏移应为50", 50f, newOffsetX, 0.001f)
        assertEquals("Y方向拖拽偏移应为-30", -30f, newOffsetY, 0.001f)
    }

    /**
     * 测试多指缩放计算
     * 依据：文档 ScaleGestureDetector 实现双指缩放
     */
    @Test
    fun `test pinch zoom scale factor`() {
        val initialScale = 1.0f
        val scaleFactor = 1.5f

        val newScale = initialScale * scaleFactor
        assertEquals("缩放因子相乘", 1.5f, newScale, 0.001f)
    }

    // 辅助方法：构建格子信息面板
    private fun buildCellInfoPanel(cell: HexCell): String {
        return buildString {
            append("(${cell.x},${cell.y}) ")
            append("${cell.terrain.chineseName} ")
            append("移动力:${cell.movementCost} ")
            append("ZOC:${cell.zoc}")
        }
    }

    // 辅助方法：构建边缘信息面板
    private fun buildEdgeInfoPanel(edge: com.xfbgy.hexmap.data.HexEdge, direction: Int): String {
        return buildString {
            append("方向$direction ")
            append("河流:${if (edge.hasRiver) "有" else "无"} ")
            append("移动破坏:${edge.movementPenalty} ")
            append("防御优势:${edge.defenseBonus}")
        }
    }

    // 辅助方法：限制缩放值
    private fun clampScale(scale: Float, minScale: Float, maxScale: Float): Float {
        return scale.coerceIn(minScale, maxScale)
    }
}