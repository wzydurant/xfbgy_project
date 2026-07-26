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
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xfbgy.hexmap.data.DebugHexMap
import com.xfbgy.hexmap.data.FortType
import com.xfbgy.hexmap.data.ResourcePointType
import com.xfbgy.hexmap.data.TerrainType
import com.xfbgy.hexmap.generation.DebugRiverGenerator
import com.xfbgy.hexmap.generation.ResourcePointScanner
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
    private lateinit var riverCountInput: EditText
    private lateinit var mapGridView: HexMapGridView
    private lateinit var mapInfoText: TextView
    private lateinit var cellInfoText: TextView
    private lateinit var zoomText: TextView
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

        // ========== Section 3.5: 格子内部UI预览 ==========
        rootLayout.addView(createSectionTitle("3.5 基础组件 - 格子内部UI"))
        rootLayout.addView(createSectionSubtitle("资源点(图标) + 预留单位空间(虚线圆)"))

        // 3种布局预览
        val cellUIPreviewLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // 1个部分：只有资源点
        val cellUI1 = HexCellDebugView(this).apply {
            hexRadius = 80f
            fillColor = 0xFF9E9E9E.toInt() // 建筑群色
            resourcePoint = com.xfbgy.hexmap.data.ResourcePoint(com.xfbgy.hexmap.data.ResourcePointType.VILLAGE)
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(240), 1f)
        }
        cellUIPreviewLayout.addView(cellUI1)

        // 2个部分：资源点 + 预留我方
        val cellUI2 = HexCellDebugView(this).apply {
            hexRadius = 80f
            fillColor = 0xFF9E9E9E.toInt()
            resourcePoint = com.xfbgy.hexmap.data.ResourcePoint(com.xfbgy.hexmap.data.ResourcePointType.TOWN)
            showFriendlyUnitSpace = true
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(240), 1f)
        }
        cellUIPreviewLayout.addView(cellUI2)

        // 3个部分：资源点 + 预留我方 + 预留敌方
        val cellUI3 = HexCellDebugView(this).apply {
            hexRadius = 80f
            fillColor = 0xFF9E9E9E.toInt()
            resourcePoint = com.xfbgy.hexmap.data.ResourcePoint(com.xfbgy.hexmap.data.ResourcePointType.CITY)
            showFriendlyUnitSpace = true
            showEnemyUnitSpace = true
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(240), 1f)
        }
        cellUIPreviewLayout.addView(cellUI3)

        rootLayout.addView(cellUIPreviewLayout)

        // CellUI 类型说明
        val cellUITypeLabel = TextView(this).apply {
            textSize = 12f
            setTextColor(0xFF80FFFFFF.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dpToPx(8))
            text = "1部分(居中) | 2部分(左右) | 3部分(三角: 资源点顶部, 我方左下, 敌方右下)"
        }
        rootLayout.addView(cellUITypeLabel)

        // 马场单独展示
        val ranchPreviewLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val cellUIRanch = HexCellDebugView(this).apply {
            hexRadius = 80f
            fillColor = 0xFFA8D5A2.toInt() // 平原色
            resourcePoint = com.xfbgy.hexmap.data.ResourcePoint(com.xfbgy.hexmap.data.ResourcePointType.RANCH)
            showFriendlyUnitSpace = true
            showEnemyUnitSpace = true
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(240), 1f)
        }
        ranchPreviewLayout.addView(cellUIRanch)
        val cellUIVillageFull = HexCellDebugView(this).apply {
            hexRadius = 80f
            fillColor = 0xFF9E9E9E.toInt()
            resourcePoint = com.xfbgy.hexmap.data.ResourcePoint(com.xfbgy.hexmap.data.ResourcePointType.VILLAGE)
            showFriendlyUnitSpace = true
            showEnemyUnitSpace = true
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(240), 1f)
        }
        ranchPreviewLayout.addView(cellUIVillageFull)
        val cellUITownFull = HexCellDebugView(this).apply {
            hexRadius = 80f
            fillColor = 0xFF9E9E9E.toInt()
            resourcePoint = com.xfbgy.hexmap.data.ResourcePoint(com.xfbgy.hexmap.data.ResourcePointType.TOWN)
            showFriendlyUnitSpace = true
            showEnemyUnitSpace = true
            layoutParams = LinearLayout.LayoutParams(0, dpToPx(240), 1f)
        }
        ranchPreviewLayout.addView(cellUITownFull)
        rootLayout.addView(ranchPreviewLayout)

        val cellUIRanchLabel = TextView(this).apply {
            textSize = 12f
            setTextColor(0xFF80FFFFFF.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dpToPx(8))
            text = "马场(平原) | 村庄(栅栏) | 城镇(土墙)  — 均含3部分三角布局"
        }
        rootLayout.addView(cellUIRanchLabel)

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
            hint = "5~50"
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

        rootLayout.addView(inputLayout)

        // 河流数量输入行
        val riverInputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val riverLabel = TextView(this).apply {
            text = "河流数量："
            textSize = 15f
            setTextColor(Color.WHITE)
        }
        riverInputLayout.addView(riverLabel)

        riverCountInput = EditText(this).apply {
            hint = "1~10"
            textSize = 15f
            inputType = InputType.TYPE_CLASS_NUMBER
            setTextColor(Color.WHITE)
            setHintTextColor(0xFF80FFFFFF.toInt())
            setBackgroundColor(0xFF37474F.toInt())
            setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
            layoutParams = LinearLayout.LayoutParams(dpToPx(80), dpToPx(44))
        }
        riverCountInput.setText("1")
        riverInputLayout.addView(riverCountInput)

        val riverHint = TextView(this).apply {
            text = " 条（横向/纵向各50%）"
            textSize = 13f
            setTextColor(0xFFB0FFFFFF.toInt())
        }
        riverInputLayout.addView(riverHint)

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
        riverInputLayout.addView(generateBtn)

        rootLayout.addView(riverInputLayout)

        // 地图信息
        mapInfoText = TextView(this).apply {
            textSize = 13f
            setTextColor(0xFFB0FFFFFF.toInt())
            gravity = Gravity.CENTER
            setPadding(0, dpToPx(8), 0, dpToPx(4))
            text = "点击\"生成地图\"创建随机地图"
        }
        rootLayout.addView(mapInfoText)

        // 缩放控制栏
        val zoomBarLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, dpToPx(4), 0, dpToPx(4))
        }

        val zoomHint = TextView(this).apply {
            text = "缩放："
            textSize = 14f
            setTextColor(0xFFB0FFFFFF.toInt())
        }
        zoomBarLayout.addView(zoomHint)

        val zoomOutBtn = Button(this).apply {
            text = "−"
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(0xFF37474F.toInt())
            layoutParams = LinearLayout.LayoutParams(dpToPx(44), dpToPx(36))
            setOnClickListener {
                mapGridView.zoomOut()
                updateZoomText()
            }
        }
        zoomBarLayout.addView(zoomOutBtn)

        zoomText = TextView(this).apply {
            text = "100%"
            textSize = 14f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dpToPx(64), LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        zoomBarLayout.addView(zoomText)

        val zoomInBtn = Button(this).apply {
            text = "+"
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(0xFF37474F.toInt())
            layoutParams = LinearLayout.LayoutParams(dpToPx(44), dpToPx(36))
            setOnClickListener {
                mapGridView.zoomIn()
                updateZoomText()
            }
        }
        zoomBarLayout.addView(zoomInBtn)

        val resetViewBtn = Button(this).apply {
            text = "重置视图"
            textSize = 12f
            setTextColor(Color.WHITE)
            setBackgroundColor(0xFF37474F.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dpToPx(36)
            ).apply {
                marginStart = dpToPx(8)
            }
            setOnClickListener {
                mapGridView.resetView()
                mapGridView.centerMap()
                updateZoomText()
            }
        }
        zoomBarLayout.addView(resetViewBtn)

        val tapHint = TextView(this).apply {
            text = "  双指缩放·拖拽平移·点击选中"
            textSize = 12f
            setTextColor(0xFF80FFFFFF.toInt())
        }
        zoomBarLayout.addView(tapHint)

        rootLayout.addView(zoomBarLayout)

        // 地图容器（自带缩放和平移）
        mapGridView = HexMapGridView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(500)
            )
            setBackgroundColor(0xFF0D1117.toInt())
            onCellSelected = { x, y -> updateCellInfo(x, y) }
        }
        rootLayout.addView(mapGridView)

        // 选中格子信息文本
        cellInfoText = TextView(this).apply {
            textSize = 13f
            setTextColor(0xFFE0E0E0.toInt())
            setBackgroundColor(0xFF1A1A2E.toInt())
            setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "点击地图格子查看详细信息"
        }
        rootLayout.addView(cellInfoText)

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

    /**
     * 更新选中格子信息文本
     */
    private fun updateCellInfo(x: Int, y: Int) {
        val m = currentMap
        if (m == null || x < 0 || y < 0) {
            cellInfoText.text = "点击地图格子查看详细信息"
            return
        }

        val cell = m.cells[x][y]
        val sb = StringBuilder()
        sb.append("坐标: ($x, $y)  |  地形: ${cell.terrain.chineseName}")

        // 资源点信息
        val rp = cell.resourcePoint
        if (rp != null) {
            sb.append("  |  ${rp.getDescription()}")
        }
        sb.append("\n")

        val dirNames = arrayOf("1-顶边", "2-右上", "3-右下", "4-底边", "5-左下", "6-左上")
        val edgeDescs = mutableListOf<String>()
        for (dir in 0 until 6) {
            val edge = m.edges[x][y][dir]
            val parts = mutableListOf<String>()
            if (edge.hasRiver) parts.add("河流")
            if (edge.fortification != FortType.NONE) parts.add("工事:${edge.fortification.chineseName}")
            val status = if (parts.isEmpty()) "—" else parts.joinToString(", ")
            edgeDescs.add("${dirNames[dir]}: $status")
        }

        sb.append(edgeDescs.joinToString("  "))
        cellInfoText.text = sb.toString()
    }

    /**
     * 更新缩放百分比显示
     */
    private fun updateZoomText() {
        zoomText.text = "${mapGridView.getZoomPercent()}%"
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
     * 生成建筑群位置（聚团 + 离散格子）
     * 按照以下算法：
     * 1. 在x坐标＜（1/6）n，随机选一个位置作为聚团1
     * 2. 移除与聚团距离＜（2/3）n的坐标
     * 3. 在剩余位置中随机选一个作为聚团2
     * 4. 继续生成聚团（要求不与现有聚团相邻）
     * 5. 聚团总数超过m*8%时停止
     * 6. 再分布m*8%的离散建筑群格子（互相不相邻，不与聚团相邻）
     *
     * @param size 地图大小
     * @param random 随机数生成器
     * @return Pair(聚团格子集合, 离散建筑群格子集合)
     */
    private fun generateUrbanCells(size: Int, random: Random): Pair<Set<Pair<Int, Int>>, Set<Pair<Int, Int>>> {
        val n = size
        val m = n * n

        android.util.Log.d("MapGen", "========== 开始建筑群生成 ==========")
        android.util.Log.d("MapGen", "地图大小: ${n}×${n}, 总格子数: ${m}")

        // 所有格子列表
        val allCells = mutableListOf<Pair<Int, Int>>()
        for (x in 0 until size) {
            for (y in 0 until size) {
                allCells.add(Pair(x, y))
            }
        }

        // 空图集：未放置任何地形的格子
        val emptySet = allCells.toMutableSet()
        // 聚团集：聚团占有的坐标集合
        val clusterSet = mutableSetOf<Pair<Int, Int>>()
        // 离散建筑集
        val discreteSet = mutableSetOf<Pair<Int, Int>>()

        // ========== 步骤1：在x坐标＜（1/6）n随机选一个位置作为聚团1 ==========
        android.util.Log.d("MapGen", "========== 步骤1：放置聚团1 ==========")
        val cluster1Candidates = allCells.filter { it.first < n / 6 }
        if (cluster1Candidates.isEmpty()) {
            android.util.Log.d("MapGen", "错误：无法找到聚团1的候选位置")
            return Pair(emptySet(), emptySet())
        }
        val cluster1Pos = cluster1Candidates.random(random)
        
        // 扩展聚团1为2-3格
        val cluster1Cells = expandToCluster(cluster1Pos, 2 + random.nextInt(2), size, clusterSet, random)
        clusterSet.addAll(cluster1Cells)
        emptySet.removeAll(cluster1Cells.toSet())

        android.util.Log.d("MapGen", "聚团1中心位置: $cluster1Pos")
        android.util.Log.d("MapGen", "聚团1格子: $cluster1Cells, 大小: ${cluster1Cells.size}")
        android.util.Log.d("MapGen", "聚团1加入聚团集后，聚团数量: ${countClusters(clusterSet)}, 聚团集大小: ${clusterSet.size}")

        // ========== 步骤2：移除与聚团距离＜（2/3）n的坐标 ==========
        android.util.Log.d("MapGen", "========== 步骤2：计算禁入区域 ==========")
        val exclusionDistance = 2.0 * n / 3
        val excludedCells = mutableSetOf<Pair<Int, Int>>()
        
        for (pos in emptySet.toList()) {
            var minDistToCluster = Int.MAX_VALUE
            for (clusterPos in clusterSet) {
                val dist = hexDistance(pos, clusterPos)
                minDistToCluster = minOf(minDistToCluster, dist)
            }
            if (minDistToCluster < exclusionDistance) {
                excludedCells.add(pos)
            }
        }
        
        emptySet.removeAll(excludedCells)
        android.util.Log.d("MapGen", "禁入区域距离阈值: ${exclusionDistance}")
        android.util.Log.d("MapGen", "禁入区域格子数: ${excludedCells.size}")
        android.util.Log.d("MapGen", "移除禁入区域后，空图集大小: ${emptySet.size}")

        // ========== 步骤3：在空图集中随机一个坐标作为聚团2 ==========
        android.util.Log.d("MapGen", "========== 步骤3：放置聚团2 ==========")
        if (emptySet.isNotEmpty()) {
            val cluster2Pos = emptySet.random(random)
            val cluster2Cells = expandToCluster(cluster2Pos, 2 + random.nextInt(2), size, clusterSet, random)
            clusterSet.addAll(cluster2Cells)
            emptySet.removeAll(cluster2Cells.toSet())
            
            // 移除聚团集和离散建筑集的所有邻居
            removeNeighborsFromSets(emptySet, clusterSet, discreteSet, size)

            android.util.Log.d("MapGen", "聚团2中心位置: $cluster2Pos")
            android.util.Log.d("MapGen", "聚团2格子: $cluster2Cells, 大小: ${cluster2Cells.size}")
            android.util.Log.d("MapGen", "聚团2加入后，聚团数量: ${countClusters(clusterSet)}, 聚团集大小: ${clusterSet.size}")
            android.util.Log.d("MapGen", "移除邻居后，空图集大小: ${emptySet.size}")
        } else {
            android.util.Log.d("MapGen", "空图集为空，跳过聚团2生成")
        }

        // ========== 步骤4：计算聚团总数 ==========
        android.util.Log.d("MapGen", "========== 步骤4：计算聚团总数 ==========")
        val clusterCount = countClusters(clusterSet)
        android.util.Log.d("MapGen", "当前聚团总数 x = $clusterCount")

        // ========== 步骤5：在空图集中生成离散建筑集（x*5次） ==========
        android.util.Log.d("MapGen", "========== 步骤5：生成离散建筑集 ==========")
        val discreteTarget = clusterCount * 5
        android.util.Log.d("MapGen", "离散建筑目标数量: ${discreteTarget}")
        
        repeat(discreteTarget) { index ->
            if (emptySet.isEmpty()) {
                android.util.Log.d("MapGen", "空图集为空，停止生成离散建筑，已生成: ${discreteSet.size}")
                return@repeat
            }
            
            val pos = emptySet.random(random)
            discreteSet.add(pos)
            emptySet.remove(pos)
            
            // 移除聚团集和离散建筑集的所有邻居
            removeNeighborsFromSets(emptySet, clusterSet, discreteSet, size)
            
            android.util.Log.d("MapGen", "第${index + 1}个离散建筑: $pos, 已生成: ${discreteSet.size}, 空图集剩余: ${emptySet.size}")
        }

        // ========== 步骤6-9：循环生成聚团和离散建筑 ==========
        android.util.Log.d("MapGen", "========== 步骤6-9：循环生成 ==========")
        val urbanTarget = (m * 0.10 - 8).toInt().coerceAtLeast(0)  // 总建筑群目标 = m*10% - 8
        var loopCount = 0
        
        while (true) {
            loopCount++
            android.util.Log.d("MapGen", "--- 循环第${loopCount}次 ---")
            
            // 步骤6：检查终止条件
            val totalUrbanCount = clusterSet.size + discreteSet.size
            android.util.Log.d("MapGen", "总建筑群格子数: ${totalUrbanCount}, 目标: ${urbanTarget}")
            
            if (totalUrbanCount >= urbanTarget || emptySet.isEmpty()) {
                android.util.Log.d("MapGen", "满足终止条件：总坐标数>=${urbanTarget}或空图集为空，跳到步骤10")
                break
            }
            
            // 步骤7：从空图集随机坐标生成聚团（聚团的所有坐标必须都在空图集中）
            if (!emptySet.isEmpty()) {
                val newClusterPos = emptySet.random(random)
                
                // 检查该位置及其邻居是否都在空图集中（用于形成2-3格聚团）
                val allClusterCells = mutableListOf(newClusterPos)
                allClusterCells.addAll(getNeighborCoords(newClusterPos.first, newClusterPos.second, size).filter { it in emptySet })
                
                // 尝试扩展聚团
                val expandedCluster = mutableListOf(newClusterPos)
                val tempOccupied = clusterSet.toMutableSet()
                
                for (pos in allClusterCells) {
                    if (expandedCluster.size >= 3) break
                    if (pos in emptySet && pos !in tempOccupied) {
                        expandedCluster.add(pos)
                        tempOccupied.add(pos)
                    }
                }
                
                if (expandedCluster.size >= 2) {
                    clusterSet.addAll(expandedCluster)
                    emptySet.removeAll(expandedCluster.toSet())
                    
                    // 移除聚团集和离散建筑集的所有邻居
                    removeNeighborsFromSets(emptySet, clusterSet, discreteSet, size)
                    
                    android.util.Log.d("MapGen", "生成新聚团: $expandedCluster, 大小: ${expandedCluster.size}")
                    android.util.Log.d("MapGen", "聚团数量: ${countClusters(clusterSet)}, 聚团集大小: ${clusterSet.size}")
                    android.util.Log.d("MapGen", "移除邻居后，空图集大小: ${emptySet.size}")
                } else {
                    android.util.Log.d("MapGen", "无法形成聚团（可用位置不足），跳过步骤7")
                }
            }
            
            // 步骤8：从空图集随机坐标放入离散建筑集
            if (!emptySet.isEmpty()) {
                val newDiscretePos = emptySet.random(random)
                discreteSet.add(newDiscretePos)
                emptySet.remove(newDiscretePos)
                
                // 移除聚团集和离散建筑集的所有邻居
                removeNeighborsFromSets(emptySet, clusterSet, discreteSet, size)
                
                android.util.Log.d("MapGen", "生成新离散建筑: $newDiscretePos, 离散建筑数量: ${discreteSet.size}")
                android.util.Log.d("MapGen", "移除邻居后，空图集大小: ${emptySet.size}")
            }
            
            // 防止无限循环
            if (loopCount > 500) {
                android.util.Log.d("MapGen", "达到最大循环次数，强制退出")
                break
            }
        }

        // ========== 步骤10：汇总结果 ==========
        android.util.Log.d("MapGen", "========== 步骤10：汇总结果 ==========")
        android.util.Log.d("MapGen", "聚团数量: ${countClusters(clusterSet)}")
        android.util.Log.d("MapGen", "聚团集格子数: ${clusterSet.size}")
        android.util.Log.d("MapGen", "聚团集格子: $clusterSet")
        android.util.Log.d("MapGen", "离散建筑集格子数: ${discreteSet.size}")
        android.util.Log.d("MapGen", "离散建筑集格子: $discreteSet")
        android.util.Log.d("MapGen", "总建筑群格子数: ${clusterSet.size + discreteSet.size}")
        android.util.Log.d("MapGen", "空图集剩余: ${emptySet.size}")
        android.util.Log.d("MapGen", "========== 建筑群生成完成 ==========")

        return Pair(clusterSet, discreteSet)
    }
    
    /**
     * 从空图集中移除聚团集和离散建筑集的所有邻居
     * @param emptySet 空图集
     * @param clusterSet 聚团集
     * @param discreteSet 离散建筑集
     * @param size 地图大小
     */
    private fun removeNeighborsFromSets(
        emptySet: MutableSet<Pair<Int, Int>>,
        clusterSet: Set<Pair<Int, Int>>,
        discreteSet: Set<Pair<Int, Int>>,
        size: Int
    ) {
        // 遍历聚团集的所有坐标点的邻居
        for (cell in clusterSet) {
            val neighbors = getNeighborCoords(cell.first, cell.second, size)
            emptySet.removeAll(neighbors)
        }
        // 遍历离散建筑集的所有坐标点的邻居
        for (cell in discreteSet) {
            val neighbors = getNeighborCoords(cell.first, cell.second, size)
            emptySet.removeAll(neighbors)
        }
    }
    
    /**
     * 计算聚团数量（从格子集合中识别独立的聚团）
     */
    private fun countClusters(clusterSet: Set<Pair<Int, Int>>): Int {
        if (clusterSet.isEmpty()) return 0
        
        val visited = mutableSetOf<Pair<Int, Int>>()
        var clusterCount = 0
        
        for (pos in clusterSet) {
            if (pos in visited) continue
            
            // BFS找聚团
            val queue = ArrayDeque<Pair<Int, Int>>()
            queue.add(pos)
            
            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                if (current in visited) continue
                if (current !in clusterSet) continue
                
                visited.add(current)
                
                // 获取邻居
                for (neighbor in getNeighborCoords(current.first, current.second, 100)) {
                    if (neighbor in clusterSet && neighbor !in visited) {
                        queue.add(neighbor)
                    }
                }
            }
            
            clusterCount++
        }
        
        return clusterCount
    }

    /**
     * 扩展一个位置为2-3格的聚团
     * @param center 中心位置
     * @param clusterSize 聚团大小（2-3）
     * @param size 地图大小
     * @param occupied 已被占用的格子集合（会排除这些格子）
     * @param random 随机数生成器
     * @return 聚团包含的格子集合
     */
    private fun expandToCluster(
        center: Pair<Int, Int>,
        clusterSize: Int,
        size: Int,
        occupied: Set<Pair<Int, Int>>,
        random: Random
    ): MutableList<Pair<Int, Int>> {
        val cluster = mutableListOf(center)
        val clusterSet = mutableSetOf(center)  // 用于快速查找

        // 获取所有相邻格子（排除已占用的）
        val neighbors = getNeighborCoords(center.first, center.second, size)
            .filter { it !in occupied }

        // 随机打乱邻居顺序
        val shuffledNeighbors = neighbors.shuffled(random)

        // 添加相邻格子直到达到目标大小
        for (neighbor in shuffledNeighbors) {
            if (cluster.size >= clusterSize) break
            if (neighbor !in clusterSet && neighbor !in occupied) {
                cluster.add(neighbor)
                clusterSet.add(neighbor)
            }
        }

        // 如果邻居不够，尝试添加相邻格子的邻居（距离2）
        if (cluster.size < clusterSize) {
            for (neighbor in shuffledNeighbors) {
                if (cluster.size >= clusterSize) break
                val secondNeighbors = getNeighborCoords(neighbor.first, neighbor.second, size)
                    .filter { it !in occupied && it !in clusterSet }
                for (second in secondNeighbors) {
                    if (cluster.size >= clusterSize) break
                    if (hexDistance(second, center) <= 2) {
                        cluster.add(second)
                        clusterSet.add(second)
                    }
                }
            }
        }

        // 确保至少2格（如果可用邻居足够）
        if (cluster.size < 2 && shuffledNeighbors.isNotEmpty()) {
            cluster.add(shuffledNeighbors[0])
        }

        return cluster
    }

    /**
     * 计算两个六角格坐标之间的六边形网格距离（步数）
     * 使用 odd-q 坐标系统的立方坐标转换
     */
    private fun hexDistance(p1: Pair<Int, Int>, p2: Pair<Int, Int>): Int {
        // odd-q 转立方坐标
        val x1 = p1.first
        val z1 = p1.second - (p1.first - (p1.first and 1)) / 2
        val y1 = -x1 - z1

        val x2 = p2.first
        val z2 = p2.second - (p2.first - (p2.first and 1)) / 2
        val y2 = -x2 - z2

        // 立方坐标距离 = max(|dx|, |dy|, |dz|)
        return maxOf(
            kotlin.math.abs(x1 - x2),
            kotlin.math.abs(y1 - y2),
            kotlin.math.abs(z1 - z2)
        )
    }

    /**
     * 获取六角格邻居坐标（odd-q 偏移坐标）
     */
    private fun getNeighborCoords(x: Int, y: Int, size: Int): List<Pair<Int, Int>> {
        val offsets = if (x % 2 == 0) {
            // 偶数列邻居偏移
            arrayOf(Pair(0, -1), Pair(1, -1), Pair(1, 0), Pair(0, 1), Pair(-1, 0), Pair(-1, -1))
        } else {
            // 奇数列邻居偏移
            arrayOf(Pair(0, -1), Pair(1, 0), Pair(1, 1), Pair(0, 1), Pair(-1, 1), Pair(-1, 0))
        }

        return offsets.map { (dx, dy) -> Pair(x + dx, y + dy) }
            .filter { (nx, ny) -> nx in 0 until size && ny in 0 until size }
    }

    /**
     * 为建筑群设置防御工事
     *
     * 规则：
     * 1. 只有建筑群格子有防御工事，其他地形6条边均无防御工事
     * 2. 离散建筑群（不与其他建筑群格子相邻）：6条边防御工事统一为"栅栏"或"土墙"，比例2:1
     * 3. 聚团建筑群：6条边防御工事统一为"石墙"
     * 4. 聚团内相邻格子之间的边：防御工事改为"无"
     *
     * @param map 地图
     * @param urbanCells 所有建筑群格子
     * @param clusterSet 聚团格子集合（来自算法生成）
     * @param random 随机数生成器
     */
    private fun assignUrbanFortifications(
        map: DebugHexMap,
        urbanCells: Set<Pair<Int, Int>>,
        clusterSet: Set<Pair<Int, Int>>,
        random: Random
    ) {
        // 第一步：重置所有格子的防御工事为NONE
        for (x in 0 until map.width) {
            for (y in 0 until map.height) {
                for (dir in 0..5) {
                    map.setFortification(x, y, dir, FortType.NONE)
                }
            }
        }

        // 离散建筑群格子 = 所有建筑群 - 聚团格子
        val discreteCells = urbanCells - clusterSet

        android.util.Log.d("MapGen", "聚团格子数: ${clusterSet.size}, 离散格子数: ${discreteCells.size}")

        // 第二步：为离散建筑群设置栅栏/土墙（2:1比例，栅栏2份，土墙1份）
        val discreteList = discreteCells.toList()
        val totalDiscrete = discreteList.size
        val fenceCount = kotlin.math.round(totalDiscrete * 2.0 / 3.0).toInt()
        val shuffledDiscretes = discreteList.shuffled(random)

        shuffledDiscretes.forEachIndexed { index, (x, y) ->
            val fortType = if (index < fenceCount) FortType.FENCE else FortType.EARTHWALL
            for (dir in 0..5) {
                map.setFortification(x, y, dir, fortType)
            }
        }

        // 第三步：为聚团建筑群设置石墙
        for (cell in clusterSet) {
            val (x, y) = cell
            for (dir in 0..5) {
                map.setFortification(x, y, dir, FortType.STONEWALL)
            }
        }

        // 第四步：擦除聚团内相邻格子之间的防御工事
        for (cell in clusterSet) {
            val (x, y) = cell
            for (dir in 0..5) {
                val (nx, ny) = map.getNeighborCoord(x, y, dir)
                if (nx !in 0 until map.width || ny !in 0 until map.height) continue

                val neighborPos = Pair(nx, ny)
                // 如果邻居也在聚团内，清除防御工事
                if (neighborPos in clusterSet) {
                    map.setFortification(x, y, dir, FortType.NONE)
                }
            }
        }
    }

    /**
     * 生成随机地图
     */
    private fun generateMap() {
        val sizeStr = mapSizeInput.text.toString()
        val size = sizeStr.toIntOrNull()

        if (size == null || size !in 5..50) {
            Toast.makeText(this, "请输入5~50之间的数字", Toast.LENGTH_SHORT).show()
            return
        }

        val totalCells = size * size
        val random = Random.Default
        val map = DebugHexMap(size, size)

        // 地形计数
        val terrainCounts = mutableMapOf<TerrainType, Int>()
        TerrainType.entries.forEach { terrainCounts[it] = 0 }

        // ========== 步骤6-7：生成建筑群（聚团 + 离散格子） ==========
        // 按照算法：聚团m*8% + 离散建筑群m*8%
        val (clusterSet, discreteSet) = generateUrbanCells(size, random)
        val allUrbanCells = clusterSet + discreteSet
        val urbanCount = allUrbanCells.size

        // 调试日志
        android.util.Log.d("MapGen", "聚团格子数: ${clusterSet.size}")
        android.util.Log.d("MapGen", "离散建筑群格子数: ${discreteSet.size}")
        android.util.Log.d("MapGen", "总建筑群格子数: $urbanCount")

        // ========== 步骤8：分配地形（60%平原，15%山地，10%森林，5%高山） ==========
        // 高山必须与山地相邻
        val nonUrbanCount = totalCells - urbanCount
        val plainRatio = 0.60f
        val hillRatio = 0.15f
        val forestRatio = 0.10f
        val mountainRatio = 0.05f

        val hillCount = (nonUrbanCount * hillRatio).toInt().coerceAtLeast(1)
        val forestCount = (nonUrbanCount * forestRatio).toInt().coerceAtLeast(1)
        val mountainCount = (nonUrbanCount * mountainRatio).toInt().coerceAtLeast(1)
        val plainCount = nonUrbanCount - hillCount - forestCount - mountainCount

        android.util.Log.d("MapGen", "地形分配: 平原=$plainCount, 山地=$hillCount, 森林=$forestCount, 高山=$mountainCount")

        // 创建加权列表（先生成山地，高山后面再处理）
        val weightedTerrains = mutableListOf<TerrainType>()
        repeat(hillCount) { weightedTerrains.add(TerrainType.HILL) }
        repeat(forestCount) { weightedTerrains.add(TerrainType.FOREST) }
        repeat(plainCount) { weightedTerrains.add(TerrainType.PLAIN) }
        weightedTerrains.shuffle(random)

        // 分配地形到每个格子（先不分配高山）
        for (x in 0 until size) {
            for (y in 0 until size) {
                val pos = Pair(x, y)
                val terrain = if (allUrbanCells.contains(pos)) {
                    TerrainType.URBAN
                } else {
                    if (weightedTerrains.isNotEmpty()) weightedTerrains.removeAt(0) else TerrainType.PLAIN
                }
                map.cells[x][y].terrain = terrain
                terrainCounts[terrain] = terrainCounts[terrain]!! + 1
            }
        }

        // 高山必须在山地旁边生成
        val hillCells = mutableListOf<Pair<Int, Int>>()
        for (x in 0 until size) {
            for (y in 0 until size) {
                if (map.cells[x][y].terrain == TerrainType.HILL) {
                    hillCells.add(Pair(x, y))
                }
            }
        }

        // 从山地格子周围随机选择位置放置高山
        var mountainsPlaced = 0
        if (hillCells.isNotEmpty() && mountainCount > 0) {
            repeat(mountainCount) {
                if (hillCells.isEmpty()) return@repeat
                val hillCell = hillCells.random(random)
                val neighbors = map.getAllNeighborCoords(hillCell.first, hillCell.second).filter { (nx, ny) ->
                    nx in 0 until size && ny in 0 until size
                }
                val availableNeighbors = neighbors.filter { (nx, ny) ->
                    map.cells[nx][ny].terrain == TerrainType.PLAIN
                }
                if (availableNeighbors.isNotEmpty()) {
                    val (mx, my) = availableNeighbors.random(random)
                    map.cells[mx][my].terrain = TerrainType.MOUNTAIN
                    mountainsPlaced++
                    terrainCounts[TerrainType.MOUNTAIN] = terrainCounts[TerrainType.MOUNTAIN]!! + 1
                } else {
                    // 如果没有可用邻居，尝试其他山地
                    val otherHills = hillCells.filter { it != hillCell }
                    if (otherHills.isNotEmpty()) {
                        val otherHill = otherHills.random(random)
                        val otherNeighbors = map.getAllNeighborCoords(otherHill.first, otherHill.second).filter { (nx, ny) ->
                            nx in 0 until size && ny in 0 until size && map.cells[nx][ny].terrain == TerrainType.PLAIN
                        }
                        if (otherHills.isNotEmpty()) {
                            val (mx, my) = otherNeighbors.random(random)
                            map.cells[mx][my].terrain = TerrainType.MOUNTAIN
                            mountainsPlaced++
                            terrainCounts[TerrainType.MOUNTAIN] = terrainCounts[TerrainType.MOUNTAIN]!! + 1
                        }
                    }
                }
            }
        }

        android.util.Log.d("MapGen", "实际放置高山数: $mountainsPlaced")



        // 3. 防御工事（按规则生成）
        // 规则：
        // - 只有建筑群格子有防御工事
        // - 离散建筑群：6条边统一为"栅栏"或"土墙"，比例2:1
        // - 聚团建筑群：6条边统一为"石墙"
        // - 聚团内相邻格子之间：防御工事改为"无"
        assignUrbanFortifications(map, allUrbanCells, clusterSet, random)

        // 4. 生成河流
        val riverCountStr = riverCountInput.text.toString()
        val riverCount = riverCountStr.toIntOrNull()
        if (riverCount == null || riverCount !in 1..10) {
            Toast.makeText(this, "河流数量请输入1~10之间的数字", Toast.LENGTH_SHORT).show()
            return
        }

        val riverGenerator = DebugRiverGenerator()
        riverGenerator.generateMultiple(map, riverCount)

        // 5. 识别资源点（村庄/城镇/都市/马场）
        val resourceScanner = ResourcePointScanner()
        resourceScanner.scanMap(map, clusterSet)

        // 6. 显示地图
        currentMap = map
        mapGridView.map = map

        // 根据地图大小调整hex半径（flat-top: 列间距=1.5R）
        val maxMapWidthPx = resources.displayMetrics.widthPixels - dpToPx(32)
        val desiredRadius = maxMapWidthPx / ((size - 1) * 1.5f + 2f)
        val clampedRadius = desiredRadius.coerceIn(
            8f * resources.displayMetrics.density,
            50f * resources.displayMetrics.density
        )
        mapGridView.hexRadius = clampedRadius

        // 居中地图
        mapGridView.post {
            mapGridView.centerMap()
            updateZoomText()
        }

        // 清空选中信息
        cellInfoText.text = "点击地图格子查看详细信息"

        // 统计河流和工事
        var riverEdgeCount = 0
        var fortCount = 0
        for (x in 0 until size) {
            for (y in 0 until size) {
                for (dir in 0..5) {
                    if (map.edges[x][y][dir].hasRiver) riverEdgeCount++
                    if (map.edges[x][y][dir].fortification != FortType.NONE) fortCount++
                }
            }
        }
        riverEdgeCount /= 2  // 河流边被两侧都计数了，除以2

        // 5. 生成地形统计信息
        val terrainStats = TerrainType.entries.joinToString(" | ") { terrain ->
            val count = terrainCounts[terrain]!!
            val percent = (count * 100.0 / totalCells).let { String.format("%.1f", it) }
            "${terrain.chineseName}: $count($percent%)"
        }

        // 资源点统计
        val resourceStats = ResourcePointType.entries.map { type ->
            var count = 0
            for (rx in 0 until size) {
                for (ry in 0 until size) {
                    if (map.cells[rx][ry].resourcePoint?.type == type) count++
                }
            }
            "${type.chineseName}:$count"
        }.joinToString(" ")

        mapInfoText.text = "${size}×${size}地图(${totalCells}格) | $terrainStats | 河流${riverCount}条(${riverEdgeCount}边) | 工事${fortCount}边 | 资源[$resourceStats]"
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
