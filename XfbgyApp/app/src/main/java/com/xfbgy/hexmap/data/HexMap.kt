package com.xfbgy.hexmap.data

import kotlin.math.sqrt

/**
 * 六角格地图容器
 *
 * 采用偏移坐标（Offset Coordinates，odd-r）存储，
 * 内部运算时转换为轴向坐标（Axial Coordinates）
 *
 * 边的编号（顺时针）：
 * - 0: 上 (顶部边)
 * - 1: 右上
 * - 2: 右下
 * - 3: 下 (底部边)
 * - 4: 左下
 * - 5: 左上
 *
 * @property width 地图宽度（列数）
 * @property height 地图高度（行数）
 */
class HexMap(
    val width: Int,
    val height: Int
) {
    /**
     * 格子数组 [x][y]
     */
    val cells: Array<Array<HexCell>> = Array(width) { x ->
        Array(height) { y ->
            HexCell(x, y, TerrainType.PLAIN)
        }
    }

    init {
        require(width in 20..40) { "地图宽度必须在20-40之间" }
        require(height in 20..40) { "地图高度必须在20-40之间" }
    }

    // ==================== 坐标工具函数 ====================

    /**
     * 偏移坐标转轴向坐标（odd-r）
     * @return Pair(q, r) 轴向坐标
     */
    fun offsetToAxial(x: Int, y: Int): Pair<Int, Int> {
        val q = x - (y - (y and 1)) / 2
        val r = y
        return Pair(q, r)
    }

    /**
     * 轴向坐标转偏移坐标（odd-r）
     * @return Pair(x, y) 偏移坐标
     */
    fun axialToOffset(q: Int, r: Int): Pair<Int, Int> {
        val x = q + (r - (r and 1)) / 2
        val y = r
        return Pair(x, y)
    }

    /**
     * 六角格中心点像素坐标（odd-r布局）
     * @param x 列坐标
     * @param y 行坐标
     * @param R 六角格外接圆半径（像素）
     * @return Pair(px, py) 像素坐标
     */
    fun hexToPixel(x: Int, y: Int, R: Float): Pair<Float, Float> {
        val colOffset = if (y % 2 == 1) R * sqrt(3f) / 2 else 0f  // 单数行左偏
        val px = x * R * sqrt(3f) + colOffset
        val py = y * R * 1.5f
        return Pair(px, py)
    }

    /**
     * 像素坐标转六角格偏移坐标
     * @param px 像素X坐标
     * @param py 像素Y坐标
     * @param R 六角格外接圆半径
     * @return Pair(x, y) 偏移坐标（可能为无效坐标）
     */
    fun pixelToHex(px: Float, py: Float, R: Float): Pair<Int, Int> {
        // 逆运算近似：先估算y，再估算x
        val y = (py / (R * 1.5f)).toInt()
        val xPrecise = px / (R * sqrt(3f)) - if (y % 2 == 1) 0.5f else 0f
        val x = (xPrecise + 0.5f).toInt()

        // 边界检查
        return if (isValidCell(x, y)) Pair(x, y) else Pair(-1, -1)
    }

    /**
     * 获取相邻格坐标（顺时针编号）
     * @param x 当前格列坐标
     * @param y 当前格行坐标
     * @return 相邻6格的坐标列表（方向0~5）
     *   0: 上邻格
     *   1: 右上邻格
     *   2: 右下邻格
     *   3: 下邻格
     *   4: 左下邻格
     *   5: 左上邻格
     */
    fun getNeighborCoords(x: Int, y: Int): List<Pair<Int, Int>> {
        // odd-r 布局的标准偏移（根据奇偶行有所不同）
        return if (y % 2 == 1) {
            // 单数行（右偏）
            listOf(
                Pair(x, y - 1),      // 0: 上
                Pair(x + 1, y - 1),  // 1: 右上
                Pair(x + 1, y),      // 2: 右下
                Pair(x, y + 1),      // 3: 下
                Pair(x - 1, y),      // 4: 左下
                Pair(x - 1, y - 1)   // 5: 左上
            )
        } else {
            // 双数行
            listOf(
                Pair(x, y - 1),      // 0: 上
                Pair(x - 1, y - 1),  // 1: 右上
                Pair(x + 1, y),      // 2: 右下
                Pair(x, y + 1),      // 3: 下
                Pair(x - 1, y),      // 4: 左下
                Pair(x - 1, y - 1)   // 5: 左上
            )
        }
    }

    /**
     * 获取两个相邻格之间的方向
     * 从 cell1 看向 cell2，返回 cell1 的哪个方向能到达 cell2
     * @param x1 格1列坐标
     * @param y1 格1行坐标
     * @param x2 格2列坐标
     * @param y2 格2行坐标
     * @return cell1 的方向编号(0~5)，如果不相邻则返回 null
     */
    fun getDirection(x1: Int, y1: Int, x2: Int, y2: Int): Int? {
        val neighbors = getNeighborCoords(x1, y1)
        for (dir in 0..5) {
            if (dir in neighbors.indices) {
                val (nx, ny) = neighbors[dir]
                if (nx == x2 && ny == y2) {
                    return dir
                }
            }
        }
        return null
    }

    /**
     * 获取指定格子某方向的边
     * @param x 列坐标
     * @param y 行坐标
     * @param direction 方向(0~5)
     * @return HexEdge 边缘对象
     */
    fun getEdge(x: Int, y: Int, direction: Int): HexEdge? {
        if (!isValidCell(x, y) || direction !in 0..5) return null
        return cells[x][y].edges[direction]
    }

    /**
     * 设置河流（同时设置两个相邻格子的对应边）
     * @param x 列坐标
     * @param y 行坐标
     * @param direction 方向(0~5)
     * @param hasRiver 是否有河流
     */
    fun setRiver(x: Int, y: Int, direction: Int, hasRiver: Boolean) {
        if (!isValidCell(x, y) || direction !in 0..5) return
        cells[x][y].setRiver(direction, hasRiver)

        // 同步相邻格的反向边
        val neighbors = getNeighborCoords(x, y)
        val (nx, ny) = neighbors[direction]
        if (isValidCell(nx, ny)) {
            val oppositeDir = (direction + 3) % 6
            cells[nx][ny].setRiver(oppositeDir, hasRiver)
        }
    }

    /**
     * 设置防御工事（同时设置两个相邻格子的对应边）
     * @param x 列坐标
     * @param y 行坐标
     * @param direction 方向(0~5)
     * @param fortType 工事类型
     */
    fun setFortification(x: Int, y: Int, direction: Int, fortType: FortType) {
        if (!isValidCell(x, y) || direction !in 0..5) return
        cells[x][y].setFortification(direction, fortType)

        // 同步相邻格的反向边
        val neighbors = getNeighborCoords(x, y)
        val (nx, ny) = neighbors[direction]
        if (isValidCell(nx, ny)) {
            val oppositeDir = (direction + 3) % 6
            cells[nx][ny].setFortification(oppositeDir, fortType)
        }
    }

    /**
     * 设置防御工事（仅设置当前格子，不同步到共享边）
     * @param x 列坐标
     * @param y 行坐标
     * @param direction 方向(0~5)
     * @param fortType 工事类型
     */
    fun setFortificationLocal(x: Int, y: Int, direction: Int, fortType: FortType) {
        if (!isValidCell(x, y) || direction !in 0..5) return
        cells[x][y].setFortification(direction, fortType)
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
     * 获取所有格子（扁平化）
     */
    fun getAllCells(): List<HexCell> {
        return cells.flatten()
    }

    /**
     * 清除所有单位
     */
    fun clearAllUnits() {
        cells.forEach { col ->
            col.forEach { cell ->
                cell.units.clear()
            }
        }
    }

    /**
     * 清除所有ZOC
     */
    fun clearAllZOC() {
        cells.forEach { col ->
            col.forEach { cell ->
                cell.clearAllZOC()
            }
        }
    }

    /**
     * 清除所有边属性（重置河流和工事）
     */
    fun clearAllEdges() {
        cells.forEach { col ->
            col.forEach { cell ->
                cell.resetEdges()
            }
        }
    }

    /**
     * 判断格子是否在地图边缘
     * @param x 列坐标
     * @param y 行坐标
     * @return 如果在边缘返回边缘边的方向列表，否则返回空列表
     */
    fun getEdgeDirections(x: Int, y: Int): List<Int> {
        val edgeDirs = mutableListOf<Int>()
        if (y == 0) edgeDirs.add(0)           // 上边在地图外
        if (x == width - 1) edgeDirs.add(2)   // 右边在地图外（奇偶行统一）
        if (y == height - 1) edgeDirs.add(3)  // 下边在地图外
        if (x == 0) edgeDirs.add(5)            // 左边在地图外（奇偶行统一）
        
        // 处理奇偶行导致的边缘边差异
        if (y % 2 == 1) {
            // 奇数行：右边更靠右
            if (x == width - 1) {
                // 右下边也在边缘
                if (!edgeDirs.contains(2)) edgeDirs.add(2)
            }
        } else {
            // 偶数行：左边更靠左
            if (x == 0) {
                // 左上边也在边缘
                if (!edgeDirs.contains(5)) edgeDirs.add(5)
            }
        }
        
        return edgeDirs.distinct()
    }

    /**
     * 判断某条边是否在地图边缘
     * @param x 列坐标
     * @param y 行坐标
     * @param direction 方向(0~5)
     * @return 是否在地图边缘
     */
    fun isEdgeOnMapBorder(x: Int, y: Int, direction: Int): Boolean {
        val borderDirs = getEdgeDirections(x, y)
        return direction in borderDirs
    }

    /**
     * 地图序列化（预留）
     */
    fun serialize(): String {
        val sb = StringBuilder()
        sb.appendLine("HexMap:$width,$height")
        for (y in 0 until height) {
            for (x in 0 until width) {
                val cell = cells[x][y]
                sb.appendLine("Cell:$x,$y,${cell.terrain.name},${cell.movementCost},${cell.zoc}")
            }
        }
        for (y in 0 until height) {
            for (x in 0 until width) {
                for (d in 0..5) {
                    val edge = cells[x][y].edges[d]
                    if (edge.hasRiver || edge.fortification != FortType.NONE) {
                        sb.appendLine("Edge:$x,$y,$d,${edge.hasRiver},${edge.fortification.name}")
                    }
                }
            }
        }
        return sb.toString()
    }

    companion object {
        /**
         * 方向常量（顺时针编号）
         */
        const val DIR_UP = 0           // 上
        const val DIR_UPPER_RIGHT = 1  // 右上
        const val DIR_LOWER_RIGHT = 2  // 右下
        const val DIR_DOWN = 3         // 下
        const val DIR_LOWER_LEFT = 4   // 左下
        const val DIR_UPPER_LEFT = 5   // 左上

        /**
         * 方向名称映射
         */
        val DIR_NAMES = arrayOf("上", "右上", "右下", "下", "左下", "左上")

        /**
         * 反向方向
         */
        fun oppositeDirection(dir: Int): Int = (dir + 3) % 6

        /**
         * 顺时针下一个方向
         */
        fun clockwiseDirection(dir: Int): Int = (dir + 1) % 6

        /**
         * 逆时针下一个方向
         */
        fun counterClockwiseDirection(dir: Int): Int = (dir + 5) % 6
    }
}
