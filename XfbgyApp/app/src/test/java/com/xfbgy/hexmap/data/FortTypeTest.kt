package com.xfbgy.hexmap.data

import org.junit.Assert.*
import org.junit.Test

/**
 * Phase 1-A: 数据层测试
 * FortType 枚举测试
 *
 * 测试依据：《兵棋手游_第一阶段开发方案.md》第二节 防御工事枚举定义
 */
class FortTypeTest {

    /**
     * 测试防御工事枚举数量
     * 依据：文档规定4种工事类型 NONE, FENCE, EARTHWALL, STONEWALL
     */
    @Test
    fun `test fort type count is four`() {
        assertEquals("防御工事类型数量应为4种", 4, FortType.entries.size)
    }

    /**
     * 测试无工事属性
     * 依据：文档 Table 防御工事枚举 - NONE
     */
    @Test
    fun `test none fort properties`() {
        val none = FortType.NONE
        assertEquals("无", none.chineseName)
        assertEquals("—", none.visualStyle)
        assertEquals(0, none.movementPenalty)
        assertEquals(0, none.defenseBonus)
    }

    /**
     * 测试栅栏属性
     * 依据：文档 Table 防御工事枚举 - FENCE
     */
    @Test
    fun `test fence fort properties`() {
        val fence = FortType.FENCE
        assertEquals("栅栏", fence.chineseName)
        assertEquals("褐色小叉连线", fence.visualStyle)
        assertEquals(0, fence.movementPenalty)
        assertEquals(1, fence.defenseBonus)
    }

    /**
     * 测试土墙属性
     * 依据：文档 Table 防御工事枚举 - EARTHWALL
     */
    @Test
    fun `test earthwall fort properties`() {
        val earthwall = FortType.EARTHWALL
        assertEquals("土墙", earthwall.chineseName)
        assertEquals("灰色小叉连线", earthwall.visualStyle)
        assertEquals(1, earthwall.movementPenalty)
        assertEquals(2, earthwall.defenseBonus)
    }

    /**
     * 测试石墙属性
     * 依据：文档 Table 防御工事枚举 - STONEWALL
     */
    @Test
    fun `test stonewall fort properties`() {
        val stonewall = FortType.STONEWALL
        assertEquals("石墙", stonewall.chineseName)
        assertEquals("白色小叉连线", stonewall.visualStyle)
        assertEquals(1, stonewall.movementPenalty)
        assertEquals(1, stonewall.defenseBonus)
    }

    /**
     * 测试 fromOrdinal 有效范围
     * 依据：文档 FortType.fromOrdinal 方法
     */
    @Test
    fun `test fromOrdinal valid range`() {
        assertEquals(FortType.NONE, FortType.fromOrdinal(0))
        assertEquals(FortType.FENCE, FortType.fromOrdinal(1))
        assertEquals(FortType.EARTHWALL, FortType.fromOrdinal(2))
        assertEquals(FortType.STONEWALL, FortType.fromOrdinal(3))
    }

    /**
     * 测试 fromOrdinal 超出范围时返回NONE默认值
     * 依据：文档 fromOrdinal 异常处理
     */
    @Test
    fun `test fromOrdinal out of range returns none`() {
        assertEquals(FortType.NONE, FortType.fromOrdinal(-1))
        assertEquals(FortType.NONE, FortType.fromOrdinal(4))
        assertEquals(FortType.NONE, FortType.fromOrdinal(100))
    }

    /**
     * 测试 randomFortification 不返回NONE
     * 依据：文档 randomFortification 方法用于建筑群生成
     */
    @Test
    fun `test randomFortification never returns none`() {
        repeat(100) {
            val randomFort = FortType.randomFortification()
            assertNotEquals(
                "randomFortification 不应返回 NONE",
                FortType.NONE,
                randomFort
            )
        }
    }

    /**
     * 测试 randomFortification 只返回有效工事类型
     * 依据：文档 randomFortification 方法
     */
    @Test
    fun `test randomFortification only returns valid types`() {
        val validTypes = setOf(FortType.FENCE, FortType.EARTHWALL, FortType.STONEWALL)
        repeat(100) {
            val randomFort = FortType.randomFortification()
            assertTrue(
                "randomFortification 应返回有效工事类型",
                randomFort in validTypes
            )
        }
    }

    /**
     * 测试土墙移动破坏值大于栅栏
     * 依据：文档防御工事效果 - 土墙移动破坏+1
     */
    @Test
    fun `test earthwall has higher movement penalty than fence`() {
        assertTrue(
            "土墙移动破坏应大于栅栏",
            FortType.EARTHWALL.movementPenalty > FortType.FENCE.movementPenalty
        )
    }

    /**
     * 测试土墙防御优势大于栅栏和石墙
     * 依据：文档防御工事效果 - 土墙防御优势+2
     */
    @Test
    fun `test earthwall has highest defense bonus`() {
        assertTrue(
            "土墙防御优势应大于栅栏",
            FortType.EARTHWALL.defenseBonus > FortType.FENCE.defenseBonus
        )
        assertTrue(
            "土墙防御优势应大于石墙",
            FortType.EARTHWALL.defenseBonus > FortType.STONEWALL.defenseBonus
        )
    }
}