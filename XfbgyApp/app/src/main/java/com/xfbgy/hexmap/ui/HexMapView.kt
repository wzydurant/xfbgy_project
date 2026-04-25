package com.xfbgy.hexmap.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.xfbgy.hexmap.data.HexCell
import com.xfbgy.hexmap.data.HexEdge
import com.xfbgy.hexmap.data.HexMap
import com.xfbgy.hexmap.data.TerrainType
import com.xfbgy.hexmap.data.FortType
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * HexMapView - 六角格地图自定义View
 *
 * 设计方案（Phase 1-C 可视化层）：
 *
 * 1. 框架设计：
 *    - 继承 View，使用 Canvas 进行绘制
 *    - 支持双指缩放（ScaleGestureDetector）
 *    - 支持单指拖拽（GestureDetector）
 *    - 点击格子检测（像素坐标 → 格子坐标转换）
 *
 * 2. 绘制层级（从下到上）：
 *    - Layer 0: 地形填充（六边形实心）
 *    - Layer 1: 格子边界线（黑色细线）
 *    - Layer 2: 河流蓝线（蓝色粗线，绘制在边界线内侧）
 *    - Layer 3: 防御工事线（小叉连线，绘制在河流线内侧）
 *    - Layer 4: 坐标标签（调试模式）
 *
 * 3. 缩放/拖拽交互：
 *    - 缩放范围：0.5x ~ 3.0x
 *    - 拖拽边界限制：防止地图拖出屏幕过多
 *    - 使用 canvas.translate() 和 canvas.scale() 实现
 *
 * 4. 点击检测：
 *    - 使用 pixelToHex() 反算格子坐标
 *    - 点击位置与格中心距离吸附到最近格
 *    - 点击空白区域取消选中
 */
class HexMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ==================== 数据层引用 ====================
    private var hexMap: HexMap? = null  // 地图数据引用

    // ==================== 缩放与拖拽状态 ====================
    private var scaleFactor = 1.0f       // 当前缩放比例
    private var minScale = 0.5f         // 最小缩放
    private var maxScale = 3.0f         // 最大缩放
    private var translateX = 0f         // 拖拽X偏移
    private var translateY = 0f         // 拖拽Y偏移

    // ==================== 格子尺寸 ====================
    private var hexRadius = 60f         // 六角格外接圆半径（像素）
    private val hexHeight: Float get() = hexRadius * 2f
    private val hexWidth: Float get() = (sqrt(3f) * hexRadius).toFloat()
    private val halfHexWidth: Float get() = hexWidth / 2f

    // ==================== 选中状态 ====================
    private var selectedCell: HexCell? = null  // 当前选中的格子
    private var selectedEdgeDir: Int = -1      // 当前选中的边方向 (-1表示无)

    // ==================== 公开属性 ====================
    val currentSelectedCell: HexCell? get() = selectedCell

    // ==================== 调试模式 ====================
    private var debugMode = false       // 是否显示坐标标签

    // ==================== 监听器 ====================
    private var onCellClickListener: ((HexCell) -> Unit)? = null
    private var onCellEdgeClickListener: ((HexCell, Int) -> Unit)? = null

    // ==================== 绘制工具 ====================
    private val hexPath = Path()       // 六边形路径（复用）
    private val terrainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = HexMapColors.BORDER
    }
    private val riverPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = HexMapColors.RIVER
        strokeCap = Paint.Cap.ROUND
    }
    private val fortPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        strokeCap = Paint.Cap.ROUND
    }
    private val debugTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 24f
        color = HexMapColors.COORD_TEXT
        textAlign = Paint.Align.CENTER
    }
    private val selectedBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = HexMapColors.PANEL_ACCENT
    }

    // ==================== 触摸检测器 ====================
    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    private val gestureDetector = GestureDetector(context, GestureListener())

    // ==================== 六角格顶点缓存 ====================
    // 预计算六角格6个顶点的相对坐标（以中心为原点）
    // 顶点顺序：上、右上、右下、下、左下、左上（顺时针，从0开始）
    private val hexVertices = Array(6) { i ->
        val angle = Math.PI / 3 * i - Math.PI / 2
        PointF(
            (hexRadius * kotlin.math.cos(angle)).toFloat(),
            (hexRadius * kotlin.math.sin(angle)).toFloat()
        )
    }

    // ==================== 初始化 ====================
    init {
        // 启用硬件加速（可选优化）
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    /**
     * 设置地图数据
     * @param map HexMap实例
     */
    fun setMap(map: HexMap) {
        this.hexMap = map
        // 初始化视图居中
        centerMap()
        invalidate()
    }

    /**
     * 获取当前地图
     */
    fun getMap(): HexMap? = hexMap

    /**
     * 设置缩放范围
     */
    fun setScaleRange(min: Float, max: Float) {
        minScale = min
        maxScale = max
    }

    /**
     * 设置调试模式
     */
    fun setDebugMode(enabled: Boolean) {
        debugMode = enabled
        invalidate()
    }

    /**
     * 设置格子点击监听
     */
    fun setOnCellClickListener(listener: (HexCell) -> Unit) {
        onCellClickListener = listener
    }

    /**
     * 设置格子边缘点击监听
     */
    fun setOnCellEdgeClickListener(listener: (HexCell, Int) -> Unit) {
        onCellEdgeClickListener = listener
    }

    /**
     * 清除选中状态
     */
    fun clearSelection() {
        selectedCell = null
        selectedEdgeDir = -1
        invalidate()
    }

    /**
     * 选中指定格子
     */
    fun selectCell(cell: HexCell) {
        selectedCell = cell
        selectedEdgeDir = -1
        invalidate()
    }

    /**
     * 将地图居中显示
     */
    fun centerMap() {
        hexMap?.let { map ->
            // 计算地图总尺寸
            val mapPixelWidth = map.width * hexWidth + halfHexWidth
            val mapPixelHeight = map.height * hexHeight * 0.75f + hexRadius * 0.25f

            // 计算居中偏移
            translateX = (width - mapPixelWidth * scaleFactor) / 2f
            translateY = (height - mapPixelHeight * scaleFactor) / 2f
        }
    }

    // ==================== 主绘制流程 ====================
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        hexMap ?: return

        canvas.save()

        // 应用变换：先缩放后平移
        canvas.translate(translateX, translateY)
        canvas.scale(scaleFactor, scaleFactor)

        // 绘制所有格子
        drawAllCells(canvas)

        canvas.restore()
    }

    /**
     * 绘制所有格子
     * 优化：只绘制视口内可见的格子
     */
    private fun drawAllCells(canvas: Canvas) {
        val map = hexMap ?: return

        for (x in 0 until map.width) {
            for (y in 0 until map.height) {
                val cell = map.cells[x][y]

                // 可见性检测：简单检测格子中心是否在视口范围内（预留边距）
                if (isCellVisible(x, y)) {
                    drawCell(canvas, cell, x, y)
                }
            }
        }
    }

    /**
     * 检测格子是否可见（视口裁剪优化）
     */
    private fun isCellVisible(x: Int, y: Int): Boolean {
        val center = HexUtils.hexToPixel(x, y, hexRadius)
        val margin = hexRadius * 2  // 预留边距

        // 转换到屏幕坐标进行可见性判断
        val screenX = center.x * scaleFactor + translateX
        val screenY = center.y * scaleFactor + translateY

        return screenX > -margin && screenX < width + margin &&
                screenY > -margin && screenY < height + margin
    }

    /**
     * 绘制单个格子
     * 绘制顺序：填充 → 边界 → 河流 → 工事 → 选中高亮 → 坐标标签
     */
    private fun drawCell(canvas: Canvas, cell: HexCell, x: Int, y: Int) {
        val center = HexUtils.hexToPixel(x, y, hexRadius)

        // 构建六边形路径
        buildHexPath(center.x, center.y)

        // Layer 0: 地形填充
        terrainPaint.color = getTerrainColor(cell.terrain)
        canvas.drawPath(hexPath, terrainPaint)

        // Layer 1: 格子边界线
        canvas.drawPath(hexPath, borderPaint)

        // Layer 2 & 3: 绘制边缘属性（河流和防御工事）
        drawEdgeProperties(canvas, cell, center.x, center.y)

        // Layer 4: 选中高亮
        if (cell == selectedCell) {
            canvas.drawPath(hexPath, selectedBorderPaint)
        }

        // Layer 5: 坐标标签（调试模式）
        if (debugMode) {
            drawDebugLabel(canvas, cell, center.x, center.y)
        }
    }

    /**
     * 构建六边形Path（复用Path对象避免GC）
     */
    private fun buildHexPath(centerX: Float, centerY: Float) {
        hexPath.reset()
        hexPath.moveTo(centerX + hexVertices[0].x, centerY + hexVertices[0].y)
        for (i in 1 until 6) {
            hexPath.lineTo(centerX + hexVertices[i].x, centerY + hexVertices[i].y)
        }
        hexPath.close()
    }

    /**
     * 绘制边缘属性（河流蓝线、防御工事线）
     *
     * 绘制规则（开发方案6.2节）：
     * - 河流蓝色粗线（6px）绘制在边界线内侧
     * - 防御工事小叉连线绘制在河流线内侧
     * - 工事类型对应颜色：栅栏-褐色，土墙-灰色，石墙-白色
     */
    private fun drawEdgeProperties(canvas: Canvas, cell: HexCell, centerX: Float, centerY: Float) {
        for (dir in 0 until 6) {
            val edge = cell.edges[dir]

            // 计算边的两个顶点
            val vertex1 = hexVertices[dir]
            val vertex2 = hexVertices[(dir + 1) % 6]

            val x1 = centerX + vertex1.x
            val y1 = centerY + vertex1.y
            val x2 = centerX + vertex2.x
            val y2 = centerY + vertex2.y

            // Layer 2: 河流蓝线（偏移到边界线内侧）
            if (edge.hasRiver) {
                val riverOffset = 4f  // 向内偏移量
                val (rx1, ry1, rx2, ry2) = getInnerEdgePoints(x1, y1, x2, y2, riverOffset)
                canvas.drawLine(rx1, ry1, rx2, ry2, riverPaint)
            }

            // Layer 3: 防御工事线（小叉连线）
            if (edge.fortification != FortType.NONE) {
                val fortOffset = if (edge.hasRiver) 10f else 4f  // 有河流时更靠内侧
                val (fx1, fy1, fx2, fy2) = getInnerEdgePoints(x1, y1, x2, y2, fortOffset)

                fortPaint.color = getFortColor(edge.fortification)

                // 绘制小叉连线（中间短线 + 两端小斜线）
                drawFortificationLines(canvas, fx1, fy1, fx2, fy2, dir, edge.fortification)
            }
        }
    }

    /**
     * 计算向内偏移的边端点
     * 用于将河流线和工事线绘制在边界内侧
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
     *
     * 设计说明（开发方案2.3节）：
     * - 栅栏：褐色小叉连线
     * - 土墙：灰色小叉连线
     * - 石墙：白色小叉连线
     * - 小叉样式：在主线上绘制X形短线
     */
    private fun drawFortificationLines(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float,
                                        direction: Int, fortType: FortType) {
        val cx = (x1 + x2) / 2
        val cy = (y1 + y2) / 2

        // 主线长度
        val dx = x2 - x1
        val dy = y2 - y1
        val len = sqrt(dx * dx + dy * dy)

        // 小叉的间距和长度
        val crossSpacing = len / 5
        val crossLen = len / 8

        // 计算垂直方向
        val nx = -dy / len * crossLen
        val ny = dx / len * crossLen

        // 绘制主线上的小叉（3-5个）
        val numCross = when (fortType) {
            FortType.FENCE -> 3  // 栅栏少一些
            FortType.EARTHWALL -> 4
            FortType.STONEWALL -> 5  // 石墙多一些
            FortType.NONE -> 0
        }

        for (i in 1..numCross) {
            val t = i.toFloat() / (numCross + 1)
            val mx = x1 + dx * t
            val my = y1 + dy * t

            // 绘制小叉（X形）
            canvas.drawLine(mx - nx, my - ny, mx + nx, my + ny, fortPaint)
        }
    }

    /**
     * 绘制调试坐标标签
     */
    private fun drawDebugLabel(canvas: Canvas, cell: HexCell, centerX: Float, centerY: Float) {
        val text = "${cell.x},${cell.y}"

        // 绘制背景
        val textBounds = debugTextPaint.measureText(text)
        val bgRect = RectF(
            centerX - textBounds / 2 - 4,
            centerY - debugTextPaint.textSize / 2 - 2,
            centerX + textBounds / 2 + 4,
            centerY + debugTextPaint.textSize / 2 + 2
        )
        canvas.drawRect(bgRect, Paint().apply {
            color = HexMapColors.COORD_BG
            style = Paint.Style.FILL
        })

        // 绘制文字
        canvas.drawText(text, centerX, centerY + debugTextPaint.textSize / 3, debugTextPaint)
    }

    /**
     * 获取地形对应的填充颜色
     */
    private fun getTerrainColor(terrain: TerrainType): Int {
        return when (terrain) {
            TerrainType.PLAIN -> HexMapColors.PLAIN
            TerrainType.FOREST -> HexMapColors.FOREST
            TerrainType.HILL -> HexMapColors.HILL
            TerrainType.MOUNTAIN -> HexMapColors.MOUNTAIN
            TerrainType.URBAN -> HexMapColors.URBAN
        }
    }

    /**
     * 获取防御工事对应的颜色
     */
    private fun getFortColor(fortType: FortType): Int {
        return when (fortType) {
            FortType.NONE -> 0
            FortType.FENCE -> HexMapColors.FENCE
            FortType.EARTHWALL -> HexMapColors.EARTHWALL
            FortType.STONEWALL -> HexMapColors.STONEWALL
        }
    }

    // ==================== 触摸事件处理 ====================
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var handled = scaleGestureDetector.onTouchEvent(event)
        handled = gestureDetector.onTouchEvent(event) || handled
        return handled || super.onTouchEvent(event)
    }

    /**
     * 缩放手势监听器
     */
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            // 计算新的缩放比例
            val oldScale = scaleFactor
            scaleFactor *= detector.scaleFactor
            scaleFactor = max(minScale, min(scaleFactor, maxScale))

            // 围绕缩放中心点调整平移
            val focusX = detector.focusX
            val focusY = detector.focusY
            val scaleChange = scaleFactor / oldScale

            translateX = focusX - (focusX - translateX) * scaleChange
            translateY = focusY - (focusY - translateY) * scaleChange

            invalidate()
            return true
        }
    }

    /**
     * 手势监听器（拖拽 + 点击）
     */
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            // 单指拖拽移动地图
            translateX -= distanceX
            translateY -= distanceY

            // 拖拽边界限制（可选，防止拖出屏幕过多）
            clampTranslation()

            invalidate()
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            // 点击检测格子
            handleClick(e.x, e.y)
            return true
        }

        override fun onDown(e: MotionEvent): Boolean {
            return true  // 必须返回true才能接收后续事件
        }
    }

    /**
     * 处理点击事件
     * 像素坐标 → 格子坐标转换
     */
    private fun handleClick(screenX: Float, screenY: Float) {
        // 转换屏幕坐标到地图坐标
        val mapX = (screenX - translateX) / scaleFactor
        val mapY = (screenY - translateY) / scaleFactor

        // 反算格子坐标
        val hexCoord = HexUtils.pixelToHex(mapX, mapY, hexRadius)

        hexMap?.let { map ->
            // 检查坐标是否有效
            if (hexCoord.first in 0 until map.width && hexCoord.second in 0 until map.height) {
                val cell = map.cells[hexCoord.first][hexCoord.second]
                selectedCell = cell
                selectedEdgeDir = -1

                // 回调监听器
                onCellClickListener?.invoke(cell)

                invalidate()
            } else {
                // 点击空白区域，清除选中
                selectedCell = null
                selectedEdgeDir = -1
                invalidate()
            }
        }
    }

    /**
     * 限制拖拽范围
     */
    private fun clampTranslation() {
        hexMap?.let { map ->
            val mapPixelWidth = map.width * hexWidth + halfHexWidth
            val mapPixelHeight = map.height * hexHeight * 0.75f + hexRadius * 0.25f

            // 限制水平方向
            val minTx = width - mapPixelWidth * scaleFactor - 100
            val maxTx = 100f
            translateX = max(minTx, min(translateX, maxTx))

            // 限制垂直方向
            val minTy = height - mapPixelHeight * scaleFactor - 100
            val maxTy = 100f
            translateY = max(minTy, min(translateY, maxTy))
        }
    }

    // ==================== 尺寸测量 ====================
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 800  // 默认宽度
        val desiredHeight = 600  // 默认高度

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(desiredWidth, widthSize)
            else -> desiredWidth
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }
}