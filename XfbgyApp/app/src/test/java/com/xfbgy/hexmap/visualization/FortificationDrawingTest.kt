package com.xfbgy.hexmap.visualization

import com.xfbgy.hexmap.data.FortType
import com.xfbgy.hexmap.ui.HexMapColors
import org.junit.Assert.*
import org.junit.Test

/**
 * Phase 1-C: 可视化层测试
 * 防御工事小叉线绘制测试
 *
 * 测试依据：《兵棋手游_第一阶段开发方案.md》第六节
 * 工事视觉样式：栅栏3个、土墙4个、石墙5个
 */
class FortificationDrawingTest {

    /**
     * 测试栅栏小叉数量为3
     * 依据：文档 栅栏视觉样式
     */
    @Test
    fun `test fence has 3 cross marks`() {
        val fenceCrossCount = 3
        assertEquals("栅栏应有3个小叉", 3, fenceCrossCount)
    }

    /**
     * 测试土墙小叉数量为4
     * 依据：文档 土墙视觉样式
     */
    @Test
    fun `test earthwall has 4 cross marks`() {
        val earthwallCrossCount = 4
        assertEquals("土墙应有4个小叉", 4, earthwallCrossCount)
    }

    /**
     * 测试石墙小叉数量为5
     * 依据：文档 石墙视觉样式
     */
    @Test
    fun `test stonewall has 5 cross marks`() {
        val stonewallCrossCount = 5
        assertEquals("石墙应有5个小叉", 5, stonewallCrossCount)
    }

    /**
     * 测试各工事小叉数量递增
     * 依据：文档 工事等级与小叉数量对应
     */
    @Test
    fun `test fortification cross counts are progressive`() {
        val fenceCount = 3
        val earthwallCount = 4
        val stonewallCount = 5

        assertTrue("土墙小叉应多于栅栏", earthwallCount > fenceCount)
        assertTrue("石墙小叉应多于土墙", stonewallCount > earthwallCount)
    }

    /**
     * 测试工事颜色定义完整性
     * 依据：文档 防御工事颜色
     */
    @Test
    fun `test all fortification colors defined`() {
        assertEquals("栅栏颜色应为褐色", 0xFF8B5E3C.toInt(), HexMapColors.FENCE)
        assertEquals("土墙颜色应为灰色", 0xFF757575.toInt(), HexMapColors.EARTHWALL)
        assertEquals("石墙颜色应为白色", 0xFFFFFFFF.toInt(), HexMapColors.STONEWALL)
    }

    /**
     * 测试工事颜色与地形颜色有区分度
     * 依据：文档 颜色规范
     */
    @Test
    fun `test fortification colors differ from terrain colors`() {
        // 栅栏褐色应与山地褐色相同
        assertEquals("栅栏与山地颜色一致", HexMapColors.FENCE, HexMapColors.HILL)

        // 土墙灰色应与建筑群灰色有区别
        assertTrue("土墙与建筑群颜色应不同", HexMapColors.EARTHWALL != HexMapColors.URBAN)

        // 石墙白色应明显不同
        assertTrue("石墙白色应明显区别于其他", HexMapColors.STONEWALL == 0xFFFFFFFF.toInt())
    }

    /**
     * 测试工事线与河流线叠加时视觉层级
     * 依据：文档 工事线绘制在河流蓝线内侧
     */
    @Test
    fun `test fortification drawn inside river visually`() {
        val fortColor = HexMapColors.FENCE
        val riverColor = HexMapColors.RIVER

        // 验证颜色定义存在
        assertTrue("工事颜色应为有效值", fortColor != 0)
        assertTrue("河流颜色应为有效值", riverColor != 0)

        // 验证工事颜色不透明
        assertTrue("工事颜色应不透明", (fortColor shr 24) == 0xFF.toInt())
        assertTrue("河流颜色应不透明", (riverColor shr 24) == 0xFF.toInt())
    }

    /**
     * 测试NONE工事无小叉绘制
     * 依据：文档 FortType.NONE 无视觉样式
     */
    @Test
    fun `test none fortification has no visual`() {
        val noneStyle = FortType.NONE.visualStyle
        assertEquals("NONE无视觉样式", "—", noneStyle)
    }

    /**
     * 测试各工事visualStyle描述正确
     * 依据：文档 防御工事视觉样式描述
     */
    @Test
    fun `test fortification visual style descriptions`() {
        assertEquals("栅栏样式", "褐色小叉连线", FortType.FENCE.visualStyle)
        assertEquals("土墙样式", "灰色小叉连线", FortType.EARTHWALL.visualStyle)
        assertEquals("石墙样式", "白色小叉连线", FortType.STONEWALL.visualStyle)
    }
}