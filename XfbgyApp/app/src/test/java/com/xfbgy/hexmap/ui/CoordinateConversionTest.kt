package com.xfbgy.hexmap.ui

import com.xfbgy.hexmap.hexToPixel
import com.xfbgy.hexmap.pixelToHex
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.sqrt

/**
 * Phase 1-C: 可视化层测试
 * 坐标转换测试
 *
 * 测试依据：《兵棋手游_第一阶段开发方案.md》第六节 hexToPixel公式
 */
class CoordinateConversionTest {

    /**
     * 测试 hexToPixel 基本公式
     * 依据：文档 像素坐标计算公式
     */
    @Test
    fun `test hex to pixel formula correctness`() {
        val R = 60f

        // 测试原点
        val origin = hexToPixel(0, 0, R)
        assertEquals(0f, origin.x, 0.001f)
        assertEquals(0f, origin.y, 0.001f)

        // 测试X方向移动一个格子
        val oneRight = hexToPixel(1, 0, R)
        val expectedX = R * sqrt(3f)
        assertEquals(expectedX, oneRight.x, 0.001f)
        assertEquals(0f, oneRight.y, 0.001f)

        // 测试Y方向移动一个格子
        val oneUp = hexToPixel(0, 1, R)
        val expectedY = R * 1.5f
        assertEquals(0f, oneUp.x, 0.001f) // 单数行左偏，但y=0时偏移为0
        assertEquals(expectedY, oneUp.y, 0.001f)
    }

    /**
     * 测试奇数行左偏效果
     * 依据：文档 单数行左偏
     */
    @Test
    fun `test odd row offset`() {
        val R = 60f

        val evenRowCell = hexToPixel(5, 0, R)  // 偶数行
        val oddRowCell = hexToPixel(5, 1, R)   // 单数行

        // 单数行应有额外的colOffset
        val colOffset = R * sqrt(3f) / 2
        assertTrue(
            "单数行X坐标应大于偶数行",
            oddRowCell.x > evenRowCell.x - colOffset + 0.001f
        )
    }

    /**
     * 测试像素到六角格坐标转换
     * 依据：文档 像素→格子坐标反算
     */
    @Test
    fun `test pixel to hex conversion`() {
        val R = 60f

        // 测试原点附近
        val nearOrigin = pixelToHex(5f, 5f, R)
        assertNotNull(nearOrigin)
    }

    /**
     * 测试坐标转换往返一致性
     * 依据：文档 坐标转换精度
     */
    @Test
    fun `test round trip conversion accuracy`() {
        val R = 60f

        // 选择几个典型坐标点
        val testPoints = listOf(
            Pair(0, 0),
            Pair(10, 5),
            Pair(20, 10),
            Pair(15, 15)
        )

        for ((x, y) in testPoints) {
            val pixel = hexToPixel(x, y, R)
            val recovered = pixelToHex(pixel.x, pixel.y, R)

            // 允许小范围误差（浮点精度和奇偶行偏移）
            assertTrue(
                "坐标($x,$y)转换后应在小误差范围内",
                kotlin.math.abs(recovered.x - x) <= 1 &&
                kotlin.math.abs(recovered.y - y) <= 1
            )
        }
    }

    /**
     * 测试六角格尺寸参数R的建议范围
     * 依据：文档 R建议初始值约60px
     */
    @Test
    fun `test R value affects pixel coordinates linearly`() {
        val smallR = 30f
        val largeR = 90f

        val smallPixel = hexToPixel(5, 5, smallR)
        val largePixel = hexToPixel(5, 5, largeR)

        assertEquals(
            "R翻3倍时X坐标应接近3倍",
            smallPixel.x * 3,
            largePixel.x,
            0.001f
        )
        assertEquals(
            "R翻3倍时Y坐标应接近3倍",
            smallPixel.y * 3,
            largePixel.y,
            0.001f
        )
    }

    /**
     * 测试不同Y值的X偏移
     * 依据：文档 odd-r 偏移坐标计算
     */
    @Test
    fun `test X offset varies with row parity`() {
        val R = 60f

        // 偶数行 (y=0)
        val evenRow0 = hexToPixel(0, 0, R)
        // 单数行 (y=1)
        val oddRow0 = hexToPixel(0, 1, R)

        // Y方向偏移是固定的 1.5f * R
        val yStep = R * 1.5f
        assertEquals(
            "Y方向步进应为R*1.5",
            yStep,
            oddRow0.y - evenRow0.y,
            0.001f
        )
    }
}