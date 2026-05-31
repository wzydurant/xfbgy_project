package com.xfbgy.hexmap.ui

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.xfbgy.hexmap.data.DebugHexMap
import com.xfbgy.hexmap.data.EquipmentType
import com.xfbgy.hexmap.data.FortType
import com.xfbgy.hexmap.data.ResourceKind
import com.xfbgy.hexmap.data.ResourcePointType
import com.xfbgy.hexmap.data.TerrainType
import com.xfbgy.hexmap.game.OccupyResult
import com.xfbgy.hexmap.game.ResourceManager
import com.xfbgy.hexmap.game.TurnManager
import com.xfbgy.hexmap.game.WithdrawResult
import com.xfbgy.hexmap.generation.DebugRiverGenerator
import com.xfbgy.hexmap.generation.ResourcePointScanner
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * 游戏页面Activity
 *
 * 全屏显示六角格地图，支持：
 * - 自由拖动和缩放
 * - 无边框显示
 * - 点击格子查看详细信息
 * - 暂不显示敌我双方阵营UI
 */
class GameActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MAP_SIZE = "extra_map_size"
        const val EXTRA_RIVER_COUNT = "extra_river_count"
    }

    private lateinit var mapGridView: GameMapGridView
    private lateinit var cellInfoPanel: TextView
    private lateinit var mapInfoBar: TextView
    private lateinit var playerInfoBar: LinearLayout
    private lateinit var turnInfoText: TextView
    private lateinit var endTurnButton: Button
    private lateinit var resourceBarView: ResourceBarView
    private var currentMap: DebugHexMap? = null
    private var turnManager: TurnManager? = null
    private var resourceManager: ResourceManager? = null

    /** 是否处于"确认"阶段（结算后等待确认再换人） */
    private var isWaitingConfirm = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mapSize = intent.getIntExtra(EXTRA_MAP_SIZE, 10)
        val riverCount = intent.getIntExtra(EXTRA_RIVER_COUNT, 2)

        val rootView = createLayout()
        setContentView(rootView)

        // 自动生成地图
        generateAndShowMap(mapSize, riverCount)
    }

    /**
     * 创建全屏布局：地图占满屏幕 + 底部信息栏 + 玩家面板 + 结束回合按钮
     */
    private fun createLayout(): FrameLayout {
        val rootLayout = FrameLayout(this).apply {
            setBackgroundColor(0xFF0D1117.toInt())
        }

        // 地图View（全屏）
        mapGridView = GameMapGridView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            onCellSelected = { x, y -> onCellSelected(x, y) }
        }
        rootLayout.addView(mapGridView)

        // 顶部地图信息栏（半透明背景）
        mapInfoBar = TextView(this).apply {
            textSize = 12f
            setTextColor(0xFFB0FFFFFF.toInt())
            setBackgroundColor(0x80000000.toInt())
            setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6))
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP
            }
        }
        rootLayout.addView(mapInfoBar)

        // 底部格子信息面板（半透明背景）
        cellInfoPanel = TextView(this).apply {
            textSize = 13f
            setTextColor(0xFFE0E0E0.toInt())
            setBackgroundColor(0x80000000.toInt())
            setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM
                bottomMargin = dpToPx(48)
            }
            text = "点击地图格子查看详细信息"
            visibility = View.GONE
        }
        rootLayout.addView(cellInfoPanel)

        // 底部操作栏：玩家信息 + 结束回合按钮
        val bottomBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0xCC000000.toInt())
            setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dpToPx(48)
            ).apply {
                gravity = Gravity.BOTTOM
            }
        }

        // 玩家信息区域（点击查看玩家信息）
        val playerInfoClickArea = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
            isClickable = true
            isFocusable = true
            setOnClickListener { showPlayerInfoDialog() }

            // 点击提示文字
            playerInfoBar = LinearLayout(this@GameActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val hintText = TextView(this@GameActivity).apply {
                text = "点击查看玩家信息"
                textSize = 13f
                setTextColor(0xFFB0B0B0.toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.CENTER
            }
            playerInfoBar.addView(hintText)
            addView(playerInfoBar)
        }
        bottomBar.addView(playerInfoClickArea)

        // 结束回合按钮
        endTurnButton = Button(this).apply {
            text = "结束回合"
            textSize = 13f
            setTextColor(Color.WHITE)
            setBackgroundColor(0xFF1E90FF.toInt())
            setPadding(dpToPx(12), dpToPx(2), dpToPx(12), dpToPx(2))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dpToPx(36)
            )
            setOnClickListener { onEndTurnClicked() }
        }
        bottomBar.addView(endTurnButton)

        rootLayout.addView(bottomBar)

        // 回合信息（悬浮在右上方）
        turnInfoText = TextView(this).apply {
            textSize = 14f
            setTextColor(0xFFFF9800.toInt())
            setBackgroundColor(0x80000000.toInt())
            setPadding(dpToPx(10), dpToPx(6), dpToPx(10), dpToPx(6))
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = dpToPx(28)
                marginEnd = dpToPx(8)
            }
        }
        rootLayout.addView(turnInfoText)

        // 资源条（悬浮在右上方，回合信息下方）
        resourceBarView = ResourceBarView(this).apply {
            setBackgroundColor(0x80000000.toInt())
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = dpToPx(56)
                marginEnd = dpToPx(8)
            }
        }
        rootLayout.addView(resourceBarView)

        return rootLayout
    }

    /**
     * 生成并显示地图
     */
    private fun generateAndShowMap(size: Int, riverCount: Int) {
        val totalCells = size * size
        val random = Random.Default
        val map = DebugHexMap(size, size)

        val terrainCounts = mutableMapOf<TerrainType, Int>()
        TerrainType.entries.forEach { terrainCounts[it] = 0 }

        // 生成建筑群
        val (clusterSet, discreteSet) = generateUrbanCells(size, random)
        val allUrbanCells = clusterSet + discreteSet
        val urbanCount = allUrbanCells.size

        // 分配地形
        val nonUrbanCount = totalCells - urbanCount
        val plainRatio = 0.60f
        val hillRatio = 0.15f
        val forestRatio = 0.10f
        val mountainRatio = 0.05f

        val hillCount = (nonUrbanCount * hillRatio).toInt().coerceAtLeast(1)
        val forestCount = (nonUrbanCount * forestRatio).toInt().coerceAtLeast(1)
        val mountainCount = (nonUrbanCount * mountainRatio).toInt().coerceAtLeast(1)
        val plainCount = nonUrbanCount - hillCount - forestCount - mountainCount

        val weightedTerrains = mutableListOf<TerrainType>()
        repeat(hillCount) { weightedTerrains.add(TerrainType.HILL) }
        repeat(forestCount) { weightedTerrains.add(TerrainType.FOREST) }
        repeat(plainCount) { weightedTerrains.add(TerrainType.PLAIN) }
        weightedTerrains.shuffle(random)

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

        // 防御工事
        assignUrbanFortifications(map, allUrbanCells, clusterSet, random)

        // 生成河流
        val riverGenerator = DebugRiverGenerator()
        riverGenerator.generateMultiple(map, riverCount)

        // 识别资源点
        val resourceScanner = ResourcePointScanner()
        resourceScanner.scanMap(map, clusterSet)

        // 显示地图
        currentMap = map
        mapGridView.map = map

        // 根据地图大小调整hex半径（与HexMapActivity一致，2倍）
        val maxMapWidthPx = resources.displayMetrics.widthPixels
        val desiredRadius = maxMapWidthPx / ((size - 1) * 1.5f + 2f)
        val clampedRadius = (desiredRadius * 2f).coerceIn(
            16f * resources.displayMetrics.density,
            100f * resources.displayMetrics.density
        )
        mapGridView.hexRadius = clampedRadius

        // 居中地图
        mapGridView.post {
            mapGridView.centerMap()
        }

        // 更新顶部信息栏
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
        riverEdgeCount /= 2

        val terrainStats = TerrainType.entries.joinToString(" | ") { terrain ->
            val count = terrainCounts[terrain]!!
            val percent = (count * 100.0 / totalCells).let { String.format("%.1f", it) }
            "${terrain.chineseName}: $count($percent%)"
        }

        val resourceStats = ResourcePointType.entries.map { type ->
            var count = 0
            for (rx in 0 until size) {
                for (ry in 0 until size) {
                    if (map.cells[rx][ry].resourcePoint?.type == type) count++
                }
            }
            "${type.chineseName}:$count"
        }.joinToString(" ")

        mapInfoBar.text = "${size}×${size}(${totalCells}格) | $terrainStats | 河流${riverCount}条(${riverEdgeCount}边) | 工事${fortCount}边 | 资源[$resourceStats]"

        // 初始化回合管理器
        initTurnManager()
    }

    /**
     * 格子选中回调
     */
    private fun onCellSelected(x: Int, y: Int) {
        val m = currentMap
        if (m == null || x < 0 || y < 0) {
            cellInfoPanel.visibility = View.GONE
            return
        }

        cellInfoPanel.visibility = View.VISIBLE

        val cell = m.cells[x][y]
        val sb = StringBuilder()
        sb.append("坐标: ($x, $y)  |  地形: ${cell.terrain.chineseName}")

        val rp = cell.resourcePoint
        if (rp != null) {
            sb.append("  |  ${rp.getDescription()}")
        }

        // 显示占领者信息（支持多玩家共享）
        val tm = turnManager
        if (tm != null) {
            val owners = tm.getCellOwners(x, y)
            if (owners.isNotEmpty()) {
                sb.append("  |  已占领: ${owners.joinToString(", ") { it.name }}")
            } else {
                sb.append("  |  未占领")
            }
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
        cellInfoPanel.text = sb.toString()

        // 弹出占领确认弹窗
        if (tm != null) {
            showOccupyDialog(x, y)
        }
    }

    /**
     * 初始化回合管理器
     */
    private fun initTurnManager() {
        val m = currentMap ?: return

        // 初始化资源管理器
        resourceManager = ResourceManager(m).also { rm ->
            rm.scanMap()
        }

        turnManager = TurnManager(m).also { tm ->
            mapGridView.turnManager = tm

            // 关联资源管理器
            tm.resourceManager = resourceManager

            tm.onTurnChanged = { player, turn ->
                runOnUiThread {
                    updatePlayerInfoBar()
                    updateTurnInfo()
                    updateResourceBar()
                }
            }

            tm.onOccupationChanged = { _, _, _ ->
                runOnUiThread {
                    updatePlayerInfoBar()
                    updateResourceBar()
                    mapGridView.invalidate()
                }
            }
        }

        updatePlayerInfoBar()
        updateTurnInfo()
        updateResourceBar()
    }

    /**
     * 显示格子操作弹窗（占领/撤出/共享占领）
     */
    private fun showOccupyDialog(x: Int, y: Int) {
        val tm = turnManager ?: return
        val m = currentMap ?: return
        if (!m.isValidCell(x, y)) return

        val cell = m.cells[x][y]
        val currentPlayer = tm.getCurrentPlayer()
        val owners = tm.getCellOwners(x, y)

        val cellDesc = "($x,$y) ${cell.terrain.chineseName}"
        val rpDesc = cell.resourcePoint?.let { " | ${it.getDescription()}" } ?: ""

        when {
            // 情况1：当前玩家已占领 → 弹出"是否撤出"
            owners.any { it.id == currentPlayer.id } -> {
                val clusterInfo = cell.resourcePoint?.let { rp ->
                    if (rp.type == ResourcePointType.CITY) "（都市聚团将一并撤出）" else ""
                } ?: ""
                AlertDialog.Builder(this)
                    .setTitle("是否撤出")
                    .setMessage("${currentPlayer.name} 是否撤出 $cellDesc$rpDesc ？$clusterInfo")
                    .setPositiveButton("是") { _, _ ->
                        val result = tm.withdrawCell(x, y)
                        when (result) {
                            is WithdrawResult.Success -> {
                                updateCellInfoAfterWithdraw(x, y)
                            }
                            is WithdrawResult.NotOwned -> {}
                            is WithdrawResult.InvalidCell -> {}
                        }
                    }
                    .setNegativeButton("否", null)
                    .show()
            }

            // 情况2：未被占领 → 弹出"是否占领"
            owners.isEmpty() -> {
                AlertDialog.Builder(this)
                    .setTitle("是否占领")
                    .setMessage("${currentPlayer.name} 是否占领 $cellDesc$rpDesc ？")
                    .setPositiveButton("是") { _, _ ->
                        val result = tm.occupyCell(x, y)
                        handleOccupyResult(result, currentPlayer, x, y)
                    }
                    .setNegativeButton("否", null)
                    .show()
            }

            // 情况3：被对方占领（但当前玩家未占领） → 弹出"是否占领"（共享）
            else -> {
                val existingNames = owners.joinToString(", ") { it.name }
                val clusterInfo = cell.resourcePoint?.let { rp ->
                    if (rp.type == ResourcePointType.CITY) "（都市聚团将一并占领）" else ""
                } ?: ""
                AlertDialog.Builder(this)
                    .setTitle("是否占领")
                    .setMessage("${currentPlayer.name} 是否占领 $cellDesc$rpDesc ？\n当前占领者: $existingNames（共享占领）$clusterInfo")
                    .setPositiveButton("是") { _, _ ->
                        val result = tm.occupyCell(x, y)
                        handleOccupyResult(result, currentPlayer, x, y)
                    }
                    .setNegativeButton("否", null)
                    .show()
            }
        }
    }

    /**
     * 处理占领/共享占领结果
     */
    private fun handleOccupyResult(result: OccupyResult, currentPlayer: com.xfbgy.hexmap.game.Player, x: Int, y: Int) {
        when (result) {
            is OccupyResult.Success -> {
                updateCellInfoAfterOccupy(currentPlayer, x, y)
            }
            is OccupyResult.SharedOccupation -> {
                updateCellInfoAfterOccupy(currentPlayer, x, y)
            }
            is OccupyResult.AlreadyOwned -> {}
            is OccupyResult.InvalidCell -> {}
        }
    }

    /**
     * 占领后更新信息面板
     */
    private fun updateCellInfoAfterOccupy(currentPlayer: com.xfbgy.hexmap.game.Player, x: Int, y: Int) {
        cellInfoPanel.visibility = View.VISIBLE
        val tm = turnManager ?: return
        val owners = tm.getCellOwners(x, y)
        val text = cellInfoPanel.text.toString()
        val ownersText = if (owners.isNotEmpty()) "已占领: ${owners.joinToString(", ") { it.name }}" else "未占领"
        cellInfoPanel.text = text.replace(Regex("未占领|已占领: [^\\s]+"), ownersText)
    }

    /**
     * 撤出后更新信息面板
     */
    private fun updateCellInfoAfterWithdraw(x: Int, y: Int) {
        cellInfoPanel.visibility = View.VISIBLE
        val tm = turnManager ?: return
        val owners = tm.getCellOwners(x, y)
        val text = cellInfoPanel.text.toString()
        val ownersText = if (owners.isNotEmpty()) "已占领: ${owners.joinToString(", ") { it.name }}" else "未占领"
        cellInfoPanel.text = text.replace(Regex("未占领|已占领: [^\\s]+"), ownersText)
    }

    /**
     * 结束回合按钮点击
     *
     * 两步流程：
     * 1. 点击"结束回合" → 执行生产结算 → 按钮变为"确认"
     * 2. 点击"确认" → 切换到对手回合
     */
    private fun onEndTurnClicked() {
        if (isWaitingConfirm) {
            // 第二步：确认换人
            onConfirmTurnSwitch()
        } else {
            // 第一步：结束回合 → 执行生产结算
            onEndTurnSettle()
        }
    }

    /**
     * 第一步：结束回合，执行当前玩家的生产结算
     */
    private fun onEndTurnSettle() {
        val tm = turnManager ?: return
        val rm = resourceManager ?: return

        // 每个玩家结束回合时，结算该玩家的资源生产
        val currentFaction = tm.getCurrentPlayer().id
        rm.processRoundEnd(tm.turnNumber, currentFaction)

        // 进入确认阶段
        isWaitingConfirm = true
        endTurnButton.text = "确认"
        endTurnButton.setBackgroundColor(0xFF4CAF50.toInt()) // 绿色确认按钮

        // 更新资源条显示结算结果
        updateResourceBar()
        updateTurnInfo()

        cellInfoPanel.visibility = View.GONE
    }

    /**
     * 第二步：确认后切换到对手回合
     */
    private fun onConfirmTurnSwitch() {
        val tm = turnManager ?: return

        tm.endTurn()

        // 退出确认阶段
        isWaitingConfirm = false
        endTurnButton.text = "结束回合"
        endTurnButton.setBackgroundColor(0xFF1E90FF.toInt()) // 蓝色结束按钮

        updatePlayerInfoBar()
        updateTurnInfo()
        updateResourceBar()
    }

    /**
     * 更新玩家信息面板（底部栏提示文字）
     */
    private fun updatePlayerInfoBar() {
        val tm = turnManager ?: return
        val currentPlayer = tm.getCurrentPlayer()
        // 更新底部提示文字，显示当前玩家
        if (playerInfoBar.childCount > 0) {
            val hintText = playerInfoBar.getChildAt(0) as? TextView
            hintText?.text = "▸ ${currentPlayer.name} | 点击查看详情"
            hintText?.setTextColor(parsePlayerColor(currentPlayer.colorHex))
        }
    }

    /**
     * 显示玩家信息弹窗
     */
    private fun showPlayerInfoDialog() {
        val tm = turnManager ?: return
        val rm = resourceManager ?: return
        val currentPlayer = tm.getCurrentPlayer()

        val sb = StringBuilder()
        sb.appendLine("── 当前玩家: ${currentPlayer.name} ──")
        sb.appendLine()

        val resourceCounts = tm.getOccupiedResourceCounts(currentPlayer.id)
        val totalCells = tm.getOccupiedCellCount(currentPlayer.id)
        val totalResources = tm.getOccupiedResourceCount(currentPlayer.id)

        sb.appendLine("占领格子数: $totalCells")
        sb.appendLine("资源点总数: $totalResources")
        sb.appendLine()

        if (resourceCounts.entries.any { it.value > 0 }) {
            sb.appendLine("资源详情:")
            for (entry in resourceCounts.entries.filter { it.value > 0 }) {
                sb.appendLine("  ${entry.key.iconLabel} ${entry.key.chineseName}: ${entry.value}")
            }
            sb.appendLine()
        }

        // 显示资源储存
        val resourceTotals = rm.getPlayerResourceTotals(currentPlayer.id)
        val equipTotals = rm.getPlayerEquipTotals(currentPlayer.id)
        val hasResources = resourceTotals.any { it.value > 0 } || equipTotals.any { it.value > 0 }

        if (hasResources) {
            sb.appendLine("资源储存:")
            for ((kind, count) in resourceTotals) {
                if (count > 0 && kind != ResourceKind.FOOD) {
                    sb.appendLine("  ${kind.icon} ${kind.chineseName}: $count")
                }
            }
            val equipParts = equipTotals.filter { it.value > 0 }.map { "  ${it.key.chineseName}×${it.value}" }
            if (equipParts.isNotEmpty()) {
                sb.appendLine("装备:")
                for (part in equipParts) {
                    sb.appendLine(part)
                }
            }
        }

        AlertDialog.Builder(this)
            .setTitle("玩家信息")
            .setMessage(sb.toString().trimEnd())
            .setPositiveButton("确定", null)
            .show()
    }

    /**
     * 更新回合信息
     */
    private fun updateTurnInfo() {
        val tm = turnManager ?: return
        val currentPlayer = tm.getCurrentPlayer()
        turnInfoText.text = "第${tm.turnNumber}回合 | ${currentPlayer.name}"
    }

    /**
     * 更新资源条显示
     */
    private fun updateResourceBar() {
        val rm = resourceManager ?: return
        val tm = turnManager ?: return
        val currentPlayer = tm.getCurrentPlayer()
        val resourceTotals = rm.getPlayerResourceTotals(currentPlayer.id)
        val equipTotals = rm.getPlayerEquipTotals(currentPlayer.id)
        val foodSummary = rm.getLastRoundFoodSummary(currentPlayer.id)
        resourceBarView.updateResources(resourceTotals, equipTotals, foodSummary)
    }

    private fun parsePlayerColor(colorHex: String): Int {
        return try {
            Color.parseColor(colorHex)
        } catch (e: Exception) {
            Color.WHITE
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    // ==================== 地图生成逻辑 ====================

    private fun generateUrbanCells(size: Int, random: Random): Pair<Set<Pair<Int, Int>>, Set<Pair<Int, Int>>> {
        val n = size
        val m = n * n

        val allCells = mutableListOf<Pair<Int, Int>>()
        for (x in 0 until size) {
            for (y in 0 until size) {
                allCells.add(Pair(x, y))
            }
        }

        val emptySet = allCells.toMutableSet()
        val clusterSet = mutableSetOf<Pair<Int, Int>>()
        val discreteSet = mutableSetOf<Pair<Int, Int>>()

        // 步骤1
        val cluster1Candidates = allCells.filter { it.first < n / 6 }
        if (cluster1Candidates.isEmpty()) {
            return Pair(emptySet(), emptySet())
        }
        val cluster1Pos = cluster1Candidates.random(random)
        val cluster1Cells = expandToCluster(cluster1Pos, 2 + random.nextInt(2), size, clusterSet, random)
        clusterSet.addAll(cluster1Cells)
        emptySet.removeAll(cluster1Cells.toSet())

        // 步骤2
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

        // 步骤3
        if (emptySet.isNotEmpty()) {
            val cluster2Pos = emptySet.random(random)
            val cluster2Cells = expandToCluster(cluster2Pos, 2 + random.nextInt(2), size, clusterSet, random)
            clusterSet.addAll(cluster2Cells)
            emptySet.removeAll(cluster2Cells.toSet())
            removeNeighborsFromSets(emptySet, clusterSet, discreteSet, size)
        }

        // 步骤4
        val clusterCount = countClusters(clusterSet)

        // 步骤5
        val discreteTarget = clusterCount * 5
        repeat(discreteTarget) {
            if (emptySet.isEmpty()) return@repeat
            val pos = emptySet.random(random)
            discreteSet.add(pos)
            emptySet.remove(pos)
            removeNeighborsFromSets(emptySet, clusterSet, discreteSet, size)
        }

        // 步骤6-9
        val urbanTarget = (m * 0.10 - 8).toInt().coerceAtLeast(0)
        var loopCount = 0

        while (true) {
            loopCount++
            val totalUrbanCount = clusterSet.size + discreteSet.size
            if (totalUrbanCount >= urbanTarget || emptySet.isEmpty()) break

            if (emptySet.isNotEmpty()) {
                val newClusterPos = emptySet.random(random)
                val allClusterCells = mutableListOf(newClusterPos)
                allClusterCells.addAll(getNeighborCoords(newClusterPos.first, newClusterPos.second, size).filter { it in emptySet })

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
                    removeNeighborsFromSets(emptySet, clusterSet, discreteSet, size)
                }
            }

            if (emptySet.isNotEmpty()) {
                val newDiscretePos = emptySet.random(random)
                discreteSet.add(newDiscretePos)
                emptySet.remove(newDiscretePos)
                removeNeighborsFromSets(emptySet, clusterSet, discreteSet, size)
            }

            if (loopCount > 500) break
        }

        return Pair(clusterSet, discreteSet)
    }

    private fun removeNeighborsFromSets(
        emptySet: MutableSet<Pair<Int, Int>>,
        clusterSet: Set<Pair<Int, Int>>,
        discreteSet: Set<Pair<Int, Int>>,
        size: Int
    ) {
        for (cell in clusterSet) {
            val neighbors = getNeighborCoords(cell.first, cell.second, size)
            emptySet.removeAll(neighbors)
        }
        for (cell in discreteSet) {
            val neighbors = getNeighborCoords(cell.first, cell.second, size)
            emptySet.removeAll(neighbors)
        }
    }

    private fun countClusters(clusterSet: Set<Pair<Int, Int>>): Int {
        if (clusterSet.isEmpty()) return 0

        val visited = mutableSetOf<Pair<Int, Int>>()
        var clusterCount = 0

        for (pos in clusterSet) {
            if (pos in visited) continue
            val queue = ArrayDeque<Pair<Int, Int>>()
            queue.add(pos)

            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                if (current in visited) continue
                if (current !in clusterSet) continue
                visited.add(current)

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

    private fun expandToCluster(
        center: Pair<Int, Int>,
        clusterSize: Int,
        size: Int,
        occupied: Set<Pair<Int, Int>>,
        random: Random
    ): MutableList<Pair<Int, Int>> {
        val cluster = mutableListOf(center)
        val clusterSet = mutableSetOf(center)

        val neighbors = getNeighborCoords(center.first, center.second, size)
            .filter { it !in occupied }
        val shuffledNeighbors = neighbors.shuffled(random)

        for (neighbor in shuffledNeighbors) {
            if (cluster.size >= clusterSize) break
            if (neighbor !in clusterSet && neighbor !in occupied) {
                cluster.add(neighbor)
                clusterSet.add(neighbor)
            }
        }

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

        if (cluster.size < 2 && shuffledNeighbors.isNotEmpty()) {
            cluster.add(shuffledNeighbors[0])
        }

        return cluster
    }

    private fun hexDistance(p1: Pair<Int, Int>, p2: Pair<Int, Int>): Int {
        val x1 = p1.first
        val z1 = p1.second - (p1.first - (p1.first and 1)) / 2
        val y1 = -x1 - z1
        val x2 = p2.first
        val z2 = p2.second - (p2.first - (p2.first and 1)) / 2
        val y2 = -x2 - z2
        return maxOf(kotlin.math.abs(x1 - x2), kotlin.math.abs(y1 - y2), kotlin.math.abs(z1 - z2))
    }

    private fun getNeighborCoords(x: Int, y: Int, size: Int): List<Pair<Int, Int>> {
        val offsets = if (x % 2 == 0) {
            arrayOf(Pair(0, -1), Pair(1, -1), Pair(1, 0), Pair(0, 1), Pair(-1, 0), Pair(-1, -1))
        } else {
            arrayOf(Pair(0, -1), Pair(1, 0), Pair(1, 1), Pair(0, 1), Pair(-1, 1), Pair(-1, 0))
        }
        return offsets.map { (dx, dy) -> Pair(x + dx, y + dy) }
            .filter { (nx, ny) -> nx in 0 until size && ny in 0 until size }
    }

    private fun assignUrbanFortifications(
        map: DebugHexMap,
        urbanCells: Set<Pair<Int, Int>>,
        clusterSet: Set<Pair<Int, Int>>,
        random: Random
    ) {
        for (x in 0 until map.width) {
            for (y in 0 until map.height) {
                for (dir in 0..5) {
                    map.setFortification(x, y, dir, FortType.NONE)
                }
            }
        }

        val discreteCells = urbanCells - clusterSet
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

        for (cell in clusterSet) {
            val (x, y) = cell
            for (dir in 0..5) {
                map.setFortification(x, y, dir, FortType.STONEWALL)
            }
        }

        for (cell in clusterSet) {
            val (x, y) = cell
            for (dir in 0..5) {
                val (nx, ny) = map.getNeighborCoord(x, y, dir)
                if (nx !in 0 until map.width || ny !in 0 until map.height) continue
                val neighborPos = Pair(nx, ny)
                if (neighborPos in clusterSet) {
                    map.setFortification(x, y, dir, FortType.NONE)
                }
            }
        }
    }
}
