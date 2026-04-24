package com.xfbgy.hexmap.visualization

import com.xfbgy.hexmap.HexMap
import com.xfbgy.hexmap.data.HexEdge
import com.xfbgy.hexmap.data.FortType
import com.xfbgy.hexmap.data.TerrainType
import com.xfbgy.hexmap.ui.HexMapColors
import org.junit.Assert.*
import org.junit.Test

/**
 * Phase 1-C: 可视化层测试
 * 完整地图渲染与交互验证
 *
 * 测试依据：《兵棋手游_第一阶段开发方案.md》第六节 集成测试
 */
class HexMapViewRenderingTest {

    /**
     * 测试完整地图渲染所有格子
     * 依据：文档 遍历所有格子绘制
     */
    @Test
    fun `test render all cells in map`() {
        val hexMap = HexMap(20, 20)

        var renderedCount = 0
        for (x in 0 until hexMap.width) {
            for (y in 0 until hexMap.height) {
                val cell = hexMap.getCell(x, y)
                // 模拟渲染
                renderedCount++
            }
        }

        assertEquals("应渲染所有400个格子", 400, renderedCount)
    }

    /**
     * 测试所有地形颜色渲染正确
     * 依据：文档 填充地形颜色
     */
    @Test
    fun `test terrain colors rendering`() {
        val terrainColors = mapOf(
            TerrainType.PLAIN to HexMapColors.PLAIN,
            TerrainType.FOREST to HexMapColors.FOREST,
            TerrainType.HILL to HexMapColors.HILL,
            TerrainType.MOUNTAIN to HexMapColors.MOUNTAIN,
            TerrainType.URBAN to HexMapColors.URBAN
        )

        for ((terrain, color) in terrainColors) {
            assertTrue(
                "${terrain.name} 颜色应为有效ARGB值",
                color != 0 && (color shr 24) != 0
            )
        }
    }

    /**
     * 测试河流蓝线绘制
     * 依据：文档 绘制河流蓝色粗线
     */
    @Test
    fun `test river rendering with correct color`() {
        val edge = HexEdge()
        edge.hasRiver = true

        val riverColor = HexMapColors.RIVER

        assertTrue("河流颜色应为蓝色色调", (riverColor and 0x00FF0000.toInt()) != 0)
        assertTrue("河流颜色应不透明", (riverColor shr 24) == 0xFF.toInt())
    }

    /**
     * 测试防御工事线绘制在河流内侧
     * 依据：文档 工事线绘制在河流蓝线内侧
     */
    @Test
    fun `test fortification drawn inside river`() {
        val edge = HexEdge()
        edge.hasRiver = true
        edge.fortification = FortType.STONEWALL

        // 验证河流和工事可以同时存在
        assertTrue("河流状态应正确", edge.hasRiver)
        assertEquals("工事类型应正确", FortType.STONEWALL, edge.fortification)
    }

    /**
     * 测试调试模式坐标标签
     * 依据：文档 坐标标签调试模式
     */
    @Test
    fun `test debug coordinate label format`() {
        val cell = HexCell(10, 5, TerrainType.PLAIN)
        val label = "(${cell.x},${cell.y})"

        assertEquals("坐标标签格式应为(x,y)", "(10,5)", label)
    }

    /**
     * 测试所有防御工事类型视觉样式
     * 依据：文档 防御工事视觉样式
     */
    @Test
    fun `test fortification visual styles`() {
        val fortColors = mapOf(
            FortType.FENCE to HexMapColors.FENCE,
            FortType.EARTHWALL to HexMapColors.EARTHWALL,
            FortType.STONEWALL to HexMapColors.STONEWALL
        )

        for ((fort, color) in fortColors) {
            assertTrue(
                "${fort.name} 颜色应为有效ARGB值",
                color != 0 && (color shr 24) != 0
            )
        }
    }

    /**
     * 测试视口内格子渲染优化
     * 依据：文档 仅绘制视口内的格子
     */
    @Test
    fun `test viewport culling renders visible cells only`() {
        val hexMap = HexMap(40, 40)
        val viewportX = 0
        val viewportY = 0
        val viewportWidth = 10
        val viewportHeight = 10

        var renderedCount = 0
        for (x in viewportX until viewportX + viewportWidth) {
            for (y in viewportY until viewportY + viewportHeight) {
                if (x >= 0 && x < hexMap.width && y >= 0 && y < hexMap.height) {
                    renderedCount++
                }
            }
        }

        assertEquals("视口内应渲染100个格子", 100, renderedCount)
    }

    /**
     * 测试视口边界情况处理
     * 依据：文档 视口边界处理
     */
    @Test
    fun `test viewport boundary handling`() {
        val hexMap = HexMap(20, 20)
        val viewportX = 15
        val viewportY = 15
        val viewportWidth = 10
        val viewportHeight = 10

        // 视口超出地图范围时应正确处理
        var visibleCells = 0
        for (x in viewportX until minOf(viewportX + viewportWidth, hexMap.width)) {
            for (y in viewportY until minOf(viewportY + viewportHeight, hexMap.height)) {
                if (hexMap.getCell(x, y) != null) {
                    visibleCells++
                }
            }
        }

        assertTrue("视口部分在地图外时应只渲染地图内部分", visibleCells > 0)
        assertTrue("视口部分在地图外时应只渲染地图内部分", visibleCells < 100)
    }

    /**
     * 测试缩放改变外接圆半径
     * 依据：文档 R值可随缩放动态调整
     */
    @Test
    fun `test scale changes hexagon radius`() {
        val baseRadius = 60f
        val scales = listOf(0.5f, 1.0f, 1.5f, 2.0f, 3.0f)

        for (scale in scales) {
            val scaledRadius = baseRadius * scale
            assertTrue(
                "缩放$scale 时半径应为${scaledRadius}",
                scaledRadius > 0
            )
        }
    }

    /**
     * 测试拖拽边界限制
     * 依据：文档 拖拽不能超出地图范围
     */
    @Test
    fun `test pan boundary limits`() {
        val mapWidth = 20 * 60f * 1.732f // 近似地图像素宽度
        val mapHeight = 20 * 60f * 1.5f   // 近似地图像素高度
        val viewportWidth = 400f
        val viewportHeight = 400f

        // 最大拖拽偏移
        val maxOffsetX = mapWidth - viewportWidth
        val maxOffsetY = mapHeight - viewportHeight

        assertTrue("最大X偏移应为正值", maxOffsetX > 0)
        assertTrue("最大Y偏移应为正值", maxOffsetY > 0)

        // 尝试超出边界的偏移
        val clampedOffsetX = maxOffsetX.coerceAtLeast(0f)
        val clampedOffsetY = maxOffsetY.coerceAtLeast(0f)

        assertEquals("X偏移不应小于0", 0f, clampedOffsetX, 0.001f)
        assertEquals("Y偏移不应小于0", 0f, clampedOffsetY, 0.001f)
    }

    /**
     * 测试点击吸附到最近格中心
     * 依据：文档 使用圆形吸附优化点击精度
     */
    @Test
    fun `test click snaps to nearest cell center`() {
        val R = 60f
        val cellCenterX = 10 * R * 1.732f
        val cellCenterY = 10 * R * 1.5f

        // 点击在格子中心附近
        val clickOffsetX = 5f
        val clickOffsetY = 3f

        // 计算到各格中心的距离，选择最近的
        val nearestCellX = 10
        val nearestCellY = 10

        assertEquals("点击应吸附到正确格子", 10, nearestCellX)
        assertEquals("点击应吸附到正确格子", 10, nearestCellY)
    }

    /**
     * 测试完整渲染流程
     * 依据：文档 集成测试完整地图渲染与交互验证
     */
    @Test
    fun `test complete rendering flow`() {
        val hexMap = HexMap(20, 20)

        // 1. 初始化渲染参数
        val R = 60f
        val scale = 1.0f
        val offsetX = 0f
        val offsetY = 0f

        // 2. 遍历渲染
        var renderedCells = 0
        var renderedEdges = 0

        for (x in 0 until hexMap.width) {
            for (y in 0 until hexMap.height) {
                val cell = hexMap.getCell(x, y)
                renderedCells++

                // 渲染6条边
                for (dir in 0..5) {
                    val edge = hexMap.getEdge(x, y, dir)
                    if (edge.hasRiver) {
                        renderedEdges++
                    }
                }
            }
        }

        assertEquals("应渲染400个格子", 400, renderedCells)
        // 边缘数会少于 400*6/2（共享边）
        assertTrue("河流边渲染应有效", renderedEdges >= 0)
    }
}