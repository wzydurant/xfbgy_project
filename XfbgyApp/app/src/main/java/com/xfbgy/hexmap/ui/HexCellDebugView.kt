package com.xfbgy.hexmap.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import com.xfbgy.hexmap.data.FortType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 组件调试专用六角格View
 *
 * 绘制内容：
 * - 六边形填充色
 * - 6条边编号（1~6，顶边为1，顺时针）
 * - 河流：蓝色3dp线，在边内侧
 * - 防御工事：小叉连线，在河流更内侧
 *
 * 边编号规则：
 * - 1号边 = 顶边（方向0）
 * - 2号边 = 右上（方向1）
 * - 3号边 = 右下（方向2）
 * - 4号边 = 底边（方向3）
 * - 5号边 = 左下（方向4）
 * - 6号边 = 左上（方向5）
 */
class HexCellDebugView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 六角格外接圆半径
    var hexRadius: Float = 120f
        set(value) {
            field = value
            recalculateVertices()
            invalidate()
        }

    // 填充颜色
    var fillColor: Int = 0xFFA8D5A2.toInt()  // 默认平原色
        set(value) {
            field = value
            invalidate()
        }

    // 每条边的河流状态
    var edgeRivers: BooleanArray = BooleanArray(6) { false }

    // 每条边的防御工事类型
    var edgeForts: Array<FortType> = Array(6) { FortType.NONE }

    // 选中的边（-1表示无选中）
    var selectedEdge: Int = -1
        set(value) {
            field = value
            invalidate()
        }

    // 是否显示边编号
    var showEdgeNumbers: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    // 六角格顶点（以中心为原点）
    private val vertices = Array(6) { PointF() }

    // 绘制工具
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = 0xFF333333.toInt()
    }
    private val selectedEdgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = 0xFFFF9800.toInt()  // 橙色高亮选中边
    }
    private val riverPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(3f)
        color = HexMapColors.RIVER
        strokeCap = Paint.Cap.ROUND
    }
    private val fortPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        strokeCap = Paint.Cap.ROUND
    }
    private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 32f
        color = 0xFF333333.toInt()
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val numberBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0x80FFFFFF.toInt()
    }

    init {
        recalculateVertices()
    }

    private fun recalculateVertices() {
        for (i in 0 until 6) {
            val angle = Math.PI / 3 * i - Math.PI / 2  // 从顶部开始顺时针
            vertices[i] = PointF(
                (hexRadius * cos(angle)).toFloat(),
                (hexRadius * sin(angle)).toFloat()
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f

        // 构建六边形路径
        val hexPath = Path()
        hexPath.moveTo(cx + vertices[0].x, cy + vertices[0].y)
        for (i in 1 until 6) {
            hexPath.lineTo(cx + vertices[i].x, cy + vertices[i].y)
        }
        hexPath.close()

        // Layer 0: 填充
        fillPaint.color = fillColor
        canvas.drawPath(hexPath, fillPaint)

        // Layer 1: 边界线
        canvas.drawPath(hexPath, borderPaint)

        // Layer 2 & 3: 逐边绘制河流和工事
        for (dir in 0 until 6) {
            val v1 = vertices[dir]
            val v2 = vertices[(dir + 1) % 6]
            val x1 = cx + v1.x
            val y1 = cy + v1.y
            val x2 = cx + v2.x
            val y2 = cy + v2.y

            // 选中边高亮
            if (dir == selectedEdge) {
                canvas.drawLine(x1, y1, x2, y2, selectedEdgePaint)
            }

            // 河流 - 直接绘制在边上，邻居间无缝隙
            val hasRiver = edgeRivers[dir]
            val fort = edgeForts[dir]

            if (hasRiver) {
                canvas.drawLine(x1, y1, x2, y2, riverPaint)
            }

            // 防御工事 - 各格子独立，在河流内侧
            if (fort != FortType.NONE) {
                val fortOffset = if (hasRiver) {
                    // 有河流时，工事在河流内侧
                    dpToPx(3f / 2f + 2f)
                } else {
                    // 无河流时，工事紧贴边内侧
                    dpToPx(2f)
                }
                val inner = getInnerEdgePoints(x1, y1, x2, y2, fortOffset)
                fortPaint.color = getFortColor(fort)
                drawFortificationLines(canvas, inner[0], inner[1], inner[2], inner[3], fort)
            }
        }

        // Layer 4: 边编号标签
        if (showEdgeNumbers) {
            for (dir in 0 until 6) {
                val v1 = vertices[dir]
                val v2 = vertices[(dir + 1) % 6]
                // 边中点
                val midX = cx + (v1.x + v2.x) / 2
                val midY = cy + (v1.y + v2.y) / 2

                // 向外偏移一些放编号
                val offsetX = (midX - cx) * 0.25f
                val offsetY = (midY - cy) * 0.25f
                val labelX = midX + offsetX
                val labelY = midY + offsetY

                // 编号背景
                val text = "${dir + 1}"
                val textWidth = numberPaint.measureText(text)
                val bgRadius = textWidth / 2 + 6f
                canvas.drawCircle(labelX, labelY - numberPaint.textSize / 3, bgRadius, numberBgPaint)

                // 编号文字
                numberPaint.color = if (dir == selectedEdge) 0xFFFF5722.toInt() else 0xFF333333.toInt()
                canvas.drawText(text, labelX, labelY, numberPaint)
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
        val crossLen = len / 8
        val nx = -dy / len * crossLen
        val ny = dx / len * crossLen

        val numCross = when (fortType) {
            FortType.FENCE -> 3
            FortType.EARTHWALL -> 4
            FortType.STONEWALL -> 5
            FortType.NONE -> 0
        }

        for (i in 1..numCross) {
            val t = i.toFloat() / (numCross + 1)
            val mx = x1 + dx * t
            val my = y1 + dy * t
            canvas.drawLine(mx - nx, my - ny, mx + nx, my + ny, fortPaint)
        }
    }

    /**
     * 获取防御工事对应颜色
     */
    private fun getFortColor(fortType: FortType): Int {
        return when (fortType) {
            FortType.NONE -> 0
            FortType.FENCE -> HexMapColors.FENCE
            FortType.EARTHWALL -> HexMapColors.EARTHWALL
            FortType.STONEWALL -> HexMapColors.STONEWALL
        }
    }

    /**
     * 设置某条边的河流
     * @param edgeNum 边编号（1~6）
     */
    fun setEdgeRiver(edgeNum: Int, hasRiver: Boolean) {
        if (edgeNum in 1..6) {
            edgeRivers[edgeNum - 1] = hasRiver
            invalidate()
        }
    }

    /**
     * 设置某条边的防御工事
     * @param edgeNum 边编号（1~6）
     */
    fun setEdgeFort(edgeNum: Int, fortType: FortType) {
        if (edgeNum in 1..6) {
            edgeForts[edgeNum - 1] = fortType
            invalidate()
        }
    }

    /**
     * 重置所有边
     */
    fun resetAllEdges() {
        edgeRivers = BooleanArray(6) { false }
        edgeForts = Array(6) { FortType.NONE }
        selectedEdge = -1
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredSize = (hexRadius * 2.6f).toInt()
        val w = resolveSize(desiredSize, widthMeasureSpec)
        val h = resolveSize(desiredSize, heightMeasureSpec)
        val size = minOf(w, h)
        setMeasuredDimension(size, size)
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }
}
