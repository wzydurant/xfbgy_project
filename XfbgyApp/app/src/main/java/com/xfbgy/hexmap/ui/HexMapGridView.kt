package com.xfbgy.hexmap.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.xfbgy.hexmap.data.DebugHexMap
import com.xfbgy.hexmap.data.FortType
import com.xfbgy.hexmap.data.HexMapColors
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 六角格地图网格View（flat-top六边形）
 *
 * 绘制 n*n 的六角格地图，包含：
 * - 格子填充色（地形颜色）
 * - 边界线
 * - 河流（蓝色3dp线，在边内侧）
 * - 防御工事（小叉连线，在河流更内侧）
 * - 点击选中格子，高亮格子本身
 * - 双指缩放、拖拽平移
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
            selectedCellX = -1
            selectedCellY = -1
            resetView()
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

    // 选中格子坐标（-1表示无选中）
    var selectedCellX: Int = -1
        private set
    var selectedCellY: Int = -1
        private set

    // 格子选中回调
    var onCellSelected: ((x: Int, y: Int) -> Unit)? = null

    // ========== 缩放 & 平移 ==========
    private var scaleFactor = 1f
    private var panX = 0f
    private var panY = 0f
    private val minScale = 0.3f
    private val maxScale = 5f

    // 手势检测
    private val scaleDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector

    // ========== 画笔 ==========
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * resources.displayMetrics.density
        color = parseColor(HexMapColors.BORDER)
    }

    private val selectedFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0x40FF9800.toInt()  // 半透明橙色
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

    init {
        scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean = true

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val oldScale = scaleFactor
                val newScale = (scaleFactor * detector.scaleFactor).coerceIn(minScale, maxScale)
                val focusX = detector.focusX
                val focusY = detector.focusY
                panX = focusX - (focusX - panX) * (newScale / oldScale)
                panY = focusY - (focusY - panY) * (newScale / oldScale)
                scaleFactor = newScale
                invalidate()
                return true
            }
        })

        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                handleTap(e.x, e.y)
                return true
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                panX -= distanceX
                panY -= distanceY
                invalidate()
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean = true
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }

    fun resetView() {
        scaleFactor = 1f
        panX = 0f
        panY = 0f
    }

    fun centerMap() {
        val m = map ?: return
        val R = hexRadius
        // flat-top: 宽=(width-1)*1.5R + 2R, 高=height*√3R + √3R/2
        val contentW = (m.width - 1) * R * 1.5f + R * 2 + padding * 2
        val contentH = m.height * R * sqrt(3f) + R * sqrt(3f) / 2 + padding * 2

        if (width > 0 && height > 0) {
            panX = (width - contentW * scaleFactor) / 2f
            panY = (height - contentH * scaleFactor) / 2f
        }
        invalidate()
    }

    fun zoomIn() {
        val newScale = (scaleFactor * 1.3f).coerceIn(minScale, maxScale)
        val cx = width / 2f
        val cy = height / 2f
        panX = cx - (cx - panX) * (newScale / scaleFactor)
        panY = cy - (cy - panY) * (newScale / scaleFactor)
        scaleFactor = newScale
        invalidate()
    }

    fun zoomOut() {
        val newScale = (scaleFactor / 1.3f).coerceIn(minScale, maxScale)
        val cx = width / 2f
        val cy = height / 2f
        panX = cx - (cx - panX) * (newScale / scaleFactor)
        panY = cy - (cy - panY) * (newScale / scaleFactor)
        scaleFactor = newScale
        invalidate()
    }

    fun getZoomPercent(): Int = (scaleFactor * 100).toInt()

    // ========== 点击选中 ==========
    private fun handleTap(viewX: Float, viewY: Float) {
        val m = map ?: return
        // 视图坐标 → 地图本地坐标（与onDraw中的canvas变换对应）
        val localX = (viewX - panX) / scaleFactor - padding
        val localY = (viewY - panY) / scaleFactor - padding - hexRadius * sqrt(3f) / 2

        val cell = pixelToHex(localX, localY, m)
        if (cell != null) {
            selectedCellX = cell.first
            selectedCellY = cell.second
        } else {
            selectedCellX = -1
            selectedCellY = -1
        }
        onCellSelected?.invoke(selectedCellX, selectedCellY)
        invalidate()
    }

    /**
     * 像素坐标 → 六角格坐标（flat-top, odd-q布局）
     */
    private fun pixelToHex(px: Float, py: Float, map: DebugHexMap): Pair<Int, Int>? {
        val R = hexRadius
        val xEst = Math.round(px / (R * 1.5f))

        var bestDist = Float.MAX_VALUE
        var bestX = -1
        var bestY = -1

        for (dx in -1..1) {
            val x = xEst + dx
            if (x < 0 || x >= map.width) continue
            val rowOffset = if (x % 2 == 1) R * sqrt(3f) / 2 else 0f
            val yEst = Math.round((py - rowOffset) / (R * sqrt(3f)))

            for (dy in -1..1) {
                val y = yEst + dy
                if (y < 0 || y >= map.height) continue
                val (cx, cy) = map.hexToPixel(x, y, R)
                val dist = sqrt((px - cx) * (px - cx) + (py - cy) * (py - cy))
                if (dist < bestDist) {
                    bestDist = dist
                    bestX = x
                    bestY = y
                }
            }
        }

        val apothem = R * sqrt(3f) / 2
        if (bestDist <= apothem && bestX >= 0) {
            return Pair(bestX, bestY)
        }
        return null
    }

    // ========== 绘制 ==========
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(
            resolveSize(w, widthMeasureSpec),
            resolveSize(h, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val m = map ?: return
        val R = hexRadius

        canvas.save()
        canvas.translate(panX, panY)
        canvas.scale(scaleFactor, scaleFactor)
        // flat-top: 顶部边中点在中心上方 R*√3/2 处
        canvas.translate(padding, padding + R * sqrt(3f) / 2)

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
        val isSelected = (x == selectedCellX && y == selectedCellY)

        // 计算顶点（flat-top：起始角-120°，顺时针）
        val vertices = Array(6) { i ->
            val angle = Math.PI / 3.0 * i - Math.PI / 2.0 - Math.PI / 6.0
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

        // Layer 0.5: 选中格子半透明叠加（只高亮格子本身，不高亮边）
        if (isSelected) {
            canvas.drawPath(hexPath, selectedFillPaint)
        }

        // Layer 1: 边界线
        canvas.drawPath(hexPath, borderPaint)

        // Layer 2 & 3: 逐边绘制
        for (dir in 0 until 6) {
            val edge = map.edges[x][y][dir]
            val v1 = vertices[dir]
            val v2 = vertices[(dir + 1) % 6]

            // 河流 - 直接绘制在边上
            if (edge.hasRiver) {
                canvas.drawLine(v1.x, v1.y, v2.x, v2.y, riverPaint)
            }

            // 防御工事 - 各格子独立绘制
            if (edge.fortification != FortType.NONE) {
                val fortOffset = if (edge.hasRiver) {
                    (3f / 2f + 2f) * resources.displayMetrics.density
                } else {
                    2f * resources.displayMetrics.density
                }
                val inner = getInnerEdgePoints(v1.x, v1.y, v2.x, v2.y, fortOffset)
                fortPaint.color = parseColor(HexMapColors.getFortColor(edge.fortification))
                drawFortificationLines(canvas, inner[0], inner[1], inner[2], inner[3], edge.fortification)
            }
        }
    }

    private fun getInnerEdgePoints(x1: Float, y1: Float, x2: Float, y2: Float, offset: Float): FloatArray {
        val dx = x2 - x1
        val dy = y2 - y1
        val len = sqrt(dx * dx + dy * dy)
        if (len == 0f) return floatArrayOf(x1, y1, x2, y2)
        val nx = -dy / len * offset
        val ny = dx / len * offset
        return floatArrayOf(x1 + nx, y1 + ny, x2 + nx, y2 + ny)
    }

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
