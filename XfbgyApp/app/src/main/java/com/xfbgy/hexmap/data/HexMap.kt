package com.xfbgy.hexmap.data

import kotlin.math.sqrt

/**
 * 六角格地图容器
 *
 * 采用偏移坐标（Offset Coordinates，odd-r）存储，
 * 内部运算时转换为轴向坐标（Axial Coordinates）
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

    /**
     * 边缘存储 [x][y][direction(0~5)]
     * direction: 0=上, 1=右上, 2=右下, 3=下, 4=左下, 5=左上
     */
    val edges: Array<Array<Array<HexEdge>>> = Array(width) { x ->
        Array(height) { y ->
            Array(6) { HexEdge() }
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
     * 获取相邻格坐标
     * @param x 当前格列坐标
     * @param y 当前格行坐标
     * @return 相邻6格的坐标列表（方向0~5）
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
     * 获取所有相邻格
     * @param x 列坐标
     * @param y 行坐标
     * @return 有效的相邻格列表
     */
    fun getNeighbors(x: Int, y: Int): List<HexCell> {
        return getNeighborCoords(x, y)
            .filter { (nx, ny) -> isValidCell(nx, ny) }
            .map { (nx, ny) -> cells[nx][ny] }
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
        return edges[x][y][direction]
    }

    /**
     * 获取两个相邻格共享的边
     * @param x1 格1列坐标
     * @param y1 格1行坐标
     * @param x2 格2列坐标
     * @param y2 格2行坐标
     * @return 共享边的方向（从格1看向格2），及边对象
     */
    fun getSharedEdge(x1: Int, y1: Int, x2: Int, y2: Int): Pair<Int, HexEdge>? {
        // 计算从(x1,y1)到(x2,y2)的方向
        val dx = x2 - x1
        val dy = y2 - y1

        // odd-r布局的方向判断
        val direction = when {
            dy == -1 && dx == 0 -> 0   // 上
            dy == -1 && dx == 1 -> 1   // 右上
            dy == 0 && dx == 1 -> 2    // 右下
            dy == 1 && dx == 0 -> 3    // 下
            dy == 0 && dx == -1 -> 4   // 左下
            dy == -1 && dx == -1 -> 5  // 左上
            else -> return null
        }

        val edge = getEdge(x1, y1, direction) ?: return null
        return Pair(direction, edge)
    }

    /**
     * 设置河流并同步共享边（别名：setEdgeRiver）
     * @param x 列坐标
     * @param y 行坐标
     * @param direction 方向(0~5)
     * @param hasRiver 是否有河流
     */
    fun setRiver(x: Int, y: Int, direction: Int, hasRiver: Boolean) {
        val edge = getEdge(x, y, direction) ?: return
        edge.hasRiver = hasRiver

        // 同步相邻格的共享边
        val neighbors = getNeighborCoords(x, y)
        if (direction in 0..5) {
            val (nx, ny) = neighbors[direction]
            if (isValidCell(nx, ny)) {
                // 对面方向 = (direction + 3) % 6
                val oppositeDir = (direction + 3) % 6
                edges[nx][ny][oppositeDir].hasRiver = hasRiver
            }
        }
    }

    /**
     * 设置河流并同步共享边
     * @param x 列坐标
     * @param y 行坐标
     * @param direction 方向(0~5)
     * @param hasRiver 是否有河流
     */
    fun setEdgeRiver(x: Int, y: Int, direction: Int, hasRiver: Boolean) {
        val edge = getEdge(x, y, direction) ?: return
        edge.hasRiver = hasRiver

        // 同步相邻格的共享边
        val neighbors = getNeighborCoords(x, y)
        if (direction in 0..5) {
            val (nx, ny) = neighbors[direction]
            if (isValidCell(nx, ny)) {
                // 对面方向 = (direction + 3) % 6
                val oppositeDir = (direction + 3) % 6
                edges[nx][ny][oppositeDir].hasRiver = hasRiver
            }
        }
    }

    /**
     * 设置防御工事（会同步到共享边）
     * @param x 列坐标
     * @param y 行坐标
     * @param direction 方向(0~5)
     * @param fortType 工事类型
     */
    fun setFortification(x: Int, y: Int, direction: Int, fortType: FortType) {
        val edge = getEdge(x, y, direction) ?: return
        edge.fortification = fortType

        // 同步相邻格的共享边
        val neighbors = getNeighborCoords(x, y)
        if (direction in 0..5) {
            val (nx, ny) = neighbors[direction]
            if (isValidCell(nx, ny)) {
                val oppositeDir = (direction + 3) % 6
                edges[nx][ny][oppositeDir].fortification = fortType
            }
        }
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
        edges.forEach { col ->
            col.forEach { cellEdges ->
                cellEdges.forEach { edge ->
                    edge.reset()
                }
            }
        }
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
                    val edge = edges[x][y][d]
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
         * 方向常量
         */
        const val DIR_UP = 0
        const val DIR_UPPER_RIGHT = 1
        const val DIR_LOWER_RIGHT = 2
        const val DIR_DOWN = 3
        const val DIR_LOWER_LEFT = 4
        const val DIR_UPPER_LEFT = 5

        /**
         * 方向名称映射
         */
        val DIR_NAMES = arrayOf("上", "右上", "右下", "下", "左下", "左上")

        /**
         * 反向方向
         */
        fun oppositeDirection(dir: Int): Int = (dir + 3) % 6
    }
}