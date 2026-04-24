package com.xfbgy.hexmap

import android.graphics.PointF
import android.graphics.Point
import com.xfbgy.hexmap.data.HexCell
import com.xfbgy.hexmap.data.HexEdge
import kotlin.math.sqrt

/**
 * 兵棋手游地图模块
 *
 * Phase 1-A: 数据层
 * - 地形枚举、工事枚举
 * - 六角格数据类
 * - 地图容器及工具函数
 * - 属性计算与ZOC接口
 */

/**
 * 六角格地图容器
 *
 * @param width 地图宽度 (20~40)
 * @param height 地图高度 (20~40)
 * @throws IllegalArgumentException 如果尺寸超出有效范围
 */
class HexMap(val width: Int, val height: Int) {

    init {
        require(width in 20..40) { "地图宽度必须在20~40之间" }
        require(height in 20..40) { "地图高度必须在20~40之间" }
    }

    /** 格子数组 [x][y] */
    val cells: Array<Array<HexCell>> = Array(width) { x ->
        Array(height) { y ->
            HexCell(x, y, com.xfbgy.hexmap.data.TerrainType.PLAIN)
        }
    }

    /** 边缘数组 [x][y][direction(0~5)] */
    val edges: Array<Array<Array<HexEdge>>> = Array(width) { x ->
        Array(height) { y ->
            Array(6) { HexEdge() }
        }
    }

    /**
     * 获取指定坐标的格子
     * @return 如果坐标有效返回格子，否则返回null
     */
    fun getCell(x: Int, y: Int): HexCell? {
        return if (x in 0 until width && y in 0 until height) {
            cells[x][y]
        } else {
            null
        }
    }

    /**
     * 获取指定格子指定方向的边
     * @throws IllegalArgumentException 如果方向超出0~5范围
     */
    fun getEdge(x: Int, y: Int, direction: Int): HexEdge {
        require(direction in 0..5) { "方向必须在0~5之间" }
        return edges[x][y][direction]
    }

    /**
     * 设置指定格子指定方向的边是否有河流
     * 会同步更新相邻格子的对应边
     */
    fun setEdgeRiver(x: Int, y: Int, direction: Int, hasRiver: Boolean) {
        require(direction in 0..5) { "方向必须在0~5之间" }

        edges[x][y][direction].hasRiver = hasRiver

        // 同步相邻格子对应边
        val (neighborX, neighborY, neighborDir) = getNeighborEdgeInfo(x, y, direction)
        if (neighborX in 0 until width && neighborY in 0 until height) {
            edges[neighborX][neighborY][neighborDir].hasRiver = hasRiver
        }
    }

    /**
     * 获取相邻格子对应边的信息
     */
    private fun getNeighborEdgeInfo(x: Int, y: Int, direction: Int): Triple<Int, Int, Int> {
        // 方向0的上边相邻格子是y-1，对应边是方向3
        // 方向1的右上边相邻格子是...以此类推
        val neighborOffsets = mapOf(
            0 to Triple(x, y - 1, 3),      // 上 -> 下
            1 to Triple(x + 1, y, 4),      // 右上 -> 左下
            2 to Triple(x + 1, y + 1, 5),  // 右下 -> 左上
            3 to Triple(x, y + 1, 0),      // 下 -> 上
            4 to Triple(x - 1, y + 1, 1),  // 左下 -> 右上
            5 to Triple(x - 1, y, 2)       // 左上 -> 右下
        )
        return neighborOffsets[direction] ?: Triple(x, y, direction)
    }

    /**
     * 获取相邻格子列表
     * @return 最多6个相邻格子（边界处可能少于6个）
     */
    fun getNeighbors(x: Int, y: Int): List<HexCell> {
        val neighbors = mutableListOf<HexCell>()

        // odd-r 坐标系的邻居偏移
        val isOddRow = y % 2 == 1
        val offsets = if (isOddRow) {
            // 单数行左偏
            arrayOf(
                arrayOf(0, -1),   // 上
                arrayOf(1, -1),   // 右上
                arrayOf(1, 0),    // 右下
                arrayOf(0, 1),    // 下
                arrayOf(-1, 0),   // 左下
                arrayOf(-1, -1)   // 左上
            )
        } else {
            // 偶数行不偏移
            arrayOf(
                arrayOf(0, -1),   // 上
                arrayOf(1, 0),    // 右上
                arrayOf(1, 1),    // 右下
                arrayOf(0, 1),    // 下
                arrayOf(-1, 1),   // 左下
                arrayOf(-1, 0)    // 左上
            )
        }

        for (offset in offsets) {
            val nx = x + offset[0]
            val ny = y + offset[1]
            val cell = getCell(nx, ny)
            if (cell != null) {
                neighbors.add(cell)
            }
        }

        return neighbors
    }

    /**
     * 根据像素坐标获取格子
     * @param pixelX 像素X坐标
     * @param pixelY 像素Y坐标
     * @param radius 外接圆半径R
     * @return 对应的格子，如果超出地图范围返回null
     */
    fun getCellAtPixel(pixelX: Float, pixelY: Float, radius: Float): HexCell? {
        val coord = pixelToHex(pixelX, pixelY, radius)
        return getCell(coord.x, coord.y)
    }
}

/**
 * 偏移坐标转像素坐标
 * 依据 odd-r 单数行左偏规则
 *
 * @param x 偏移坐标X
 * @param y 偏移坐标Y
 * @param R 外接圆半径（像素）
 * @return 像素坐标 PointF
 */
fun hexToPixel(x: Int, y: Int, R: Float): PointF {
    val colOffset = if (y % 2 == 1) R * sqrt(3f) / 2 else 0f  // 单数行左偏
    val px = x * R * sqrt(3f) + colOffset
    val py = y * R * 1.5f
    return PointF(px, py)
}

/**
 * 像素坐标转偏移坐标
 *
 * @param px 像素X坐标
 * @param py 像素Y坐标
 * @param R 外接圆半径（像素）
 * @return 偏移坐标 Point
 */
fun pixelToHex(px: Float, py: Float, R: Float): Point {
    // 逆向计算（简化版本）
    val y = (py / (R * 1.5f)).toInt()
    val x = ((px - y * R * sqrt(3f) / 2) / (R * sqrt(3f))).toInt()
    return Point(x.coerceIn(0, 39), y.coerceIn(0, 39))
}

/**
 * 获取方向对应的两个顶点
 *
 * @param direction 方向编号 0~5
 * @return Pair<顶点1编号, 顶点2编号>
 */
fun getDirectionVertices(direction: Int): Pair<Int, Int> {
    return when (direction) {
        0 -> Pair(0, 1)   // 上: 顶点0 → 顶点1
        1 -> Pair(1, 2)   // 右上: 顶点1 → 顶点2
        2 -> Pair(2, 3)   // 右下: 顶点2 → 顶点3
        3 -> Pair(3, 4)   // 下: 顶点3 → 顶点4
        4 -> Pair(4, 5)   // 左下: 顶点4 → 顶点5
        5 -> Pair(5, 0)   // 左上: 顶点5 → 顶点0
        else -> throw IllegalArgumentException("方向必须在0~5之间")
    }
}

/**
 * 获取指定行奇偶性的邻居偏移
 *
 * @param y 行坐标
 * @return 6个方向的邻居偏移数组
 */
fun getNeighborOffsets(y: Int): Array<Pair<Int, Int>> {
    val isOddRow = y % 2 == 1
    return if (isOddRow) {
        arrayOf(
            Pair(0, -1),   // 上
            Pair(1, -1),   // 右上
            Pair(1, 0),    // 右下
            Pair(0, 1),    // 下
            Pair(-1, 0),   // 左下
            Pair(-1, -1)   // 左上
        )
    } else {
        arrayOf(
            Pair(0, -1),   // 上
            Pair(1, 0),    // 右上
            Pair(1, 1),    // 右下
            Pair(0, 1),    // 下
            Pair(-1, 1),   // 左下
            Pair(-1, 0)    // 左上
        )
    }
}