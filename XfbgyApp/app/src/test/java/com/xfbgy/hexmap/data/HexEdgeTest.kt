package com.xfbgy.hexmap.data

import org.junit.Assert.*
import org.junit.Test

/**
 * Phase 1-A: 数据层测试
 * HexEdge 六角格边缘属性测试
 *
 * 测试依据：《兵棋手游_第一阶段开发方案.md》第二节 HexEdge定义
 */
class HexEdgeTest {

    /**
     * 测试 HexEdge 基本构造默认值
     * 依据：文档 HexEdge 数据类字段定义
     */
    @Test
    fun `test hex edge default construction`() {
        val edge = HexEdge()

        assertFalse("河流默认应为false", edge.hasRiver)
        assertEquals("防御工事默认为NONE", FortType.NONE, edge.fortification)
        assertEquals("移动破坏初始为0", 0, edge.movementPenalty)
        assertEquals("防御优势初始为0", 0, edge.defenseBonus)
        assertTrue("条件防御优势列表应为空", edge.defenseConditions.isEmpty())
        assertEquals("进攻优势初始为0", 0, edge.attackBonus)
        assertTrue("条件进攻优势列表应为空", edge.attackConditions.isEmpty())
    }

    /**
     * 测试设置河流
     * 依据：文档 hasRiver 属性
     */
    @Test
    fun `test set river`() {
        val edge = HexEdge()
        edge.hasRiver = true

        assertTrue("河流应被设置", edge.hasRiver)
    }

    /**
     * 测试设置防御工事
     * 依据：文档 fortification 属性
     */
    @Test
    fun `test set fortification`() {
        val edge = HexEdge()
        edge.fortification = FortType.FENCE

        assertEquals("防御工事应为栅栏", FortType.FENCE, edge.fortification)
    }

    /**
     * 测试所有防御工事类型
     * 依据：文档 FortType 枚举
     */
    @Test
    fun `test all fortification types`() {
        for (fortType in FortType.entries) {
            val edge = HexEdge()
            edge.fortification = fortType
            assertEquals("防御工事应匹配", fortType, edge.fortification)
        }
    }

    /**
     * 测试移动破坏属性
     * 依据：文档 movementPenalty 属性
     */
    @Test
    fun `test movement penalty accumulation`() {
        val edge = HexEdge()

        // 叠加移动破坏
        edge.movementPenalty += 1
        assertEquals("移动破坏应为1", 1, edge.movementPenalty)

        edge.movementPenalty += 2
        assertEquals("移动破坏应为3", 3, edge.movementPenalty)
    }

    /**
     * 测试防御优势属性
     * 依据：文档 defenseBonus 属性
     */
    @Test
    fun `test defense bonus accumulation`() {
        val edge = HexEdge()

        // 叠加防御优势
        edge.defenseBonus += 1
        assertEquals("防御优势应为1", 1, edge.defenseBonus)

        edge.defenseBonus += 2
        assertEquals("防御优势应为3", 3, edge.defenseBonus)
    }

    /**
     * 测试进攻优势属性
     * 依据：文档 attackBonus 属性
     */
    @Test
    fun `test attack bonus accumulation`() {
        val edge = HexEdge()

        // 叠加进攻优势
        edge.attackBonus += 1
        assertEquals("进攻优势应为1", 1, edge.attackBonus)

        edge.attackBonus += 2
        assertEquals("进攻优势应为3", 3, edge.attackBonus)
    }

    /**
     * 测试条件防御优势列表
     * 依据：文档 defenseConditions 属性
     */
    @Test
    fun `test defense conditions list`() {
        val edge = HexEdge()

        edge.defenseConditions.add("骑兵")
        edge.defenseConditions.add("对攻")

        assertEquals("条件列表应有2个元素", 2, edge.defenseConditions.size)
        assertTrue("应包含骑兵", edge.defenseConditions.contains("骑兵"))
        assertTrue("应包含对攻", edge.defenseConditions.contains("对攻"))
    }

    /**
     * 测试条件进攻优势列表
     * 依据：文档 attackConditions 属性
     */
    @Test
    fun `test attack conditions list`() {
        val edge = HexEdge()

        edge.attackConditions.add("远射")
        edge.attackConditions.add("炮击")

        assertEquals("条件列表应有2个元素", 2, edge.attackConditions.size)
        assertTrue("应包含远射", edge.attackConditions.contains("远射"))
        assertTrue("应包含炮击", edge.attackConditions.contains("炮击"))
    }

    /**
     * 测试河流效果叠加（移动破坏+防御优势）
     * 依据：文档 河流对边属性的影响
     */
    @Test
    fun `test river effects on edge`() {
        val edge = HexEdge()
        edge.hasRiver = true

        // 河流应叠加移动破坏+1和防御优势+1
        edge.movementPenalty += 1  // 河流效果
        edge.defenseBonus += 1    // 河流效果

        assertEquals("河流移动破坏应为1", 1, edge.movementPenalty)
        assertEquals("河流防御优势应为1", 1, edge.defenseBonus)
    }

    /**
     * 测试河流与工事叠加
     * 依据：文档 河流与工事效果叠加说明
     */
    @Test
    fun `test river and fortification stacking`() {
        val edge = HexEdge()
        edge.hasRiver = true
        edge.fortification = FortType.EARTHWALL

        // 叠加河流效果
        edge.movementPenalty += 1  // 河流
        edge.defenseBonus += 1    // 河流

        // 叠加工事效果（土墙：移动破坏+1，防御优势+2）
        edge.movementPenalty += edge.fortification.movementPenalty
        edge.defenseBonus += edge.fortification.defenseBonus

        assertEquals("移动破坏应为2（河流1+土墙1）", 2, edge.movementPenalty)
        assertEquals("防御优势应为3（河流1+土墙2）", 3, edge.defenseBonus)
    }
}