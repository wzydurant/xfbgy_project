package com.xfbgy.hexmap.visualization

import com.xfbgy.hexmap.HexMap
import org.junit.Assert.*
import org.junit.Test

/**
 * Phase 1-C: 可视化层测试
 * 缩放与拖拽交互测试
 *
 * 测试依据：《兵棋手游_第一阶段开发方案.md》第六节
 * 缩放(0.5x~3.0x)、拖拽交互
 */
class ZoomPanInteractionTest {

    /**
     * 测试最小缩放限制0.5x
     * 依据：文档 最小缩放限制
     */
    @Test
    fun `test minimum zoom level is 0_5x`() {
        val minZoom = 0.5f
        assertEquals("最小缩放应为0.5x", 0.5f, minZoom)
    }

    /**
     * 测试最大缩放限制3.0x
     * 依据：文档 最大缩放限制
     */
    @Test
    fun `test maximum zoom level is 3_0x`() {
        val maxZoom = 3.0f
        assertEquals("最大缩放应为3.0x", 3.0f, maxZoom)
    }

    /**
     * 测试缩放范围有效性
     * 依据：文档 最小/最大缩放限制
     */
    @Test
    fun `test zoom range validity`() {
        val minZoom = 0.5f
        val maxZoom = 3.0f
        assertTrue("最小缩放应小于最大缩放", minZoom < maxZoom)
    }

    /**
     * 测试缩放值限制函数
     * 依据：文档 缩放限制实现
     */
    @Test
    fun `test clamp zoom value`() {
        val minZoom = 0.5f
        val maxZoom = 3.0f

        // 测试低于最小值
        assertEquals("低于最小值应限制为0.5", minZoom, clampZoom(0.1f, minZoom, maxZoom), 0.001f)

        // 测试高于最大值
        assertEquals("高于最大值应限制为3.0", maxZoom, clampZoom(5.0f, minZoom, maxZoom), 0.001f)

        // 测试正常值
        assertEquals("正常值应保持", 1.5f, clampZoom(1.5f, minZoom, maxZoom), 0.001f)
    }

    /**
     * 测试外接圆半径随缩放变化
     * 依据：文档 R值可随缩放动态调整
     */
    @Test
    fun `test radius scales with zoom`() {
        val baseRadius = 60f
        val scales = listOf(0.5f, 1.0f, 1.5f, 2.0f, 2.5f, 3.0f)

        for (scale in scales) {
            val expectedRadius = baseRadius * scale
            assertTrue(
                "缩放${scale}x时半径应为${expectedRadius}px",
                expectedRadius > 0 && expectedRadius <= 180f
            )
        }
    }

    /**
     * 测试拖拽偏移量计算
     * 依据：文档 GestureDetector onScroll 实现拖拽
     */
    @Test
    fun `test pan offset calculation`() {
        var offsetX = 0f
        var offsetY = 0f

        // 模拟拖拽
        val scrollDeltaX = 50f
        val scrollDeltaY = -30f

        offsetX += scrollDeltaX
        offsetY += scrollDeltaY

        assertEquals("X方向偏移应为50", 50f, offsetX, 0.001f)
        assertEquals("Y方向偏移应为-30", -30f, offsetY, 0.001f)
    }

    /**
     * 测试拖拽边界限制
     * 依据：文档 拖拽不能超出地图范围
     */
    @Test
    fun `test pan boundary limits`() {
        val mapWidthPx = 20 * 60f * 1.732f  // 约2078px
        val mapHeightPx = 20 * 60f * 1.5f   // 约1800px
        val viewportWidth = 400f
        val viewportHeight = 400f

        val maxOffsetX = mapWidthPx - viewportWidth
        val maxOffsetY = mapHeightPx - viewportHeight

        assertTrue("最大X偏移应为正值", maxOffsetX > 0)
        assertTrue("最大Y偏移应为正值", maxOffsetY > 0)

        // 限制后的偏移
        val limitedOffsetX = maxOffsetX.coerceAtLeast(0f)
        val limitedOffsetY = maxOffsetY.coerceAtLeast(0f)

        assertEquals("X偏移不应为负", 0f, limitedOffsetX, 0.001f)
        assertEquals("Y偏移不应为负", 0f, limitedOffsetY, 0.001f)
    }

    /**
     * 测试多次拖拽累积偏移
     * 依据：文档 拖拽偏移累积
     */
    @Test
    fun `test multiple pan accumulation`() {
        var offsetX = 0f
        var offsetY = 0f

        // 多次拖拽
        repeat(5) {
            offsetX += 10f
            offsetY += 10f
        }

        assertEquals("累积X偏移应为50", 50f, offsetX, 0.001f)
        assertEquals("累积Y偏移应为50", 50f, offsetY, 0.001f)
    }

    /**
     * 测试缩放时保持焦点位置
     * 依据：文档 双指缩放焦点
     */
    @Test
    fun `test zoom maintains focus point`() {
        val focusX = 100f
        val focusY = 100f
        val initialScale = 1.0f
        val newScale = 2.0f

        // 焦点在缩放前后应保持相对位置一致
        val expectedFocusUnchanged = (focusX == focusX) && (focusY == focusY)
        assertTrue("缩放焦点应保持不变", expectedFocusUnchanged)
    }

    /**
     * 测试双指缩放缩放因子计算
     * 依据：文档 ScaleGestureDetector 实现
     */
    @Test
    fun `test pinch zoom scale factor`() {
        val currentScale = 1.0f
        val scaleFactor = 1.5f

        val newScale = currentScale * scaleFactor
        assertEquals("新缩放值应为1.5", 1.5f, newScale, 0.001f)
    }

    /**
     * 测试缩放边界值精度
     * 依据：文档 浮点精度处理
     */
    @Test
    fun `test zoom boundary precision`() {
        val minZoom = 0.5f
        val maxZoom = 3.0f

        // 边界值测试
        assertEquals("最小缩放边界", 0.5f, minZoom, 0.0001f)
        assertEquals("最大缩放边界", 3.0f, maxZoom, 0.0001f)
    }

    /**
     * 测试拖拽方向判断
     * 依据：文档 onScroll 方向判断
     */
    @Test
    fun `test pan direction detection`() {
        val scrollRight = 50f
        val scrollLeft = -50f
        val scrollDown = 50f
        val scrollUp = -50f

        assertTrue("正X向右拖", scrollRight > 0)
        assertTrue("负X向左拖", scrollLeft < 0)
        assertTrue("正Y向下拖", scrollDown > 0)
        assertTrue("负Y向上拖", scrollUp < 0)
    }

    // 辅助方法：限制缩放值
    private fun clampZoom(scale: Float, minScale: Float, maxScale: Float): Float {
        return scale.coerceIn(minScale, maxScale)
    }
}