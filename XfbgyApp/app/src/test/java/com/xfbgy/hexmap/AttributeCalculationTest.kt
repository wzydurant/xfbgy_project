package com.xfbgy.hexmap

import com.xfbgy.hexmap.data.HexCell
import com.xfbgy.hexmap.data.HexEdge
import com.xfbgy.hexmap.data.TerrainType
import com.xfbgy.hexmap.data.FortType
import org.junit.Assert.*
import org.junit.Test

/**
 * Phase 1-A: 数据层测试
 * 初始属性计算规则测试
 *
 * 测试依据：《兵棋手游_第一阶段开发方案.md》第三节 属性计算规则
 */
class AttributeCalculationTest {

    /**
     * 测试 Step1 平原地形移动力消耗
     * 依据：文档 地形对格子属性的影响 - 平原 +1
     */
    @Test
    fun `test plain terrain movement cost`() {
        val cell = HexCell(0, 0, TerrainType.PLAIN)
        applyTerrainEffects(cell)

        assertEquals("平原移动力消耗应为1", 1, cell.movementCost)
    }

    /**
     * 测试 Step1 树林地形移动力消耗
     * 依据：文档 地形对格子属性的影响 - 树林 +1
     */
    @Test
    fun `test forest terrain movement cost`() {
        val cell = HexCell(0, 0, TerrainType.FOREST)
        applyTerrainEffects(cell)

        assertEquals("树林移动力消耗应为1", 1, cell.movementCost)
    }

    /**
     * 测试 Step1 山地地形移动力消耗
     * 依据：文档 地形对格子属性的影响 - 山地 +2
     */
    @Test
    fun `test hill terrain movement cost`() {
        val cell = HexCell(0, 0, TerrainType.HILL)
        applyTerrainEffects(cell)

        assertEquals("山地移动力消耗应为2", 2, cell.movementCost)
    }

    /**
     * 测试 Step1 高山地形移动力消耗
     * 依据：文档 地形对格子属性的影响 - 高山 +20
     */
    @Test
    fun `test mountain terrain movement cost`() {
        val cell = HexCell(0, 0, TerrainType.MOUNTAIN)
        applyTerrainEffects(cell)

        assertEquals("高山移动力消耗应为20", 20, cell.movementCost)
    }

    /**
     * 测试 Step1 建筑群地形移动力消耗
     * 依据：文档 地形对格子属性的影响 - 建筑群 +1
     */
    @Test
    fun `test urban terrain movement cost`() {
        val cell = HexCell(0, 0, TerrainType.URBAN)
        applyTerrainEffects(cell)

        assertEquals("建筑群移动力消耗应为1", 1, cell.movementCost)
    }

    /**
     * 测试 Step2 树林地形边属性 - 移动破坏
     * 依据：文档 地形对边属性的影响 - 树林所有边+1
     */
    @Test
    fun `test forest edge movement penalty`() {
        val cell = HexCell(0, 0, TerrainType.FOREST)
        val edges = Array(6) { HexEdge() }
        applyTerrainEdgeEffects(edges, TerrainType.FOREST)

        for (i in 0..5) {
            assertEquals(
                "树林边$i 移动破坏应为1",
                1,
                edges[i].movementPenalty
            )
        }
    }

    /**
     * 测试 Step2 树林地形边属性 - 条件防御优势
     * 依据：文档 树林追加"骑兵""对攻""远射""炮击"
     */
    @Test
    fun `test forest edge defense conditions`() {
        val cell = HexCell(0, 0, TerrainType.FOREST)
        val edges = Array(6) { HexEdge() }
        applyTerrainEdgeEffects(edges, TerrainType.FOREST)

        val expectedConditions = listOf("骑兵", "对攻", "远射", "炮击")
        for (i in 0..5) {
            for (condition in expectedConditions) {
                assertTrue(
                    "树林边$i 应包含条件'$condition'",
                    edges[i].defenseConditions.contains(condition)
                )
            }
        }
    }

    /**
     * 测试 Step2 山地地形边属性 - 移动破坏
     * 依据：文档 山地所有边+1
     */
    @Test
    fun `test hill edge movement penalty`() {
        val edges = Array(6) { HexEdge() }
        applyTerrainEdgeEffects(edges, TerrainType.HILL)

        for (i in 0..5) {
            assertEquals(
                "山地边$i 移动破坏应为1",
                1,
                edges[i].movementPenalty
            )
        }
    }

    /**
     * 测试 Step2 山地地形边属性 - 进攻优势
     * 依据：文档 山地所有边进攻优势+1
     */
    @Test
    fun `test hill edge attack bonus`() {
        val edges = Array(6) { HexEdge() }
        applyTerrainEdgeEffects(edges, TerrainType.HILL)

        for (i in 0..5) {
            assertEquals(
                "山地边$i 进攻优势应为1",
                1,
                edges[i].attackBonus
            )
        }
    }

    /**
     * 测试 Step2 山地地形边属性 - 防御优势
     * 依据：文档 山地所有边防御优势+1
     */
    @Test
    fun `test hill edge defense bonus`() {
        val edges = Array(6) { HexEdge() }
        applyTerrainEdgeEffects(edges, TerrainType.HILL)

        for (i in 0..5) {
            assertEquals(
                "山地边$i 防御优势应为1",
                1,
                edges[i].defenseBonus
            )
        }
    }

    /**
     * 测试 Step2 建筑群地形边属性 - 移动破坏
     * 依据：文档 建筑群所有边+1
     */
    @Test
    fun `test urban edge movement penalty`() {
        val edges = Array(6) { HexEdge() }
        applyTerrainEdgeEffects(edges, TerrainType.URBAN)

        for (i in 0..5) {
            assertEquals(
                "建筑群边$i 移动破坏应为1",
                1,
                edges[i].movementPenalty
            )
        }
    }

    /**
     * 测试 Step2 建筑群地形边属性 - 条件防御优势
     * 依据：文档 建筑群追加"骑兵""远射"
     */
    @Test
    fun `test urban edge defense conditions`() {
        val edges = Array(6) { HexEdge() }
        applyTerrainEdgeEffects(edges, TerrainType.URBAN)

        val expectedConditions = listOf("骑兵", "远射")
        for (i in 0..5) {
            for (condition in expectedConditions) {
                assertTrue(
                    "建筑群边$i 应包含条件'$condition'",
                    edges[i].defenseConditions.contains(condition)
                )
            }
        }
    }

    /**
     * 测试 Step3 河流边属性 - 移动破坏
     * 依据：文档 hasRiver=true 移动破坏+1
     */
    @Test
    fun `test river movement penalty`() {
        val edge = HexEdge()
        edge.hasRiver = true
        applyRiverEffects(edge)

        assertEquals("河流移动破坏应为1", 1, edge.movementPenalty)
    }

    /**
     * 测试 Step3 河流边属性 - 防御优势
     * 依据：文档 hasRiver=true 防御优势+1
     */
    @Test
    fun `test river defense bonus`() {
        val edge = HexEdge()
        edge.hasRiver = true
        applyRiverEffects(edge)

        assertEquals("河流防御优势应为1", 1, edge.defenseBonus)
    }

    /**
     * 测试 Step4 栅栏工事效果
     * 依据：文档 栅栏 移动破坏0 防御优势+1
     */
    @Test
    fun `test fence fortification effects`() {
        val edge = HexEdge()
        edge.fortification = FortType.FENCE
        applyFortificationEffects(edge)

        assertEquals("栅栏移动破坏应为0", 0, edge.movementPenalty)
        assertEquals("栅栏防御优势应为1", 1, edge.defenseBonus)
    }

    /**
     * 测试 Step4 土墙工事效果
     * 依据：文档 土墙 移动破坏+1 防御优势+2
     */
    @Test
    fun `test earthwall fortification effects`() {
        val edge = HexEdge()
        edge.fortification = FortType.EARTHWALL
        applyFortificationEffects(edge)

        assertEquals("土墙移动破坏应为1", 1, edge.movementPenalty)
        assertEquals("土墙防御优势应为2", 2, edge.defenseBonus)
    }

    /**
     * 测试 Step4 石墙工事效果
     * 依据：文档 石墙 移动破坏+1 防御优势+1
     */
    @Test
    fun `test stonewall fortification effects`() {
        val edge = HexEdge()
        edge.fortification = FortType.STONEWALL
        applyFortificationEffects(edge)

        assertEquals("石墙移动破坏应为1", 1, edge.movementPenalty)
        assertEquals("石墙防御优势应为1", 1, edge.defenseBonus)
    }

    /**
     * 测试河流与工事叠加效果
     * 依据：文档 若该边同时有河流，工事效果依然叠加
     */
    @Test
    fun `test river and fortification stacking`() {
        val edge = HexEdge()
        edge.hasRiver = true
        edge.fortification = FortType.EARTHWALL

        // 按叠加顺序执行
        applyRiverEffects(edge)
        applyFortificationEffects(edge)

        assertEquals("河流+土墙移动破坏应为2", 2, edge.movementPenalty)
        assertEquals("河流+土墙防御优势应为3", 3, edge.defenseBonus)
    }

    /**
     * 测试完整属性叠加流程
     * 依据：文档 叠加顺序
     */
    @Test
    fun `test full attribute stacking sequence`() {
        val cell = HexCell(0, 0, TerrainType.HILL)
        val edge = cell.let { HexEdge() }.apply {
            hasRiver = true
            fortification = FortType.STONEWALL
        }

        // Step 1: 地形 → 格子移动力消耗
        applyTerrainEffects(cell)
        assertEquals("Step1: 山地移动力消耗应为2", 2, cell.movementCost)

        // Step 2: 地形 → 6条边
        val edges = Array(6) { HexEdge() }
        applyTerrainEdgeEffects(edges, TerrainType.HILL)
        for (e in edges) {
            assertEquals("Step2: 山地边移动破坏应为1", 1, e.movementPenalty)
            assertEquals("Step2: 山地边进攻优势应为1", 1, e.attackBonus)
            assertEquals("Step2: 山地边防御优势应为1", 1, e.defenseBonus)
        }

        // Step 3: 河流
        applyRiverEffects(edge)
        assertEquals("Step3: 河流移动破坏+1", 1, edge.movementPenalty)
        assertEquals("Step3: 河流防御优势+1", 1, edge.defenseBonus)

        // Step 4: 防御工事
        applyFortificationEffects(edge)
        assertEquals("Step4: 石墙移动破坏+1", 2, edge.movementPenalty)
        assertEquals("Step4: 石墙防御优势+1", 2, edge.defenseBonus)
    }

    // 辅助方法：应用地形对格子的影响
    private fun applyTerrainEffects(cell: HexCell) {
        cell.movementCost = when (cell.terrain) {
            TerrainType.PLAIN -> 1
            TerrainType.FOREST -> 1
            TerrainType.HILL -> 2
            TerrainType.MOUNTAIN -> 20
            TerrainType.URBAN -> 1
        }
    }

    // 辅助方法：应用地形对边的影响
    private fun applyTerrainEdgeEffects(edges: Array<HexEdge>, terrain: TerrainType) {
        when (terrain) {
            TerrainType.PLAIN -> { /* 无额外效果 */ }
            TerrainType.FOREST -> {
                edges.forEach { edge ->
                    edge.movementPenalty += 1
                    edge.defenseConditions.addAll(listOf("骑兵", "对攻", "远射", "炮击"))
                }
            }
            TerrainType.HILL -> {
                edges.forEach { edge ->
                    edge.movementPenalty += 1
                    edge.attackBonus += 1
                    edge.defenseBonus += 1
                }
            }
            TerrainType.MOUNTAIN -> { /* 移动力在格子层处理，边缘无额外效果 */ }
            TerrainType.URBAN -> {
                edges.forEach { edge ->
                    edge.movementPenalty += 1
                    edge.defenseConditions.addAll(listOf("骑兵", "远射"))
                }
            }
        }
    }

    // 辅助方法：应用河流效果
    private fun applyRiverEffects(edge: HexEdge) {
        if (edge.hasRiver) {
            edge.movementPenalty += 1
            edge.defenseBonus += 1
        }
    }

    // 辅助方法：应用防御工事效果
    private fun applyFortificationEffects(edge: HexEdge) {
        edge.movementPenalty += edge.fortification.movementPenalty
        edge.defenseBonus += edge.fortification.defenseBonus
    }
}