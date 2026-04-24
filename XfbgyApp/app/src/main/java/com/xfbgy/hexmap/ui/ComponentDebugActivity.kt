package com.xfbgy.hexmap.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.InputType
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xfbgy.hexmap.data.DebugHexMap
import com.xfbgy.hexmap.data.FortType
import com.xfbgy.hexmap.data.TerrainType
import com.xfbgy.hexmap.generation.DebugRiverGenerator
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * 组件调试Activity
 *
 * 展示和调试基础组件：
 * 1. HexCell六边形组件（带边编号1~6）
 * 2. 河流组件预览
 * 3. 防御工事组件预览（栅栏、土墙、石墙）
 * 4. 交互控制：设置颜色、添加/移除河流、添加/移除防御工事
 */
class ComponentDebugActivity : AppCompatActivity() {

    private lateinit var hexCellView: HexCellDebugView

    // 河流预览View
    private lateinit var riverPreviewView: RiverPreviewView

    // 工事预览Views
    private lateinit var fencePreviewView: FortPreviewView
    private lateinit var earthwallPreviewView: FortPreviewView
    private lateinit var stonewallPreviewView: FortPreviewView

    // 当前选中边（1~6）
    private var currentEdgeNum: Int = 1

    // 状态文本
    private lateinit var statusText: TextView
    private lateinit var edgeInfoText: TextView

    // 地图生成相关
    private lateinit var mapSizeInput: EditText
    private lateinit var mapGridView: HexMapGridView
    private lateinit var mapInfoText: TextView
    private var currentMap: DebugHexMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootView = createLayout()
        setContentView(rootView)

        updateEdgeInfo()
    }

    /**
     * 动态创建布局（纯代码，无XML）
     */
    private fun createLayout(): ScrollView {
        val scrollView = ScrollView(this).apply {
            setBackgroundColor(0xFF1A1A2E.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        // 标题
        val titleText = TextView(this).apply {
            text = "组件调试页面"
            textSize = 24f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dpToPx(16))
        }
        rootLayout.addView(titleText)

        // ========== Section 1: HexCell 基础组件 ==========
        rootLayout.addView(createSectionTitle("1. 基础组件 - HexCell"))
        rootLayout.addView(createSectionSubtitle("顶边为1号边，顺时针标记2~6号"))

        hexCellView = HexCellDebugView(this).apply {
            hexRadius = 130f
            fillColor = 0xFFA8D5A2.toInt()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(360)
            )
        }
        rootLayout.addView(hexCellView)

        // 边信息文本
        edgeInfoText = TextView(this).apply {
            textSize = 14f
            setTextColor(0xFFB0FFFFFF.toInt())
            gravity = Gravity.CENTER
            setPadding(0, dpToPx(4), 0, dpToPx(8))
        }
        rootLayout.addView(edgeInfoText)

        // ========== Section 2: 河流组件 ==========
        rootLayout.addView(createSectionTitle("2. 基础组件 - 河流"))
        rootLayout.addView(createSectionSubtitle("3dp宽度，覆盖HexCell边，邻居间无缝隙"))

        riverPreviewView = RiverPreviewView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(80)
            )
        }
        rootLayout.addView(riverPreviewView)

        // ========== Section 3: 防御工事组件 ==========
        rootLayout.addView(createSectionTitle("3. 基础组件 - 防御工事"))
        rootLayout.addView(createSectionSubtitle("栅栏(褐色)、土墙(灰色)、石墙(白色)"))

        val fortPreviewLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        fencePreviewView = FortPreviewView(this, FortType.FENCE).apply {
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(120), 1f)
        }
        fortPreviewLayout.addView(fencePreviewView)

        earthwallPreviewView = FortPreviewView(this, FortType.EARTHWALL).apply {
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(120), 1f)
        }
        fortPreviewLayout.addView(earthwallPreviewView)

        stonewallPreviewView = FortPreviewView(this, FortType.STONEWALL).apply {
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(120), 1f)
        }
        fortPreviewLayout.addView(stonewallPreviewView)

        rootLayout.addView(fortPreviewLayout)

        // ========== Section 4: 交互控制 ==========
        rootLayout.addView(createSectionTitle("4. 交互控制"))

        // 当前状态
        statusText = TextView(this).apply {
            textSize = 15f
            setTextColor(0xFF1E90FF.toInt())
            gravity = Gravity.CENTER
            setPadding(0, dpToPx(4), 0, dpToPx(12))
            text = "当前选中：1号边"
        }
        rootLayout.addView(statusText)

        // 边选择按钮
        rootLayout.addView(createSectionSubtitle("选择边（1~6号）"))
        val edgeButtonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        for (i in 1..6) {
            val btn = Button(this).apply {
                text = "$i"
                textSize = 16f
                setTextColor(Color.WHITE)
                setBackgroundColor(0xFF37474F.toInt())
                layoutParams = LinearLayout.LayoutParams(0, dpToPx(48), 1f).apply {
                    marginStart = dpToPx(2)
                    marginEnd = dpToPx(2)
                }
                setOnClickListener {
                    currentEdgeNum = i
                    hexCellView.selectedEdge = i - 1
                    updateEdgeInfo()
                }
            }
            edgeButtonLayout.addView(btn)
        }
        rootLayout.addView(edgeButtonLayout)

        // 设置HexCell颜色
        rootLayout.addView(createSectionSubtitle("设置HexCell颜色"))
        val colorButtonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val terrainTypes = TerrainType.entries
        for (terrain in terrainTypes) {
            val btn = createColorButton(terrain.chineseName, Color.parseColor(terrain.colorHex)) {
                hexCellView.fillColor = Color.parseColor(terrain.colorHex)
            }
            colorButtonLayout.addView(btn)
        }
        rootLayout.addView(colorButtonLayout)

        // 设置防御工事
        rootLayout.addView(createSectionSubtitle("设置选中边的防御工事"))
        val fortButtonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val fortTypes = FortType.entries
        for (fort in fortTypes) {
            val colorHex = when (fort) {
                FortType.NONE -> "#37474F"
                FortType.FENCE -> "#8B5E3C"
                FortType.EARTHWALL -> "#757575"
                FortType.STONEWALL -> "#FFFFFF"
            }
            val btn = createColorButton(fort.chineseName, Color.parseColor(colorHex)) {
                hexCellView.setEdgeFort(currentEdgeNum, fort)
                updateEdgeInfo()
            }
            fortButtonLayout.addView(btn)
        }
        rootLayout.addView(fortButtonLayout)

        // 设置河流
        rootLayout.addView(createSectionSubtitle("设置选中边的河流"))
        val riverButtonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val addRiverBtn = createColorButton("添加河流", Color.parseColor("#1E90FF")) {
            hexCellView.setEdgeRiver(currentEdgeNum, true)
            updateEdgeInfo()
        }
        val removeRiverBtn = createColorButton("移除河流", 0xFF37474F.toInt()) {
            hexCellView.setEdgeRiver(currentEdgeNum, false)
            updateEdgeInfo()
        }
        riverButtonLayout.addView(addRiverBtn)
        riverButtonLayout.addView(removeRiverBtn)
        rootLayout.addView(riverButtonLayout)

        // 重置按钮
        rootLayout.addView(createSectionSubtitle("重置"))
        val resetBtn = Button(this).apply {
            text = "重置所有边"
            textSize = 16f
            setTextColor(Color.WHITE)
            setBackgroundColor(0xFFD32F2F.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(48)
            ).apply {
                topMargin = dpToPx(8)
            }
            setOnClickListener {
                hexCellView.resetAllEdges()
                hexCellView.fillColor = 0xFFA8D5A2.toInt()
                currentEdgeNum = 1
                updateEdgeInfo()
            }
        }
        rootLayout.addView(resetBtn)

        // ========== Section 5: 地图生成 ==========
        rootLayout.addView(createSectionTitle("5. 地图生成"))
        rootLayout.addView(createSectionSubtitle("输入大小生成n×n六角格地图，随机地形/工事/河流"))

        // 输入行
        val inputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val inputLabel = TextView(this).apply {
            text = "地图大小："
            textSize = 15f
            setTextColor(Color.WHITE)
        }
        inputLayout.addView(inputLabel)

        mapSizeInput = EditText(this).apply {
            hint = "3~15"
            textSize = 15f
            inputType = InputType.TYPE_CLASS_NUMBER
            setTextColor(Color.WHITE)
            setHintTextColor(0xFF80FFFFFF.toInt())
            setBackgroundColor(0xFF37474F.toInt())
            setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
            layoutParams = LinearLayout.LayoutParams(dpToPx(80), dpToPx(44))
        }
        mapSizeInput.setText("5")
        inputLayout.addView(mapSizeInput)

        val sizeSuffix = TextView(this).apply {
            text = " × n"
            textSize = 15f
            setTextColor(0xFFB0FFFFFF.toInt())
        }
        inputLayout.addView(sizeSuffix)

        val generateBtn = Button(this).apply {
            text = "生成地图"
            textSize = 15f
            setTextColor(Color.WHITE)
            setBackgroundColor(0xFF1E90FF.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dpToPx(44)
            ).apply {
                marginStart = dpToPx(12)
            }
            setOnClickListener { generateMap() }
        }
        inputLayout.addView(generateBtn)

        rootLayout.addView(inputLayout)

        // 地图信息
        mapInfoText = TextView(this).apply {
            textSize = 13f
            setTextColor(0xFFB0FFFFFF.toInt())
            gravity = Gravity.CENTER
            setPadding(0, dpToPx(8), 0, dpToPx(4))
            text = "点击\"生成地图\"创建随机地图"
        }
        rootLayout.addView(mapInfoText)

        // 地图容器（可横向+纵向滚动）
        val mapScrollContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(0xFF0D1117.toInt())
        }

        val hScrollView = HorizontalScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            isFillViewport = true
        }

        val mapVScrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(500)
            )
        }

        mapGridView = HexMapGridView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        mapVScrollView.addView(mapGridView)
        hScrollView.addView(mapVScrollView)
        mapScrollContainer.addView(hScrollView)
        rootLayout.addView(mapScrollContainer)

        // 底部间距
        val spacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(32)
            )
        }
        rootLayout.addView(spacer)

        scrollView.addView(rootLayout)
        return scrollView
    }

    private fun updateEdgeInfo() {
        val edgeIdx = currentEdgeNum - 1
        val hasRiver = hexCellView.edgeRivers[edgeIdx]
        val fort = hexCellView.edgeForts[edgeIdx]

        statusText.text = "当前选中：${currentEdgeNum}号边"
        edgeInfoText.text = "${currentEdgeNum}号边 | 河流: ${if (hasRiver) "有" else "无"} | 工事: ${fort.chineseName}"

        val dirNames = arrayOf("上(顶)", "右上", "右下", "下(底)", "左下", "左上")
        val detailText = "边${currentEdgeNum} = ${dirNames[edgeIdx]}方向"
        edgeInfoText.append("\n$detailText")
    }

    private fun createSectionTitle(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 18f
            setTextColor(Color.WHITE)
            setPadding(0, dpToPx(16), 0, dpToPx(4))
        }
    }

    private fun createSectionSubtitle(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(0xFF80FFFFFF.toInt())
            setPadding(0, 0, 0, dpToPx(8))
        }
    }

    private fun createColorButton(text: String, bgColor: Int, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(Color.WHITE)
            setBackgroundColor(bgColor)
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(44), 1f).apply {
                marginStart = dpToPx(2)
                marginEnd = dpToPx(2)
            }
            setOnClickListener { onClick() }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    /**
     * 生成随机地图
     */
    private fun generateMap() {
        val sizeStr = mapSizeInput.text.toString()
        val size = sizeStr.toIntOrNull()

        if (size == null || size !in 3..15) {
            Toast.makeText(this, "请输入3~15之间的数字", Toast.LENGTH_SHORT).show()
            return
        }

        val random = Random.Default
        val map = DebugHexMap(size, size)

        // 1. 随机地形颜色
        val terrainTypes = TerrainType.entries
        for (x in 0 until size) {
            for (y in 0 until size) {
                map.cells[x][y].terrain = terrainTypes.random(random)
            }
        }

        // 2. 随机防御工事（各格子独立，每条边30%概率有工事）
        val fortTypes = FortType.entries.filter { it != FortType.NONE }
        for (x in 0 until size) {
            for (y in 0 until size) {
                for (dir in 0..5) {
                    // 工事各格子独立，不需要与邻居同步
                    if (random.nextFloat() < 0.3f) {
                        val fort = fortTypes.random(random)
                        map.setFortification(x, y, dir, fort)
                    }
                }
            }
        }

        // 3. 生成河流
        val riverGenerator = DebugRiverGenerator()
        riverGenerator.generate(map)

        // 4. 显示地图
        currentMap = map
        mapGridView.map = map

        // 根据地图大小调整hex半径
        val maxMapWidthPx = resources.displayMetrics.widthPixels - dpToPx(32)
        val desiredRadius = maxMapWidthPx / (size * sqrt(3f) + sqrt(3f) / 2)
        val clampedRadius = desiredRadius.coerceIn(
            15f * resources.displayMetrics.density,
            50f * resources.displayMetrics.density
        )
        mapGridView.hexRadius = clampedRadius

        // 统计信息
        var riverCount = 0
        var fortCount = 0
        for (x in 0 until size) {
            for (y in 0 until size) {
                for (dir in 0..5) {
                    if (map.edges[x][y][dir].hasRiver) riverCount++
                    if (map.edges[x][y][dir].fortification != FortType.NONE) fortCount++
                }
            }
        }
        // 河流边被两侧都计数了，除以2
        riverCount /= 2
        // 工事各格子独立，直接计数

        mapInfoText.text = "${size}×${size} 地图 | 河流边: $riverCount | 工事边: $fortCount"
    }

    /**
     * 河流预览View - 单独展示河流线条
     */
    class RiverPreviewView @JvmOverloads constructor(
        context: android.content.Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : View(context, attrs, defStyleAttr) {

        private val riverPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f * context.resources.displayMetrics.density  // 3dp
            color = HexMapColors.RIVER
            strokeCap = Paint.Cap.ROUND
        }

        private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = 0xFF666666.toInt()
        }

        private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 14f * context.resources.displayMetrics.density
            color = 0xFFB0FFFFFF.toInt()
            textAlign = Paint.Align.CENTER
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val margin = dpToPx(40f)
            val edgeLen = width - margin * 2
            val y = height / 2f

            // 参考边界线
            canvas.drawLine(margin, y, margin + edgeLen, y, borderPaint)

            // 河流（直接覆盖在边界线上，邻居间无缝隙）
            canvas.drawLine(margin, y, margin + edgeLen, y, riverPaint)

            // 标签
            canvas.drawText("边界线 + 河流（覆盖边上）", margin + edgeLen / 2, y - 16f, labelPaint)
            canvas.drawText("3dp蓝色线，邻居间无缝衔接", margin + edgeLen / 2, y + 28f, labelPaint)
        }

        private fun dpToPx(dp: Float): Float {
            return dp * resources.displayMetrics.density
        }
    }

    /**
     * 防御工事预览View - 展示单一类型工事
     */
    class FortPreviewView @JvmOverloads constructor(
        context: android.content.Context,
        private val fortType: FortType,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : View(context, attrs, defStyleAttr) {

        private val fortPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            strokeCap = Paint.Cap.ROUND
            color = when (fortType) {
                FortType.FENCE -> HexMapColors.FENCE
                FortType.EARTHWALL -> HexMapColors.EARTHWALL
                FortType.STONEWALL -> HexMapColors.STONEWALL
                FortType.NONE -> 0
            }
        }

        private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = 0xFF666666.toInt()
        }

        private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 13f * context.resources.displayMetrics.density
            color = 0xFFB0FFFFFF.toInt()
            textAlign = Paint.Align.CENTER
        }

        private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0xFF263238.toInt()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            // 背景
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            val margin = dpToPx(20f)
            val edgeLen = width - margin * 2
            val centerY = height / 2f - dpToPx(14f)

            // 参考边界线
            canvas.drawLine(margin, centerY, margin + edgeLen, centerY, borderPaint)

            // 工事（偏移到内侧）
            val fortOffset = 14f
            val x1 = margin
            val y1 = centerY + fortOffset
            val x2 = margin + edgeLen
            val y2 = centerY + fortOffset

            drawFortificationLines(canvas, x1, y1, x2, y2)

            // 标签
            canvas.drawText(fortType.chineseName, width / 2f, height - dpToPx(8f), labelPaint)
        }

        private fun drawFortificationLines(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float) {
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

        private fun dpToPx(dp: Float): Float {
            return dp * resources.displayMetrics.density
        }
    }
}
