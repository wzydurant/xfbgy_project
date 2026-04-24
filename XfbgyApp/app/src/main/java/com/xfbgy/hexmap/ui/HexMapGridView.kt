package com.xfbgy.hexmap.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import com.xfbgy.hexmap.data.DebugHexMap
import com.xfbgy.hexmap.data.FortType
import com.xfbgy.hexmap.data.HexMapColors
import com.xfbgy.hexmap.data.TerrainType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 六角格地图网格View
 *
 * 绘制 n*n 的六角格地图，包含：
 * - 格子填充色（地形颜色）
 * - 边界线
 * - 河流（蓝色3dp线，在边内侧）
 * - 防御工事（小叉连线，在河流更内侧）
 */
class HexMapGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 地图数据
    var map: DebugHexMap? = null
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    // 六角格外接圆半径（像素）
    var hexRadius: Float = 40f * resources.displayMetrics.density
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    // 画笔
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * resources.displayMetrics.density
        color = parseColor(HexMapColors.BORDER)
    }

    private val riverPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f * resources.displayMetrics.density
        color = parseColor(HexMapColors.RIVER)
        strokeCap = Paint.Cap.ROUND
    }

    private val fortPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * resources.displayMetrics.density
        strokeCap = Paint.Cap.ROUND
    }

    // 内边距
    private val padding = 16f * resources.displayMetrics.density

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val m = map
        if (m == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        val R = hexRadius
        // 计算地图像素尺寸
        val mapWidth = m.width * R * sqrt(3f) + R * sqrt(3f) / 2 + padding * 2
        val mapHeight = (m.height - 1) * R * 1.5f + R * 2 + padding * 2

        setMeasuredDimension(
            resolveSize(mapWidth.toInt(), widthMeasureSpec),
            resolveSize(mapHeight.toInt(), heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val m = map ?: return
        val R = hexRadius

        canvas.save()
        canvas.translate(padding, padding + R)

        // 逐格子绘制
        for (y in 0 until m.height) {
            for (x in 0 until m.width) {
                drawCell(canvas, m, x, y, R)
            }
        }

        canvas.restore()
    }

    /**
     * 绘制单个格子
     */
    private fun drawCell(canvas: Canvas, map: DebugHexMap, x: Int, y: Int, R: Float) {
        val (cx, cy) = map.hexToPixel(x, y, R)

        // 计算顶点
        val vertices = Array(6) { i ->
            val angle = Math.PI / 3.0 * i - Math.PI / 2.0
            PointF(
                (cx + R * cos(angle)).toFloat(),
                (cy + R * sin(angle)).toFloat()
            )
        }

        // 构建六边形路径
        val hexPath = Path()
        hexPath.moveTo(vertices[0].x, vertices[0].y)
        for (i in 1 until 6) {
            hexPath.lineTo(vertices[i].x, vertices[i].y)
        }
        hexPath.close()

        // Layer 0: 填充地形颜色
        val cell = map.cells[x][y]
        fillPaint.color = parseColor(HexMapColors.getTerrainColor(cell.terrain))
        canvas.drawPath(hexPath, fillPaint)

        // Layer 1: 边界线
        canvas.drawPath(hexPath, borderPaint)

        // Layer 2 & 3: 逐边绘制河流和工事
        // 河流直接绘制在边上(offset=0)，邻居河流无缝衔接
        // 工事各格子独立，不要求与邻居对应
        for (dir in 0 until 6) {
            val edge = map.edges[x][y][dir]
            val v1 = vertices[dir]
            val v2 = vertices[(dir + 1) % 6]

            // 河流 - 直接绘制在边上，确保邻居间无缝隙
            if (edge.hasRiver) {
                canvas.drawLine(v1.x, v1.y, v2.x, v2.y, riverPaint)
            }

            // 防御工事 - 各格子独立绘制
            if (edge.fortification != FortType.NONE) {
                val fortOffset = if (edge.hasRiver) {
                    // 有河流时，工事在河流内侧（河流线宽3dp的一半 + 间距）
                    (3f / 2f + 2f) * resources.displayMetrics.density
                } else {
                    // 无河流时，工事紧贴边内侧
                    2f * resources.displayMetrics.density
                }
                val inner = getInnerEdgePoints(v1.x, v1.y, v2.x, v2.y, fortOffset)
                fortPaint.color = parseColor(HexMapColors.getFortColor(edge.fortification))
                drawFortificationLines(canvas, inner[0], inner[1], inner[2], inner[3], edge.fortification)
            }
        }
    }

    /**
     * 计算向内偏移的边端点
     */
    private fun getInnerEdgePoints(x1: Float, y1: Float, x2: Float, y2: Float, offset: Float): FloatArray {
        val dx = x2 - x1
        val dy = y2 - y1
        val len = sqrt(dx * dx + dy * dy)
        if (len == 0f) return floatArrayOf(x1, y1, x2, y2)
        val nx = -dy / len * offset
        val ny = dx / len * offset
        return floatArrayOf(x1 + nx, y1 + ny, x2 + nx, y2 + ny)
    }

    /**
     * 绘制防御工事小叉连线
     */
    private fun drawFortificationLines(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float, fortType: FortType) {
        val dx = x2 - x1
        val dy = y2 - y1
        val len = sqrt(dx * dx + dy * dy)
        if (len == 0f) return

        val crossLen = len / 8
        val nx = -dy / len * crossLen
        val ny = dx / len * crossLen

        val numCross = when (fortType) {
            FortType.FENCE -> 2
            FortType.EARTHWALL -> 3
            FortType.STONEWALL -> 4
            FortType.NONE -> 0
        }

        for (i in 1..numCross) {
            val t = i.toFloat() / (numCross + 1)
            val mx = x1 + dx * t
            val my = y1 + dy * t
            canvas.drawLine(mx - nx, my - ny, mx + nx, my + ny, fortPaint)
        }
    }

    private fun parseColor(colorHex: String): Int {
        return android.graphics.Color.parseColor(colorHex)
    }
}
