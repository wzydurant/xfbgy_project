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
import com.xfbgy.hexmap.data.ResourcePoint
import com.xfbgy.hexmap.game.TurnManager
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 游戏页面专用六角格地图View
 *
 * 与HexMapGridView的区别：
 * - 不绘制格子边框
 * - 不绘制敌我双方阵营预留单位UI
 * - 保留资源点图标显示
 * - 保留点击选中逻辑
 * - 支持自由拖动和缩放
 */
class GameMapGridView @JvmOverloads constructor(
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

    // 回合管理器（用于绘制占领标记）
    var turnManager: TurnManager? = null

    // ========== 缩放 & 平移 ==========
    private var scaleFactor = 1f
    private var panX = 0f
    private var panY = 0f
    private val minScale = 0.2f
    private val maxScale = 8f

    // 手势检测
    private val scaleDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector

    // ========== 画笔 ==========
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // 无边框 - 不创建borderPaint

    private val selectedFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0x40FF9800.toInt()  // 半透明橙色
    }

    private val selectedBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * resources.displayMetrics.density
        color = 0xFFFF9800.toInt()  // 橙色选中边框
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

    // ========== CellUI 画笔（仅资源点，无阵营UI） ==========
    private val resourceIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val resourceTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    // ========== 玩家占领标记画笔 ==========
    private val occupationFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        alpha = 50  // 半透明填充
    }

    private val occupationBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * resources.displayMetrics.density
    }

    private val occupationTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        color = 0xFFFFFFFF.toInt()
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
        canvas.translate(padding, padding + R * sqrt(3f) / 2)

        for (y in 0 until m.height) {
            for (x in 0 until m.width) {
                drawCell(canvas, m, x, y, R)
            }
        }

        canvas.restore()
    }

    /**
     * 绘制单个格子（无边框，无阵营UI）
     */
    private fun drawCell(canvas: Canvas, map: DebugHexMap, x: Int, y: Int, R: Float) {
        val (cx, cy) = map.hexToPixel(x, y, R)
        val isSelected = (x == selectedCellX && y == selectedCellY)

        // 计算顶点
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

        // Layer 0.5: 选中格子高亮（橙色半透明填充 + 橙色边框）
        if (isSelected) {
            canvas.drawPath(hexPath, selectedFillPaint)
            canvas.drawPath(hexPath, selectedBorderPaint)
        }

        // Layer 1: 不画边框（游戏页面无边框）

        // Layer 1.5: 格子内部UI（仅资源点，无阵营UI）
        drawCellUI(canvas, cell, cx, cy, R)

        // Layer 2 & 3: 逐边绘制河流和工事
        for (dir in 0 until 6) {
            val edge = map.edges[x][y][dir]
            val v1 = vertices[dir]
            val v2 = vertices[(dir + 1) % 6]

            // 河流
            if (edge.hasRiver) {
                canvas.drawLine(v1.x, v1.y, v2.x, v2.y, riverPaint)
            }

            // 防御工事
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

    /**
     * 绘制格子内部UI（资源点 + 玩家占领标记）
     */
    private fun drawCellUI(canvas: Canvas, cell: com.xfbgy.hexmap.data.HexCell, cx: Float, cy: Float, R: Float) {
        val rp = cell.resourcePoint
        val density = resources.displayMetrics.density

        // 绘制玩家占领标记（支持多个玩家共享）
        val tm = turnManager
        if (tm != null) {
            val owners = tm.getCellOwners(cell.x, cell.y)
            if (owners.isNotEmpty()) {
                drawOccupationMarkers(canvas, cx, cy, R, owners, density)
            }
        }

        // 绘制资源点图标
        if (rp != null) {
            val iconRadius = R * 0.22f
            // 有占领者时资源点偏上，无占领者时居中
            val iconY = if (tm != null && tm.getCellOwners(cell.x, cell.y).isNotEmpty()) {
                cy - R * 0.18f
            } else {
                cy
            }
            drawResourceIcon(canvas, cx, iconY, iconRadius, rp, density)
        }
    }

    /**
     * 绘制多个玩家占领标记
     * 1个玩家：居中偏下
     * 2个玩家：左右分布
     */
    private fun drawOccupationMarkers(
        canvas: Canvas,
        cx: Float, cy: Float,
        R: Float,
        owners: List<com.xfbgy.hexmap.game.Player>,
        density: Float
    ) {
        val markerRadius = R * 0.17f

        when (owners.size) {
            1 -> {
                // 单个玩家：居中偏下
                drawSingleMarker(canvas, cx, cy + R * 0.22f, markerRadius, owners[0], density)
            }
            2 -> {
                // 两个玩家：左右分布
                val leftX = cx - R * 0.22f
                val rightX = cx + R * 0.22f
                val markerY = cy + R * 0.22f
                drawSingleMarker(canvas, leftX, markerY, markerRadius, owners[0], density)
                drawSingleMarker(canvas, rightX, markerY, markerRadius, owners[1], density)
            }
            else -> {
                // 3个及以上：从左到右排列
                val spacing = R * 0.35f
                val startX = cx - (owners.size - 1) * spacing / 2f
                for ((index, player) in owners.withIndex()) {
                    drawSingleMarker(canvas, startX + index * spacing, cy + R * 0.22f, markerRadius, player, density)
                }
            }
        }
    }

    /**
     * 绘制单个玩家占领标记
     */
    private fun drawSingleMarker(
        canvas: Canvas,
        cx: Float, cy: Float,
        markerRadius: Float,
        player: com.xfbgy.hexmap.game.Player,
        density: Float
    ) {
        // 半透明填充圆
        val playerColor = parseColor(player.colorHex)
        occupationFillPaint.color = playerColor
        occupationFillPaint.alpha = 60
        canvas.drawCircle(cx, cy, markerRadius, occupationFillPaint)

        // 边框圆
        occupationBorderPaint.color = playerColor
        occupationBorderPaint.alpha = 200
        canvas.drawCircle(cx, cy, markerRadius, occupationBorderPaint)

        // 玩家编号文字
        val textSize = markerRadius * 1.0f
        occupationTextPaint.textSize = textSize
        occupationTextPaint.alpha = 230
        canvas.drawText("P${player.id}", cx, cy + textSize / 3, occupationTextPaint)
    }

    /**
     * 绘制资源点图标
     */
    private fun drawResourceIcon(
        canvas: Canvas,
        x: Float, y: Float,
        radius: Float,
        rp: ResourcePoint,
        density: Float
    ) {
        // 背景圆
        val bgColor = parseColor(rp.type.colorHex)
        resourceIconPaint.color = bgColor
        resourceIconPaint.alpha = 200
        canvas.drawCircle(x, y, radius, resourceIconPaint)

        // 边框
        resourceIconPaint.style = Paint.Style.STROKE
        resourceIconPaint.strokeWidth = 1f * density
        resourceIconPaint.color = 0xFFFFFFFF.toInt()
        resourceIconPaint.alpha = 180
        canvas.drawCircle(x, y, radius, resourceIconPaint)
        resourceIconPaint.style = Paint.Style.FILL
        resourceIconPaint.alpha = 255

        // 文字标签
        val textSize = radius * 1.1f
        resourceTextPaint.textSize = textSize
        resourceTextPaint.color = 0xFFFFFFFF.toInt()
        resourceTextPaint.alpha = if (rp.isSuppressed) 120 else 255

        canvas.drawText(rp.type.iconLabel, x, y + textSize / 3, resourceTextPaint)
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
