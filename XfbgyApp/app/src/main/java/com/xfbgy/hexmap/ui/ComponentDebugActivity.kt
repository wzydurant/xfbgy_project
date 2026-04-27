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
     * 生成建筑群位置（聚团 + 离散格子混合）
     *
     * 规则：
     * - 聚团数量与建筑群格比例为1:5（聚团算1个格子）
     * - 聚团数量限制在2-6个
     * - 每个聚团1个格子（确保聚团数:离散格子数 ≈ 1:5）
     * - 聚团间距离≥2/3边长
     * - 剩余格子作为离散格子分布
     *
     * @param size 地图大小
     * @param urbanCount 建筑群数量
     * @param random 随机数生成器
     * @return 建筑群格子坐标集合
     */
    private fun generateUrbanCells(size: Int, urbanCount: Int, random: Random): Set<Pair<Int, Int>> {
        val minClusterDist = maxOf(3, size / 2)  // 聚团中心间最小距离（≥1/2边长，至少3格）

        // ========== 修复：确保至少有2个聚团 ==========
        // 每个聚团至少需要1个格子，如果 urbanCount 不足以生成2个聚团，强制增加
        val effectiveUrbanCount = maxOf(urbanCount, 4)  // 至少4个格子，确保能生成2个聚团

        // 计算聚团数量：至少2个
        val clusterCount = maxOf(2, (effectiveUrbanCount / 5.0).toInt().coerceAtMost(6))

        // 所有格子列表
        val allCells = mutableListOf<Pair<Int, Int>>()
        for (x in 0 until size) {
            for (y in 0 until size) {
                allCells.add(Pair(x, y))
            }
        }

        // 确保至少有2个格子
        if (allCells.size < 2) {
            return emptySet()
        }

        // ========== 1. 生成聚团中心 ==========
        // 策略：确保至少2个聚团中心，且它们之间距离足够远（不会被BFS合并）
        val clusterCenters = mutableListOf<Pair<Int, Int>>()
        
        // 随机打乱格子顺序
        allCells.shuffle(random)
        
        // 第一个聚团中心：随机选择
        clusterCenters.add(allCells[0])
        
        // 第二个聚团中心：选择与第一个距离最大的，且距离必须 >= 2（避免BFS合并）
        var maxDist = 0
        var secondCenter = allCells[0]
        for (cell in allCells) {
            if (cell == clusterCenters[0]) continue
            val dist = hexDistance(cell, clusterCenters[0])
            // 优先选择距离 >= 2 的格子（避免BFS合并），其次选择距离最大的
            if (dist >= 2 && dist > maxDist) {
                maxDist = dist
                secondCenter = cell
            } else if (maxDist < 2 && dist > maxDist) {
                // 如果还没找到距离 >= 2 的，选择距离最大的
                maxDist = dist
                secondCenter = cell
            }
        }
        
        // 确保第二个中心与第一个距离至少为 2（不会被BFS合并为同一聚团）
        if (hexDistance(secondCenter, clusterCenters[0]) < 2) {
            // 如果找不到距离 >= 2 的，强制找一个距离最大的
            for (cell in allCells) {
                if (cell == clusterCenters[0]) continue
                val dist = hexDistance(cell, clusterCenters[0])
                if (dist > maxDist) {
                    maxDist = dist
                    secondCenter = cell
                }
            }
        }
        
        clusterCenters.add(secondCenter)

        android.util.Log.d("MapGen", "聚团中心: $clusterCenters, 数量: ${clusterCenters.size}")
        
        // 如果还需要更多聚团中心（clusterCount > 2），尝试添加
        if (clusterCount > 2) {
            while (clusterCenters.size < clusterCount) {
                var bestCell: Pair<Int, Int>? = null
                var bestMinDist = 0

                for (cell in allCells) {
                    if (cell in clusterCenters) continue

                    val minDist = clusterCenters.minOf { hexDistance(it, cell) }
                    // 确保与所有现有中心的距离都 >= 2（不会被BFS合并）
                    if (minDist >= 2 && minDist > bestMinDist) {
                        bestMinDist = minDist
                        bestCell = cell
                    }
                }

                if (bestCell != null) {
                    clusterCenters.add(bestCell)
                } else {
                    break  // 无法添加更多时退出
                }
            }
        }

        // ========== 2. 在聚团中心生成建筑群 ==========
        val selectedUrbans = mutableSetOf<Pair<Int, Int>>()
        for (center in clusterCenters) {
            selectedUrbans.add(center)
        }

        // ========== 3. 生成离散建筑群格子 ==========
        val remainingCount = effectiveUrbanCount - selectedUrbans.size
        
        if (remainingCount > 0) {
            // 收集可用的离散格子（排除已有建筑群和聚团中心附近）
            val occupied = selectedUrbans.toMutableSet()
            val candidates = allCells.filter { cell ->
                !occupied.contains(cell) && 
                clusterCenters.none { hexDistance(cell, it) < 2.5 }
            }.toMutableList()  // 转换为 MutableList
            
            // 随机选择离散格子
            candidates.shuffle(random)
            for (i in 0 until minOf(remainingCount, candidates.size)) {
                selectedUrbans.add(candidates[i])
            }
        }

        return selectedUrbans
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
     */
    private fun assignUrbanFortifications(
        map: DebugHexMap,
        urbanCells: Set<Pair<Int, Int>>,
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

        // 第二步：识别所有建筑群聚团（通过BFS）
        val visited = mutableSetOf<Pair<Int, Int>>()
        val clusters = mutableListOf<List<Pair<Int, Int>>>()

        android.util.Log.d("MapGen", "开始BFS识别聚团，urbanCells: $urbanCells")

        for (cell in urbanCells) {
            if (cell in visited) continue

            val cluster = mutableListOf<Pair<Int, Int>>()
            val queue = ArrayDeque<Pair<Int, Int>>()
            queue.add(cell)

            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                if (current in visited) continue

                visited.add(current)
                cluster.add(current)

                // 查找相邻的建筑群格子
                for ((nx, ny) in map.getAllNeighborCoords(current.first, current.second)) {
                    val neighbor = Pair(nx, ny)
                    if (neighbor in urbanCells && neighbor !in visited) {
                        queue.add(neighbor)
                    }
                }
            }

            if (cluster.isNotEmpty()) {
                clusters.add(cluster)
                android.util.Log.d("MapGen", "发现聚团: $cluster")
            }
        }

        android.util.Log.d("MapGen", "BFS识别完成，共 ${clusters.size} 个聚团")

        // 构建聚团ID映射：格子坐标 -> 聚团索引
        val clusterIdMap = mutableMapOf<Pair<Int, Int>, Int>()
        clusters.forEachIndexed { index, cluster ->
            cluster.forEach { pos -> clusterIdMap[pos] = index }
        }

        // 第三步：收集离散格子（1格聚团）
        val discreteCells = mutableListOf<Pair<Int, Int>>()
        for (cluster in clusters) {
            if (cluster.size == 1) {
                discreteCells.add(cluster[0])
            }
        }

        // 第四步：为离散建筑群设置栅栏/土墙（2:1比例，栅栏2份，土墙1份）
        // 使用 round() 四舍五入确保比例准确
        val totalDiscrete = discreteCells.size
        val fenceCount = kotlin.math.round(totalDiscrete * 2.0 / 3.0).toInt()
        val shuffledDiscretes = discreteCells.shuffled(random)

        shuffledDiscretes.forEachIndexed { index, (x, y) ->
            val fortType = if (index < fenceCount) FortType.FENCE else FortType.EARTHWALL
            for (dir in 0..5) {
                map.setFortification(x, y, dir, fortType)
            }
        }

        // 第五步：为聚团建筑群设置石墙
        for (cluster in clusters) {
            if (cluster.size > 1) {
                for (cell in cluster) {
                    val (x, y) = cell
                    for (dir in 0..5) {
                        map.setFortification(x, y, dir, FortType.STONEWALL)
                    }
                }
            }
        }

        // 第六步：擦除聚团内相邻格子之间的防御工事
        for (cluster in clusters) {
            if (cluster.size > 1) {
                for (cell in cluster) {
                    val (x, y) = cell
                    for (dir in 0..5) {
                        val (nx, ny) = map.getNeighborCoord(x, y, dir)
                        if (nx !in 0 until map.width || ny !in 0 until map.height) continue

                        val neighborPos = Pair(nx, ny)
                        // 如果邻居在同一个聚团内，清除防御工事
                        if (clusterIdMap[neighborPos] == clusterIdMap[Pair(x, y)]) {
                            map.setFortification(x, y, dir, FortType.NONE)
                        }
                    }
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

        // 目标比例：森林12%, 山地20%, 高山8%, 建筑群8%, 平原52%
        // 高山必须与山地相连才能存在
        val targetForest = 0.12f
        val targetHill = 0.20f
        val targetMountain = 0.08f
        val targetUrban = 0.08f

        // 1. 生成建筑群位置
        val urbanCount = (totalCells * targetUrban).toInt().coerceAtLeast(2)  // 至少2个
        val urbanCells = generateUrbanCells(size, urbanCount, random)

        // 调试日志
        android.util.Log.d("MapGen", "生成建筑群格子数: ${urbanCells.size}")
        android.util.Log.d("MapGen", "建筑群位置: $urbanCells")

        // 2. 计算非建筑群格子的目标数量和比例
        val nonUrbanCount = totalCells - urbanCount
        // 非建筑群格子中：平原52%, 森林12%, 山地20%, 高山8%（按比例调整）
        val nonUrbanPlainRatio = 0.52f / (0.52f + 0.12f + 0.20f + 0.08f)
        val nonUrbanForestRatio = 0.12f / (0.52f + 0.12f + 0.20f + 0.08f)
        val nonUrbanHillRatio = 0.20f / (0.52f + 0.12f + 0.20f + 0.08f)
        val nonUrbanMountainRatio = 0.08f / (0.52f + 0.12f + 0.20f + 0.08f)

        val hillCount = (nonUrbanCount * nonUrbanHillRatio).toInt().coerceAtLeast(1)
        val forestCount = (nonUrbanCount * nonUrbanForestRatio).toInt().coerceAtLeast(1)
        val mountainCount = (nonUrbanCount * nonUrbanMountainRatio).toInt().coerceAtLeast(1)
        val plainCount = (nonUrbanCount * nonUrbanPlainRatio).toInt().coerceAtLeast(1)

        // 创建加权列表（不含建筑群和高山，先生成山地）
        val weightedTerrains = mutableListOf<TerrainType>()
        repeat(hillCount) { weightedTerrains.add(TerrainType.HILL) }
        repeat(forestCount) { weightedTerrains.add(TerrainType.FOREST) }
        repeat(plainCount) { weightedTerrains.add(TerrainType.PLAIN) }

        // 分配地形到每个格子（先不分配高山）
        for (x in 0 until size) {
            for (y in 0 until size) {
                val pos = Pair(x, y)
                val terrain = if (urbanCells.contains(pos)) {
                    TerrainType.URBAN
                } else {
                    weightedTerrains.randomOrNull(random) ?: TerrainType.PLAIN
                }
                map.cells[x][y].terrain = terrain
                terrainCounts[terrain] = terrainCounts[terrain]!! + 1
            }
        }

        // 高山必须在山地旁边生成：在所有山地格子周围放置高山
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
                    // 如果没有可用邻居，从其他山地格子周围找
                    val otherHills = hillCells.filter { it != hillCell }
                    if (otherHills.isNotEmpty()) {
                        val otherHill = otherHills.random(random)
                        val otherNeighbors = map.getAllNeighborCoords(otherHill.first, otherHill.second).filter { (nx, ny) ->
                            nx in 0 until size && ny in 0 until size && map.cells[nx][ny].terrain == TerrainType.PLAIN
                        }
                        if (otherNeighbors.isNotEmpty()) {
                            val (mx, my) = otherNeighbors.random(random)
                            map.cells[mx][my].terrain = TerrainType.MOUNTAIN
                            mountainsPlaced++
                            terrainCounts[TerrainType.MOUNTAIN] = terrainCounts[TerrainType.MOUNTAIN]!! + 1
                        }
                    }
                }
            }
        }



        // 3. 防御工事（按规则生成）
        // 规则：
        // - 只有建筑群格子有防御工事
        // - 离散建筑群：6条边统一为"栅栏"或"土墙"，比例2:1
        // - 聚团建筑群：6条边统一为"石墙"
        // - 聚团内相邻格子之间：防御工事改为"无"
        assignUrbanFortifications(map, urbanCells, random)

        // 4. 生成河流
        val riverCountStr = riverCountInput.text.toString()
        val riverCount = riverCountStr.toIntOrNull()
        if (riverCount == null || riverCount !in 1..10) {
            Toast.makeText(this, "河流数量请输入1~10之间的数字", Toast.LENGTH_SHORT).show()
            return
        }

        val riverGenerator = DebugRiverGenerator()
        riverGenerator.generateMultiple(map, riverCount)

        // 4. 显示地图
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

        mapInfoText.text = "${size}×${size}地图(${totalCells}格) | $terrainStats | 河流${riverCount}条(${riverEdgeCount}边) | 工事${fortCount}边"
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
