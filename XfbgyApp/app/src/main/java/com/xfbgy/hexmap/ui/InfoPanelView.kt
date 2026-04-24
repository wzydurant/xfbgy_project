package com.xfbgy.hexmap.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.FrameLayout
import com.xfbgy.hexmap.data.FortType
import com.xfbgy.hexmap.data.HexCell
import com.xfbgy.hexmap.data.HexEdge
import com.xfbgy.hexmap.data.TerrainType
import com.xfbgy.hexmap.data.Unit as GameUnit

/**
 * InfoPanelView - 地图信息面板自定义View
 *
 * 1. 面板布局设计：
 *    - 顶部：选中格子基本信息（坐标、地形）
 *    - 中部：移动力消耗、ZOC状态
 *    - 单位下拉框（点击切换选择）
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
 *    - 单位下拉框点击切换选择单位
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
    private var selectedUnitIndex: Int = 0  // 当前选中的单位索引

    // ==================== 公开属性 ====================
    val panelIsVisible: Boolean get() = isVisible

    // ==================== 布局参数 ====================
    private val panelWidth = 420f         // 面板宽度
    private val panelHeight = 550f         // 面板高度
    private val cornerRadius = 20f         // 圆角半径
    private val padding = 28f              // 内边距
    private val itemSpacing = 14f         // 条目间距
    private val buttonSize = 52f          // 方向按钮大小
    private val spinnerHeight = 60f        // 下拉框高度

    // ==================== 单位列表 ====================
    private var units: List<GameUnit> = emptyList()

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
        textSize = 22f
    }
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HexMapColors.PANEL_TEXT
        textSize = 26f
        typeface = Typeface.DEFAULT_BOLD
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HexMapColors.PANEL_TEXT
        textSize = 17f
        alpha = 180
    }
    private val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HexMapColors.PANEL_ACCENT
        textSize = 22f
    }
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val buttonTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HexMapColors.PANEL_TEXT
        textSize = 26f
        textAlign = Paint.Align.CENTER
    }
    private val arrowPath = Path()
    private val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // ==================== 下拉框区域 ====================
    private val spinnerRect = RectF()

    // ==================== 方向按钮位置 ====================
    private val directionButtons = Array(6) { RectF() }

    // 方向常量
    companion object {
        const val DIR_TOP = 0
        const val DIR_TOP_RIGHT = 1
        const val DIR_BOTTOM_RIGHT = 2
        const val DIR_BOTTOM = 3
        const val DIR_BOTTOM_LEFT = 4
        const val DIR_TOP_LEFT = 5

        private val DIR_NAMES = arrayOf("上", "右上", "右下", "下", "左下", "左上")
    }

    // ==================== 监听器 ====================
    private var onDirectionClickListener: ((Int) -> Unit)? = null
    private var onPanelDismissListener: (() -> Unit)? = null
    private var onUnitSelectedListener: ((GameUnit?) -> Unit)? = null

    // ==================== 公开接口 ====================

    /**
     * 显示格子信息面板
     */
    fun showCellInfo(cell: HexCell) {
        currentCell = cell
        showingEdgeInfo = false
        currentEdgeDir = -1
        currentEdge = null
        selectedUnitIndex = 0
        units = cell.units.toList()
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
        units = cell.units.toList()
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
        selectedUnitIndex = 0
        units = emptyList()
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
     * 设置单位选择监听
     */
    fun setOnUnitSelectedListener(listener: (GameUnit?) -> Unit) {
        onUnitSelectedListener = listener
    }

    /**
     * 获取当前选中的单位
     */
    fun getSelectedUnit(): GameUnit? {
        return units.getOrNull(selectedUnitIndex)
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
        val left = (width - panelWidth) / 2f
        val top = (height - panelHeight) / 2f
        return RectF(left, top, left + panelWidth, top + panelHeight)
    }

    /**
     * 绘制格子信息
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

        // 单位选择区域
        val unitLabel = "单位选择："
        canvas.drawText(unitLabel, rect.left + padding, currentY + labelPaint.textSize, labelPaint)
        currentY += labelPaint.textSize + 8f

        // 绘制Spinner背景
        val spinnerBgRect = RectF(
            rect.left + padding,
            currentY,
            rect.right - padding,
            currentY + spinnerHeight
        )
        spinnerRect.set(spinnerBgRect)
        buttonPaint.color = 0xFF444444.toInt()
        buttonPaint.alpha = 200
        canvas.drawRoundRect(spinnerBgRect, 8f, 8f, buttonPaint)
        
        // 绘制Spinner文字
        val selectedUnit = units.getOrNull(selectedUnitIndex)
        val spinnerText = if (selectedUnit != null) {
            "${selectedUnit.name} (攻:${selectedUnit.attack} 防:${selectedUnit.defense} 移:${selectedUnit.movement})"
        } else {
            "（无单位）"
        }
        textPaint.textSize = 18f
        canvas.drawText(spinnerText, rect.left + padding + 12f, currentY + spinnerHeight / 2 + textPaint.textSize / 3, textPaint)
        textPaint.textSize = 22f

        // 绘制下拉箭头
        val arrowX = rect.right - padding - 30f
        val arrowY = currentY + spinnerHeight / 2
        drawDropdownArrow(canvas, arrowX, arrowY)
        
        currentY += spinnerHeight + itemSpacing

        // 分隔线
        canvas.drawLine(
            rect.left + padding, currentY,
            rect.right - padding, currentY,
            Paint().apply { color = HexMapColors.PANEL_ACCENT; strokeWidth = 1f; alpha = 100 }
        )
        currentY += itemSpacing

        // 单位列表（只读显示）
        if (units.size > 1) {
            val listLabel = "单位列表（${units.size}个）："
            canvas.drawText(listLabel, rect.left + padding, currentY + labelPaint.textSize, labelPaint)
            currentY += labelPaint.textSize + 6f

            for (i in units.indices) {
                val unit = units[i]
                val unitText = "${i + 1}. ${unit.name} (攻:${unit.attack} 防:${unit.defense})"
                labelPaint.alpha = 160
                canvas.drawText(unitText, rect.left + padding + 12f, currentY + labelPaint.textSize, labelPaint)
                labelPaint.alpha = 180
                currentY += labelPaint.textSize + 4f
            }
        }

        // 方向按钮提示
        currentY = rect.bottom - padding - buttonSize - itemSpacing * 2
        val hintText = "点击方向按钮查看边缘属性"
        canvas.drawText(hintText, rect.left + padding, currentY, labelPaint)
        currentY += itemSpacing * 2

        // 绘制6个方向按钮
        drawDirectionButtons(canvas, rect, currentY)
    }

    /**
     * 绘制下拉箭头
     */
    private fun drawDropdownArrow(canvas: Canvas, x: Float, y: Float) {
        arrowPath.reset()
        val size = 12f
        arrowPath.moveTo(x - size / 2, y - size / 3)
        arrowPath.lineTo(x, y + size / 3)
        arrowPath.lineTo(x + size / 2, y - size / 3)
        arrowPaint.color = HexMapColors.PANEL_TEXT
        arrowPaint.style = Paint.Style.STROKE
        arrowPaint.strokeWidth = 2f
        canvas.drawPath(arrowPath, arrowPaint)
    }

    /**
     * 绘制边缘信息
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

        val dirSymbols = arrayOf("↑", "↗", "↘", "↓", "↙", "↖")

        for (i in 0 until 6) {
            val buttonX = rect.left + padding + i * (buttonSize + buttonSpacing)

            val isSelected = (i == currentEdgeDir)
            buttonPaint.color = if (isSelected) HexMapColors.PANEL_ACCENT else HexMapColors.PANEL_BG
            buttonPaint.alpha = if (isSelected) 255 else 150

            val buttonRect = RectF(buttonX, buttonY, buttonX + buttonSize, buttonY + buttonSize)
            directionButtons[i] = buttonRect

            canvas.drawRoundRect(buttonRect, 8f, 8f, buttonPaint)

            buttonTextPaint.color = HexMapColors.PANEL_TEXT
            buttonTextPaint.textSize = buttonSize * 0.5f
            buttonTextPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(
                dirSymbols[i],
                buttonRect.centerX(),
                buttonRect.centerY() + buttonTextPaint.textSize / 3,
                buttonTextPaint
            )
        }
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

    // ==================== 触摸事件处理 ====================
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isVisible) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 检查是否点击了下拉框
                if (!showingEdgeInfo && spinnerRect.contains(event.x, event.y)) {
                    // 循环选择下一个单位
                    if (units.isNotEmpty()) {
                        selectedUnitIndex = (selectedUnitIndex + 1) % units.size
                        onUnitSelectedListener?.invoke(units.getOrNull(selectedUnitIndex))
                        invalidate()
                    }
                    return true
                }

                // 检查是否点击了方向按钮
                val dir = getDirectionAt(event.x, event.y)
                if (dir != -1) {
                    if (showingEdgeInfo) {
                        showingEdgeInfo = false
                        currentEdgeDir = -1
                        currentEdge = null
                        invalidate()
                    }
                    onDirectionClickListener?.invoke(dir)
                    return true
                }

                // 如果在边缘信息模式，点击面板其他地方返回格子信息
                if (showingEdgeInfo) {
                    val panelRect = getPanelRect()
                    if (panelRect.contains(event.x, event.y)) {
                        showingEdgeInfo = false
                        currentEdgeDir = -1
                        currentEdge = null
                        invalidate()
                        return true
                    }
                }

                return true
            }
        }
        return super.onTouchEvent(event)
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

    private fun minOf(a: Int, b: Int): Int = if (a < b) a else b
}
