package com.xfbgy.hexmap.data

import kotlin.math.sqrt

/**
 * 调试用六角格地图容器
 *
 * 与 HexMap 功能类似，但无 20~40 的大小限制，
 * 适用于组件调试页面的地图生成功能。
 *
 * 采用 odd-r 偏移坐标，邻居偏移经过精确计算。
 *
 * @property width 地图宽度（列数）
 * @property height 地图高度（行数）
 */
class DebugHexMap(
    val width: Int,
    val height: Int
) {
    val cells: Array<Array<HexCell>> = Array(width) { x ->
        Array(height) { y ->
            HexCell(x, y, TerrainType.PLAIN)
        }
    }

    val edges: Array<Array<Array<HexEdge>>> = Array(width) { x ->
        Array(height) { y ->
            Array(6) { HexEdge() }
        }
    }

    // 偶数行邻居偏移表
    private val evenRowOffsets = arrayOf(
        intArrayOf(0, -1),   // 0: 顶边
        intArrayOf(1, 0),    // 1: 右上
        intArrayOf(0, 1),    // 2: 右下
        intArrayOf(-1, 1),   // 3: 底边
        intArrayOf(-1, 0),   // 4: 左下
        intArrayOf(-1, -1)   // 5: 左上
    )

    // 奇数行邻居偏移表
    private val oddRowOffsets = arrayOf(
        intArrayOf(1, -1),   // 0: 顶边
        intArrayOf(1, 0),    // 1: 右上
        intArrayOf(1, 1),    // 2: 右下
        intArrayOf(0, 1),    // 3: 底边
        intArrayOf(-1, 0),   // 4: 左下
        intArrayOf(0, -1)    // 5: 左上
    )

    /**
     * 获取邻居坐标
     * @param x 列坐标
     * @param y 行坐标
     * @param direction 方向(0~5)
     * @return Pair(nx, ny) 邻居坐标
     */
    fun getNeighborCoord(x: Int, y: Int, direction: Int): Pair<Int, Int> {
        val offsets = if (y % 2 == 0) evenRowOffsets else oddRowOffsets
        val d = direction % 6
        return Pair(x + offsets[d][0], y + offsets[d][1])
    }

    /**
     * 获取所有6个方向的邻居坐标
     */
    fun getAllNeighborCoords(x: Int, y: Int): List<Pair<Int, Int>> {
        return (0..5).map { getNeighborCoord(x, y, it) }
    }

    /**
     * 检查坐标是否有效
     */
    fun isValidCell(x: Int, y: Int): Boolean {
        return x in 0 until width && y in 0 until height
    }

    /**
     * 获取格子
     */
    fun getCell(x: Int, y: Int): HexCell? {
        return if (isValidCell(x, y)) cells[x][y] else null
    }

    /**
     * 获取边
     */
    fun getEdge(x: Int, y: Int, direction: Int): HexEdge? {
        if (!isValidCell(x, y) || direction !in 0..5) return null
        return edges[x][y][direction]
    }

    /**
     * 设置河流（同步共享边）
     */
    fun setRiver(x: Int, y: Int, direction: Int, hasRiver: Boolean) {
        val edge = getEdge(x, y, direction) ?: return
        edge.hasRiver = hasRiver

        val (nx, ny) = getNeighborCoord(x, y, direction)
        if (isValidCell(nx, ny)) {
            val oppositeDir = (direction + 3) % 6
            edges[nx][ny][oppositeDir].hasRiver = hasRiver
        }
    }

    /**
     * 设置防御工事（各格子独立，不同步邻居）
     */
    fun setFortification(x: Int, y: Int, direction: Int, fortType: FortType) {
        val edge = getEdge(x, y, direction) ?: return
        edge.fortification = fortType
        // 工事各格子独立，不再同步到邻居
    }

    /**
     * 六角格中心点像素坐标（odd-r布局）
     */
    fun hexToPixel(x: Int, y: Int, R: Float): Pair<Float, Float> {
        val colOffset = if (y % 2 == 1) R * sqrt(3f) / 2 else 0f
        val px = x * R * sqrt(3f) + colOffset
        val py = y * R * 1.5f
        return Pair(px, py)
    }

    /**
     * 清除所有边属性
     */
    fun clearAllEdges() {
        edges.forEach { col ->
            col.forEach { cellEdges ->
                cellEdges.forEach { edge ->
                    edge.reset()
                }
            }
        }
    }

    /**
     * 重置所有格子为平原
     */
    fun resetAllTerrain() {
        cells.forEach { col ->
            col.forEach { cell ->
                cell.terrain = TerrainType.PLAIN
                cell.movementCost = 0
                cell.zoc = 0b000
            }
        }
    }

    companion object {
        const val DIR_TOP = 0
        const val DIR_UPPER_RIGHT = 1
        const val DIR_LOWER_RIGHT = 2
        const val DIR_BOTTOM = 3
        const val DIR_LOWER_LEFT = 4
        const val DIR_UPPER_LEFT = 5

        val DIR_NAMES = arrayOf("顶边", "右上", "右下", "底边", "左下", "左上")

        fun oppositeDirection(dir: Int): Int = (dir + 3) % 6
    }
}
