package com.xfbgy.hexmap.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import com.xfbgy.hexmap.data.FortType
import com.xfbgy.hexmap.data.HexCell
import com.xfbgy.hexmap.data.HexEdge
import com.xfbgy.hexmap.data.TerrainType

/**
 * InfoPanelView - 地图信息面板自定义View
 *
 * 设计方案（Phase 1-C 可视化层）：
 *
 * 1. 面板布局设计：
 *    - 顶部：选中格子基本信息（坐标、地形）
 *    - 中间：移动力消耗、ZOC状态
 *    - 底部：6个方向按钮（上、右上、右下、下、左下、左上）
 *
 * 2. 边缘信息展示：
 *    - 点击方向按钮后，切换到该边缘详情模式
 *    - 显示：河流状态、防御工事、移动破坏、防御优势、进攻优势、条件列表
 *
 * 3. 视觉设计：
 *    - 深灰色半透明背景
 *    - 圆角卡片样式
 *    - 白色文字，蓝色强调色
 *    - 方向按钮带有方向指示箭头
 *
 * 4. 交互设计：
 *    - 方向按钮可点击切换边缘信息
 *    - 点击面板外区域自动关闭
 *    - 支持滑动/展开动画（可选）
 */
class InfoPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ==================== 显示状态 ====================
    private var isVisible = false          // 面板是否显示
    private var showingEdgeInfo = false    // 是否显示边缘详情
    private var currentCell: HexCell? = null
    private var currentEdgeDir: Int = -1  // 当前选中的边方向
    private var currentEdge: HexEdge? = null

    // ==================== 公开属性 ====================
    val panelIsVisible: Boolean get() = isVisible

    // ==================== 布局参数 ====================
    private val panelWidth = 320f         // 面板宽度
    private val panelHeight = 400f        // 面板高度
    private val cornerRadius = 16f         // 圆角半径
    private val padding = 24f              // 内边距
    private val itemSpacing = 12f         // 条目间距
    private val buttonSize = 48f          // 方向按钮大小

    // ==================== 绘制工具 ====================
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HexMapColors.PANEL_BG
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HexMapColors.PANEL_ACCENT
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HexMapColors.PANEL_TEXT
        textSize = 18f
    }
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HexMapColors.PANEL_TEXT
        textSize = 22f
        typeface = Typeface.DEFAULT_BOLD
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HexMapColors.PANEL_TEXT
        textSize = 14f
        alpha = 180
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HexMapColors.PANEL_TEXT
        textSize = 18f
    }
    private val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HexMapColors.PANEL_ACCENT
        textSize = 18f
    }
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val buttonTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HexMapColors.PANEL_TEXT
        textSize = 20f
        textAlign = Paint.Align.CENTER
    }
    private val arrowPath = Path()        // 方向箭头Path

    // ==================== 方向按钮位置 ====================
    // 6个方向按钮的矩形区域（用于点击检测）
    private val directionButtons = Array(6) { RectF() }

    // 方向常量（与开发方案6.3节对应）
    companion object {
        const val DIR_TOP = 0          // 上
        const val DIR_TOP_RIGHT = 1   // 右上
        const val DIR_BOTTOM_RIGHT = 2 // 右下
        const val DIR_BOTTOM = 3       // 下
        const val DIR_BOTTOM_LEFT = 4  // 左下
        const val DIR_TOP_LEFT = 5     // 左上

        // 方向名称（中文）
        private val DIR_NAMES = arrayOf("上", "右上", "右下", "下", "左下", "左上")
    }

    // ==================== 监听器 ====================
    private var onDirectionClickListener: ((Int) -> Unit)? = null
    private var onPanelDismissListener: (() -> Unit)? = null

    // ==================== 公开接口 ====================

    /**
     * 显示格子信息面板
     */
    fun showCellInfo(cell: HexCell) {
        currentCell = cell
        showingEdgeInfo = false
        currentEdgeDir = -1
        currentEdge = null
        isVisible = true
        invalidate()
    }

    /**
     * 显示边缘信息面板
     */
    fun showEdgeInfo(cell: HexCell, direction: Int, edge: HexEdge) {
        currentCell = cell
        showingEdgeInfo = true
        currentEdgeDir = direction
        currentEdge = edge
        isVisible = true
        invalidate()
    }

    /**
     * 关闭面板
     */
    fun dismiss() {
        isVisible = false
        currentCell = null
        currentEdgeDir = -1
        currentEdge = null
        onPanelDismissListener?.invoke()
        invalidate()
    }

    /**
     * 设置方向按钮点击监听
     */
    fun setOnDirectionClickListener(listener: (Int) -> Unit) {
        onDirectionClickListener = listener
    }

    /**
     * 设置面板关闭监听
     */
    fun setOnPanelDismissListener(listener: () -> Unit) {
        onPanelDismissListener = listener
    }

    /**
     * 检查点击位置是否在面板内
     */
    fun isPointInPanel(x: Float, y: Float): Boolean {
        if (!isVisible) return false
        val panelRect = getPanelRect()
        return panelRect.contains(x, y)
    }

    /**
     * 检查点击位置是否在方向按钮内
     */
    fun getDirectionAt(x: Float, y: Float): Int {
        for (i in 0 until 6) {
            if (directionButtons[i].contains(x, y)) {
                return i
            }
        }
        return -1
    }

    // ==================== 绘制逻辑 ====================
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!isVisible || currentCell == null) return

        val panelRect = getPanelRect()

        // 绘制面板背景
        canvas.drawRoundRect(panelRect, cornerRadius, cornerRadius, bgPaint)
        canvas.drawRoundRect(panelRect, cornerRadius, cornerRadius, borderPaint)

        // 根据显示模式绘制不同内容
        if (showingEdgeInfo) {
            drawEdgeInfo(canvas, panelRect)
        } else {
            drawCellInfo(canvas, panelRect)
        }
    }

    /**
     * 计算面板区域
     */
    private fun getPanelRect(): RectF {
        // 默认居中显示
        val left = (width - panelWidth) / 2f
        val top = (height - panelHeight) / 2f
        return RectF(left, top, left + panelWidth, top + panelHeight)
    }

    /**
     * 绘制格子信息
     *
     * 布局：
     * - 标题：坐标 + 地形
     * - 分隔线
     * - 移动力消耗
     * - ZOC状态
     * - 分隔线
     * - 方向按钮（6个）
     */
    private fun drawCellInfo(canvas: Canvas, rect: RectF) {
        var currentY = rect.top + padding

        // 标题：坐标
        val titleText = "格子 (${currentCell!!.x}, ${currentCell!!.y})"
        canvas.drawText(titleText, rect.left + padding, currentY + titlePaint.textSize, titlePaint)
        currentY += titlePaint.textSize + itemSpacing

        // 地形信息
        val terrainText = "地形：${getTerrainName(currentCell!!.terrain)}"
        canvas.drawText(terrainText, rect.left + padding, currentY + textPaint.textSize, textPaint)
        currentY += textPaint.textSize + itemSpacing

        // 移动力消耗
        val moveCostText = "移动力消耗：${currentCell!!.movementCost}"
        canvas.drawText(moveCostText, rect.left + padding, currentY + textPaint.textSize, textPaint)
        currentY += textPaint.textSize + itemSpacing

        // ZOC状态
        val zocText = "ZOC：${getZOCStatus(currentCell!!)}"
        canvas.drawText(zocText, rect.left + padding, currentY + textPaint.textSize, textPaint)
        currentY += textPaint.textSize + itemSpacing

        // 分隔线
        currentY += itemSpacing
        canvas.drawLine(
            rect.left + padding, currentY,
            rect.right - padding, currentY,
            Paint().apply { color = HexMapColors.PANEL_ACCENT; strokeWidth = 1f; alpha = 100 }
        )
        currentY += itemSpacing

        // 提示文字
        val hintText = "点击方向按钮查看边缘属性"
        canvas.drawText(hintText, rect.left + padding, currentY + labelPaint.textSize, labelPaint)
        currentY += labelPaint.textSize + itemSpacing * 2

        // 绘制6个方向按钮
        drawDirectionButtons(canvas, rect, currentY)
    }

    /**
     * 绘制边缘信息
     *
     * 布局：
     * - 标题：方向名称
     * - 分隔线
     * - 河流状态
     * - 防御工事
     * - 移动破坏
     * - 防御优势
     * - 进攻优势
     * - 条件列表
     */
    private fun drawEdgeInfo(canvas: Canvas, rect: RectF) {
        val edge = currentEdge ?: return

        var currentY = rect.top + padding

        // 标题：方向
        val titleText = "边缘 - ${DIR_NAMES[currentEdgeDir]}"
        canvas.drawText(titleText, rect.left + padding, currentY + titlePaint.textSize, titlePaint)
        currentY += titlePaint.textSize + itemSpacing

        // 分隔线
        canvas.drawLine(
            rect.left + padding, currentY,
            rect.right - padding, currentY,
            Paint().apply { color = HexMapColors.PANEL_ACCENT; strokeWidth = 1f; alpha = 100 }
        )
        currentY += itemSpacing

        // 河流状态
        val riverText = "河流：${if (edge.hasRiver) "有" else "无"}"
        canvas.drawText(riverText, rect.left + padding, currentY + textPaint.textSize, textPaint)
        currentY += textPaint.textSize + itemSpacing

        // 防御工事
        val fortText = "防御工事：${getFortName(edge.fortification)}"
        canvas.drawText(fortText, rect.left + padding, currentY + textPaint.textSize, textPaint)
        currentY += textPaint.textSize + itemSpacing

        // 移动破坏
        val movePenaltyText = "移动破坏：${edge.movementPenalty}"
        canvas.drawText(movePenaltyText, rect.left + padding, currentY + textPaint.textSize, textPaint)
        currentY += textPaint.textSize + itemSpacing

        // 防御优势
        val defenseText = "防御优势：${edge.defenseBonus}"
        accentPaint.color = if (edge.defenseBonus > 0) HexMapColors.PLAIN else HexMapColors.PANEL_TEXT
        canvas.drawText(defenseText, rect.left + padding, currentY + textPaint.textSize, accentPaint)
        currentY += textPaint.textSize + itemSpacing

        // 进攻优势
        val attackText = "进攻优势：${edge.attackBonus}"
        accentPaint.color = if (edge.attackBonus > 0) HexMapColors.RIVER else HexMapColors.PANEL_TEXT
        canvas.drawText(attackText, rect.left + padding, currentY + textPaint.textSize, accentPaint)
        currentY += textPaint.textSize + itemSpacing

        // 条件防御优势
        if (edge.defenseConditions.isNotEmpty()) {
            val conditionsText = "条件防御：${edge.defenseConditions.joinToString(", ")}"
            accentPaint.color = HexMapColors.PLAIN
            canvas.drawText(conditionsText, rect.left + padding, currentY + labelPaint.textSize, labelPaint)
            currentY += labelPaint.textSize + itemSpacing
        }

        // 条件进攻优势
        if (edge.attackConditions.isNotEmpty()) {
            val conditionsText = "条件进攻：${edge.attackConditions.joinToString(", ")}"
            accentPaint.color = HexMapColors.RIVER
            canvas.drawText(conditionsText, rect.left + padding, currentY + labelPaint.textSize, labelPaint)
            currentY += labelPaint.textSize + itemSpacing
        }

        // 返回按钮提示
        currentY = rect.bottom - padding - labelPaint.textSize
        labelPaint.alpha = 150
        canvas.drawText("点击任意处返回格子信息", rect.left + padding, currentY, labelPaint)
        labelPaint.alpha = 180
    }

    /**
     * 绘制6个方向按钮
     */
    private fun drawDirectionButtons(canvas: Canvas, rect: RectF, startY: Float) {
        val buttonSpacing = (rect.width() - padding * 2 - buttonSize * 6) / 5
        val buttonY = startY

        for (i in 0 until 6) {
            val buttonX = rect.left + padding + i * (buttonSize + buttonSpacing)

            // 按钮背景
            val isSelected = (i == currentEdgeDir)
            buttonPaint.color = if (isSelected) HexMapColors.PANEL_ACCENT else HexMapColors.PANEL_BG
            buttonPaint.alpha = if (isSelected) 255 else 100

            val buttonRect = RectF(buttonX, buttonY, buttonX + buttonSize, buttonY + buttonSize)
            directionButtons[i] = buttonRect

            canvas.drawRoundRect(buttonRect, 8f, 8f, buttonPaint)

            // 绘制方向箭头
            drawDirectionArrow(canvas, buttonRect.centerX(), buttonRect.centerY(), i)
        }
    }

    /**
     * 绘制方向箭头
     */
    private fun drawDirectionArrow(canvas: Canvas, cx: Float, cy: Float, direction: Int) {
        arrowPath.reset()

        val arrowSize = buttonSize / 4

        when (direction) {
            DIR_TOP -> { // 上
                arrowPath.moveTo(cx, cy - arrowSize)
                arrowPath.lineTo(cx - arrowSize / 2, cy + arrowSize / 2)
                arrowPath.lineTo(cx + arrowSize / 2, cy + arrowSize / 2)
            }
            DIR_TOP_RIGHT -> { // 右上
                arrowPath.moveTo(cx + arrowSize * 0.7f, cy - arrowSize * 0.7f)
                arrowPath.lineTo(cx - arrowSize / 2, cy + arrowSize / 2)
                arrowPath.lineTo(cx - arrowSize / 2, cy - arrowSize / 2)
            }
            DIR_BOTTOM_RIGHT -> { // 右下
                arrowPath.moveTo(cx + arrowSize * 0.7f, cy + arrowSize * 0.7f)
                arrowPath.lineTo(cx - arrowSize / 2, cy - arrowSize / 2)
                arrowPath.lineTo(cx - arrowSize / 2, cy + arrowSize / 2)
            }
            DIR_BOTTOM -> { // 下
                arrowPath.moveTo(cx, cy + arrowSize)
                arrowPath.lineTo(cx - arrowSize / 2, cy - arrowSize / 2)
                arrowPath.lineTo(cx + arrowSize / 2, cy - arrowSize / 2)
            }
            DIR_BOTTOM_LEFT -> { // 左下
                arrowPath.moveTo(cx - arrowSize * 0.7f, cy + arrowSize * 0.7f)
                arrowPath.lineTo(cx + arrowSize / 2, cy - arrowSize / 2)
                arrowPath.lineTo(cx + arrowSize / 2, cy + arrowSize / 2)
            }
            DIR_TOP_LEFT -> { // 左上
                arrowPath.moveTo(cx - arrowSize * 0.7f, cy - arrowSize * 0.7f)
                arrowPath.lineTo(cx + arrowSize / 2, cy + arrowSize / 2)
                arrowPath.lineTo(cx + arrowSize / 2, cy - arrowSize / 2)
            }
        }

        arrowPath.close()

        buttonTextPaint.color = HexMapColors.PANEL_TEXT
        canvas.drawPath(arrowPath, buttonTextPaint)
    }

    /**
     * 获取地形中文名称
     */
    private fun getTerrainName(terrain: TerrainType): String {
        return when (terrain) {
            TerrainType.PLAIN -> "平原"
            TerrainType.FOREST -> "树林"
            TerrainType.HILL -> "山地"
            TerrainType.MOUNTAIN -> "高山"
            TerrainType.URBAN -> "建筑群"
        }
    }

    /**
     * 获取防御工事中文名称
     */
    private fun getFortName(fortType: FortType): String {
        return when (fortType) {
            FortType.NONE -> "无"
            FortType.FENCE -> "栅栏"
            FortType.EARTHWALL -> "土墙"
            FortType.STONEWALL -> "石墙"
        }
    }

    /**
     * 获取ZOC状态描述
     */
    private fun getZOCStatus(cell: HexCell): String {
        val factions = mutableListOf<String>()
        for (i in 0..2) {
            if ((cell.zoc and (1 shl i)) != 0) {
                factions.add("阵营${i + 1}")
            }
        }
        return if (factions.isEmpty()) "无" else factions.joinToString(", ")
    }

    // ==================== 尺寸测量 ====================
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = panelWidth.toInt()
        val desiredHeight = panelHeight.toInt()

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> minOf(desiredWidth, widthSize)
            else -> desiredWidth
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> minOf(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }
}