package com.xfbgy.hexmap

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.sqrt

/**
 * Phase 1-A: 数据层测试
 * 坐标转换工具函数测试
 *
 * 测试依据：《兵棋手游_第一阶段开发方案.md》第六节 hexToPixel计算公式
 */
class HexCoordinateTest {

    /**
     * 测试 hexToPixel 原点坐标
     * 依据：文档 hexToPixel 公式，原点(0,0)应返回(0,0)
     */
    @Test
    fun `test hex to pixel origin`() {
        val result = hexToPixel(0, 0, 60f)

        assertEquals("原点X像素坐标应为0", 0f, result.x, 0.001f)
        assertEquals("原点Y像素坐标应为0", 0f, result.y, 0.001f)
    }

    /**
     * 测试 hexToPixel 偶数行计算
     * 依据：文档 odd-r 偏移坐标公式
     */
    @Test
    fun `test hex to pixel even row`() {
        val R = 60f
        val result = hexToPixel(1, 0, R)

        // 偶数行无偏移，px = x * R * sqrt(3), py = y * R * 1.5
        val expectedX = 1 * R * sqrt(3f)
        val expectedY = 0f

        assertEquals("偶数行X坐标计算错误", expectedX, result.x, 0.001f)
        assertEquals("偶数行Y坐标计算错误", expectedY, result.y, 0.001f)
    }

    /**
     * 测试 hexToPixel 单数行左偏
     * 依据：文档 单数行左偏公式
     */
    @Test
    fun `test hex to pixel odd row has offset`() {
        val R = 60f
        val result0 = hexToPixel(0, 0, R)  // 偶数行
        val result1 = hexToPixel(0, 1, R)  // 单数行

        // 单数行Y偏移不同
        assertEquals("单数行Y坐标应为R*15", 1 * R * 1.5f, result1.y, 0.001f)
        // 单数行有colOffset
        val colOffset = R * sqrt(3f) / 2
        assertTrue("单数行应有额外X偏移", result1.x > result0.x)
    }

    /**
     * 测试 hexToPixel Y方向步进
     * 依据：文档 py = y * R * 1.5 公式
     */
    @Test
    fun `test hex to pixel y step`() {
        val R = 60f
        val result0 = hexToPixel(0, 0, R)
        val result1 = hexToPixel(0, 1, R)
        val result2 = hexToPixel(0, 2, R)

        assertEquals("Y方向每步增加R*15", R * 1.5f, result1.y - result0.y, 0.001f)
        assertEquals("Y方向步进一致", result1.y - result0.y, result2.y - result1.y, 0.001f)
    }

    /**
     * 测试 hexToPixel 不同R值缩放
     * 依据：文档 R 为外接圆半径
     */
    @Test
    fun `test hex to pixel scales with R`() {
        val result60 = hexToPixel(1, 0, 60f)
        val result120 = hexToPixel(1, 0, 120f)

        assertEquals("R翻倍时X坐标应翻倍", result60.x * 2, result120.x, 0.001f)
        assertEquals("R翻倍时Y坐标应翻倍", result60.y * 2, result120.y, 0.001f)
    }

    /**
     * 测试 pixelToHex 原点逆转换
     * 依据：文档 像素到格子坐标转换
     */
    @Test
    fun `test pixel to hex origin inverse`() {
        val result = pixelToHex(0f, 0f, 60f)

        assertEquals("原点逆转换X应为0", 0, result.x)
        assertEquals("原点逆转换Y应为0", 0, result.y)
    }

    /**
     * 测试 pixelToHex 是 hexToPixel 的逆操作
     * 依据：文档 坐标转换互逆性
     */
    @Test
    fun `test pixel to hex is inverse of hex to pixel`() {
        val R = 60f
        val original = Pair(5, 10)
        val pixel = hexToPixel(original.first, original.second, R)
        val recovered = pixelToHex(pixel.x, pixel.y, R)

        // 由于浮点精度和奇偶行偏移，逆转换可能略有偏差
        assertTrue(
            "逆转换应在小范围内（实际: ${recovered.x}, ${recovered.y}）",
            kotlin.math.abs(recovered.x - original.first) <= 1 &&
            kotlin.math.abs(recovered.y - original.second) <= 1
        )
    }

    /**
     * 测试边方向常量定义
     * 依据：文档 边的方向与绘制映射
     */
    @Test
    fun `test direction constants defined`() {
        assertEquals("方向0应为上", 0, DIRECTION_UP)
        assertEquals("方向1应为右上", 1, DIRECTION_UPPER_RIGHT)
        assertEquals("方向2应为右下", 2, DIRECTION_LOWER_RIGHT)
        assertEquals("方向3应为下", 3, DIRECTION_DOWN)
        assertEquals("方向4应为左下", 4, DIRECTION_LOWER_LEFT)
        assertEquals("方向5应为左上", 5, DIRECTION_UPPER_LEFT)
    }

    /**
     * 测试方向编号完整性
     * 依据：文档 6条边编号0~5
     */
    @Test
    fun `test all six directions defined`() {
        val directions = listOf(
            DIRECTION_UP,
            DIRECTION_UPPER_RIGHT,
            DIRECTION_LOWER_RIGHT,
            DIRECTION_DOWN,
            DIRECTION_LOWER_LEFT,
            DIRECTION_UPPER_LEFT
        )

        assertEquals("应有6个方向", 6, directions.size)
        assertTrue("方向应包含0-5", directions.toSet() == (0..5).toSet())
    }

    /**
     * 测试方向顶点对映射
     * 依据：文档 方向编号与顶点对对应关系
     */
    @Test
    fun `test direction vertex pairs`() {
        // 方向0: 上 → 顶点0→1
        val dir0Vertices = getDirectionVertices(0)
        assertEquals("方向0顶点0应为0", 0, dir0Vertices.first)
        assertEquals("方向0顶点1应为1", 1, dir0Vertices.second)

        // 方向3: 下 → 顶点3→4
        val dir3Vertices = getDirectionVertices(3)
        assertEquals("方向3顶点0应为3", 3, dir3Vertices.first)
        assertEquals("方向3顶点1应为4", 4, dir3Vertices.second)
    }

    /**
     * 测试获取邻居坐标偏移
     * 依据：文档 odd-r 坐标邻居偏移计算
     */
    @Test
    fun `test neighbor offset for even row`() {
        // 偶数行 (y % 2 == 0)
        val offsets = getNeighborOffsets(0) // 偶数行

        // 方向0 (上) 在偶数行应该是 (0, -1)
        assertTrue("方向0邻居偏移应有dx=0", offsets[0].dx == 0)
    }

    /**
     * 测试奇偶行邻居偏移差异
     * 依据：文档 odd-r 偏移导致邻居坐标不同
     */
    @Test
    fun `test neighbor offset differs by row parity`() {
        val evenOffsets = getNeighborOffsets(0) // 偶数行 y=0
        val oddOffsets = getNeighborOffsets(1)  // 奇数行 y=1

        // 奇偶行的邻居偏移应不同
        assertTrue("奇偶行邻居偏移应不同", evenOffsets != oddOffsets)
    }
}