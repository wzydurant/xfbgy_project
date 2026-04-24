package com.xfbgy.hexmap.ui

import org.junit.Assert.*
import org.junit.Test

/**
 * Phase 1-C: 可视化层测试
 * HexMapColors 颜色常量测试
 *
 * 测试依据：《兵棋手游_第一阶段开发方案.md》第十节 颜色规范
 */
class HexMapColorsTest {

    /**
     * 测试平原颜色值
     * 依据：文档 平原 #A8D5A2
     */
    @Test
    fun `test plain color value`() {
        assertEquals("平原颜色应为0xFFA8D5A2", 0xFFA8D5A2.toInt(), HexMapColors.PLAIN)
    }

    /**
     * 测试树林颜色值
     * 依据：文档 树林 #2D6A4F
     */
    @Test
    fun `test forest color value`() {
        assertEquals("树林颜色应为0xFF2D6A4F", 0xFF2D6A4F.toInt(), HexMapColors.FOREST)
    }

    /**
     * 测试山地颜色值
     * 依据：文档 山地 #8B5E3C
     */
    @Test
    fun `test hill color value`() {
        assertEquals("山地颜色应为0xFF8B5E3C", 0xFF8B5E3C.toInt(), HexMapColors.HILL)
    }

    /**
     * 测试高山颜色值
     * 依据：文档 高山 #222222
     */
    @Test
    fun `test mountain color value`() {
        assertEquals("高山颜色应为0xFF222222", 0xFF222222.toInt(), HexMapColors.MOUNTAIN)
    }

    /**
     * 测试建筑群颜色值
     * 依据：文档 建筑群 #9E9E9E
     */
    @Test
    fun `test urban color value`() {
        assertEquals("建筑群颜色应为0xFF9E9E9E", 0xFF9E9E9E.toInt(), HexMapColors.URBAN)
    }

    /**
     * 测试边界线颜色值
     * 依据：文档 边界线 #333333
     */
    @Test
    fun `test border color value`() {
        assertEquals("边界线颜色应为0xFF333333", 0xFF333333.toInt(), HexMapColors.BORDER)
    }

    /**
     * 测试河流颜色值
     * 依据：文档 河流 #1E90FF
     */
    @Test
    fun `test river color value`() {
        assertEquals("河流颜色应为0xFF1E90FF", 0xFF1E90FF.toInt(), HexMapColors.RIVER)
    }

    /**
     * 测试栅栏工事颜色值
     * 依据：文档 栅栏 #8B5E3C
     */
    @Test
    fun `test fence color value`() {
        assertEquals("栅栏颜色应为0xFF8B5E3C", 0xFF8B5E3C.toInt(), HexMapColors.FENCE)
    }

    /**
     * 测试土墙工事颜色值
     * 依据：文档 土墙 #757575
     */
    @Test
    fun `test earthwall color value`() {
        assertEquals("土墙颜色应为0xFF757575", 0xFF757575.toInt(), HexMapColors.EARTHWALL)
    }

    /**
     * 测试石墙工事颜色值
     * 依据：文档 石墙 #FFFFFF
     */
    @Test
    fun `test stonewall color value`() {
        assertEquals("石墙颜色应为0xFFFFFFFF", 0xFFFFFFFF.toInt(), HexMapColors.STONEWALL)
    }

    /**
     * 测试所有地形颜色都是不透明的
     * 依据：文档 颜色值应为完整ARGB
     */
    @Test
    fun `test all terrain colors are opaque`() {
        val terrainColors = listOf(
            HexMapColors.PLAIN,
            HexMapColors.FOREST,
            HexMapColors.HILL,
            HexMapColors.MOUNTAIN,
            HexMapColors.URBAN
        )

        for (color in terrainColors) {
            val alpha = (color shr 24) and 0xFF
            assertTrue(
                "颜色应不透明，alpha=$alpha",
                alpha == 0xFF.toInt()
            )
        }
    }

    /**
     * 测试调试坐标文字颜色
     * 依据：文档 COORD_TEXT
     */
    @Test
    fun `test coord text color`() {
        assertEquals("坐标文字应为黑色", 0xFF000000.toInt(), HexMapColors.COORD_TEXT)
    }

    /**
     * 测试调试坐标背景颜色
     * 依据：文档 COORD_BG
     */
    @Test
    fun `test coord background color has alpha`() {
        val alpha = (HexMapColors.COORD_BG shr 24) and 0xFF
        assertTrue("坐标背景应有透明度", alpha < 0xFF.toInt())
    }

    /**
     * 测试信息面板背景颜色
     * 依据：文档 PANEL_BG
     */
    @Test
    fun `test panel background color`() {
        assertEquals("面板背景应为深灰色", 0xFF2D2D2D.toInt(), HexMapColors.PANEL_BG)
    }

    /**
     * 测试信息面板文字颜色
     * 依据：文档 PANEL_TEXT
     */
    @Test
    fun `test panel text color`() {
        assertEquals("面板文字应为白色", 0xFFFFFFFF.toInt(), HexMapColors.PANEL_TEXT)
    }

    /**
     * 测试信息面板强调色
     * 依据：文档 PANEL_ACCENT
     */
    @Test
    fun `test panel accent color`() {
        assertEquals("面板强调色应为蓝色", 0xFF1E90FF.toInt(), HexMapColors.PANEL_ACCENT)
    }
}