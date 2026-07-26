package com.xfbgy.hexmap.ui

import android.graphics.PointF
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * HexUtils - 六角格坐标转换工具
 *
 * 设计方案（Phase 1-C 可视化层）：
 *
 * 提供像素坐标与格子坐标之间的双向转换功能
 *
 * 坐标系说明（开发方案2.1节）：
 * - 采用偏移坐标（Offset Coordinates，odd-r）存储地图数据
 * - 单数行左偏布局
 * - 原点(0,0)在最左下角
 *
 * 坐标转换公式：
 * - hexToPixel: 格子坐标 → 像素坐标
 * - pixelToHex: 像素坐标 → 格子坐标（反算）
 *
 * 顶点顺序（开发方案6.3节）：
 * - 0: 上
 * - 1: 右上
 * - 2: 右下
 * - 3: 下
 * - 4: 左下
 * - 5: 左上
 */
object HexUtils {

    /**
     * 边的方向常量（顺时针，从顶边开始）
     * 对应开发方案6.3节表格
     */
    const val DIR_TOP = 0
    const val DIR_TOP_RIGHT = 1
    const val DIR_BOTTOM_RIGHT = 2
    const val DIR_BOTTOM = 3
    const val DIR_BOTTOM_LEFT = 4
    const val DIR_TOP_LEFT = 5

    /**
     * 偏移坐标转像素坐标
     *
     * 使用 odd-r 布局（单数行左偏）：
     * - 单数行(y % 2 == 1)的格子左偏半个格子宽度
     *
     * @param x 格子列坐标
     * @param y 格子行坐标
     * @param radius 六角格外接圆半径（像素）
     * @return 格子中心点的像素坐标
     */
    fun hexToPixel(x: Int, y: Int, radius: Float): PointF {
        val colOffset = if (y % 2 == 1) {
            radius * sqrt(3f) / 2
        } else {
            0f
        }

        val px = x * radius * sqrt(3f) + colOffset
        val py = y * radius * 1.5f

        return PointF(px, py)
    }

    /**
     * 像素坐标转偏移坐标
     *
     * 实现思路：
     * 1. 估算所在列（除以格子宽度）
     * 2. 根据奇偶行修正
     * 3. 使用圆形吸附找到最近格中心
     *
     * @param px 像素X坐标
     * @param py 像素Y坐标
     * @param radius 六角格外接圆半径（像素）
     * @return Pair(列坐标, 行坐标)
     */
    fun pixelToHex(px: Float, py: Float, radius: Float): Pair<Int, Int> {
        // 估算行号
        val y = floor(py / (radius * 1.5f)).toInt()

        // 计算该行的列偏移（奇数行左偏）
        val colOffset = if (y % 2 == 1) radius * sqrt(3f) / 2 else 0f

        // 估算列号
        val x = floor((px - colOffset) / (radius * sqrt(3f))).toInt()

        // 精确计算：找到最近的格中心
        val center = hexToPixel(x, y, radius)
        var bestX = x
        var bestY = y
        var bestDist = Float.MAX_VALUE

        // 检查相邻格（最多检查3行3列的9个格）
        for (dy in -1..1) {
            for (dx in -1..1) {
                val checkX = x + dx
                val checkY = y + dy
                val checkCenter = hexToPixel(checkX, checkY, radius)
                val dist = (checkCenter.x - px) * (checkCenter.x - px) +
                        (checkCenter.y - py) * (checkCenter.y - py)

                if (dist < bestDist) {
                    bestDist = dist
                    bestX = checkX
                    bestY = checkY
                }
            }
        }

        return Pair(bestX, bestY)
    }

    /**
     * 获取指定方向的相邻格子偏移
     *
     * odd-r 布局的邻居偏移量（从当前格找6个邻居）：
     * - 方向0(上):      (-1, -1) 或 (0, -1) 取决于行奇偶
     * - 方向1(右上):   (0, -1) 或 (1, -1)
     * - 方向2(右下):   (0, 1) 或 (1, 1)
     * - 方向3(下):      (-1, 1) 或 (0, 1)
     * - 方向4(左下):   (-1, 0)
     * - 方向5(左上):    (-1, -1) 或 (0, -1)
     */
    fun getNeighborOffset(y: Int): Pair<Int, Int> {
        // odd-r 布局邻居查找表（简化为行列偏移）
        // 这里返回的是行列偏移量的近似值，实际使用时需根据具体实现调整
        return Pair(0, 0) // 占位，实际由 HexMap.getNeighbors() 提供
    }

    /**
     * 计算两点之间的距离（像素）
     */
    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * 计算六角格外顶点数组
     *
     * 顶点顺序（顺时针，从顶部开始）：
     * - 顶点0: 正上方
     * - 顶点1: 右上
     * - 顶点2: 右下
     * - 顶点3: 正下方
     * - 顶点4: 左下
     * - 顶点5: 左上
     *
     * @param centerX 中心X坐标
     * @param centerY 中心Y坐标
     * @param radius 外接圆半径
     * @return 6个顶点的坐标数组
     */
    fun getHexVertices(centerX: Float, centerY: Float, radius: Float): Array<PointF> {
        return Array(6) { i ->
            val angle = Math.PI / 3 * i - Math.PI / 2  // 从顶部开始
            PointF(
                (centerX + radius * cos(angle)).toFloat(),
                (centerY + radius * sin(angle)).toFloat()
            )
        }
    }

    /**
     * 获取边的中点坐标
     *
     * @param centerX 格子中心X
     * @param centerY 格子中心Y
     * @param radius 外接圆半径
     * @param direction 边方向(0-5)
     * @return 边中点坐标
     */
    fun getEdgeMidpoint(centerX: Float, centerY: Float, radius: Float, direction: Int): PointF {
        val vertices = getHexVertices(centerX, centerY, radius)
        val v1 = vertices[direction]
        val v2 = vertices[(direction + 1) % 6]

        return PointF(
            (v1.x + v2.x) / 2,
            (v1.y + v2.y) / 2
        )
    }

    /**
     * 屏幕坐标转地图坐标（考虑缩放和平移）
     *
     * @param screenX 屏幕X坐标
     * @param screenY 屏幕Y坐标
     * @param translateX 地图X偏移
     * @param translateY 地图Y偏移
     * @param scaleFactor 缩放比例
     * @param radius 六角格外接圆半径
     * @return Pair(列坐标, 行坐标)
     */
    fun screenToHex(
        screenX: Float,
        screenY: Float,
        translateX: Float,
        translateY: Float,
        scaleFactor: Float,
        radius: Float
    ): Pair<Int, Int> {
        val mapX = (screenX - translateX) / scaleFactor
        val mapY = (screenY - translateY) / scaleFactor
        return pixelToHex(mapX, mapY, radius)
    }
}