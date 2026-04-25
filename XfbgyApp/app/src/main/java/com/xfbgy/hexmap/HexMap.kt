package com.xfbgy.hexmap

import android.graphics.PointF
import android.graphics.Point
import com.xfbgy.hexmap.data.HexCell
import com.xfbgy.hexmap.data.HexEdge
import com.xfbgy.hexmap.data.FortType
import kotlin.math.sqrt

/**
 * 兵棋手游地图模块（旧版兼容层）
 *
 * 建议使用 com.xfbgy.hexmap.data.HexMap 替代
 * 此文件保留兼容性，代理到新数据结构
 */

/**
 * 六角格地图容器
 *
 * @param width 地图宽度 (20~40)
 * @param height 地图高度 (20~40)
 */
class HexMap(val width: Int, val height: Int) {

    init {
        require(width in 20..40) { "地图宽度必须在20~40之间" }
        require(height in 20..40) { "地图高度必须在20~40之间" }
    }

    /** 格子数组 [x][y]，每个格子包含自己的6条边（cell.edges[0-5]） */
    val cells: Array<Array<HexCell>> = Array(width) { x ->
        Array(height) { y ->
            HexCell(x, y, com.xfbgy.hexmap.data.TerrainType.PLAIN)
        }
    }

    /**
     * 获取指定坐标的格子
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
     */
    fun getEdge(x: Int, y: Int, direction: Int): HexEdge {
        require(direction in 0..5) { "方向必须在0~5之间" }
        return cells[x][y].edges[direction]
    }

    /**
     * 设置指定格子指定方向的边是否有河流
     * 会同步更新相邻格子的对应边
     */
    fun setEdgeRiver(x: Int, y: Int, direction: Int, hasRiver: Boolean) {
        require(direction in 0..5) { "方向必须在0~5之间" }
        require(x in 0 until width && y in 0 until height) { "坐标超出范围" }

        cells[x][y].edges[direction].hasRiver = hasRiver

        // 同步相邻格子对应边
        val mapping = getNeighborEdgeInfo(x, y, direction)
        val (nx, ny, nd) = mapping
        if (nx in 0 until width && ny in 0 until height) {
            cells[nx][ny].edges[nd].hasRiver = hasRiver
        }
    }

    /**
     * 获取相邻格子对应边的信息
     */
    private fun getNeighborEdgeInfo(x: Int, y: Int, direction: Int): Triple<Int, Int, Int> {
        val neighborOffsets = getNeighborOffsetsCompat(y)
        val (dx, dy) = neighborOffsets[direction]
        val nx = x + dx
        val ny = y + dy
        val nd = (direction + 3) % 6
        return Triple(nx, ny, nd)
    }

    /**
     * 获取相邻格子列表
     */
    fun getNeighbors(x: Int, y: Int): List<HexCell> {
        val neighbors = mutableListOf<HexCell>()
        val offsets = getNeighborOffsetsCompat(y)

        for (offset in offsets) {
            val nx = x + offset.first
            val ny = y + offset.second
            val cell = getCell(nx, ny)
            if (cell != null) {
                neighbors.add(cell)
            }
        }
        return neighbors
    }

    /**
     * 兼容方法：获取邻居偏移
     */
    private fun getNeighborOffsetsCompat(y: Int): Array<Pair<Int, Int>> {
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

    fun getCellAtPixel(pixelX: Float, pixelY: Float, radius: Float): HexCell? {
        val coord = pixelToHex(pixelX, pixelY, radius)
        return getCell(coord.x, coord.y)
    }
}

fun hexToPixel(x: Int, y: Int, R: Float): PointF {
    val colOffset = if (y % 2 == 1) R * sqrt(3f) / 2 else 0f
    val px = x * R * sqrt(3f) + colOffset
    val py = y * R * 1.5f
    return PointF(px, py)
}

fun pixelToHex(px: Float, py: Float, R: Float): Point {
    val y = (py / (R * 1.5f)).toInt()
    val x = ((px - y * R * sqrt(3f) / 2) / (R * sqrt(3f))).toInt()
    return Point(x.coerceIn(0, 39), y.coerceIn(0, 39))
}

fun getDirectionVertices(direction: Int): Pair<Int, Int> {
    return when (direction) {
        0 -> Pair(0, 1)
        1 -> Pair(1, 2)
        2 -> Pair(2, 3)
        3 -> Pair(3, 4)
        4 -> Pair(4, 5)
        5 -> Pair(5, 0)
        else -> throw IllegalArgumentException("方向必须在0~5之间")
    }
}

fun getNeighborOffsets(y: Int): Array<Pair<Int, Int>> {
    val isOddRow = y % 2 == 1
    return if (isOddRow) {
        arrayOf(
            Pair(0, -1), Pair(1, -1), Pair(1, 0),
            Pair(0, 1), Pair(-1, 0), Pair(-1, -1)
        )
    } else {
        arrayOf(
            Pair(0, -1), Pair(1, 0), Pair(1, 1),
            Pair(0, 1), Pair(-1, 1), Pair(-1, 0)
        )
    }
}
