package com.xfbgy.hexmap.ui

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
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
    private var currentMap: DebugHexMap? = null

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
     * 创建全屏布局：地图占满屏幕 + 底部信息栏 + 点击信息面板
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
            onCellSelected = { x, y -> updateCellInfo(x, y) }
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
            }
            text = "点击地图格子查看详细信息"
            visibility = View.GONE
        }
        rootLayout.addView(cellInfoPanel)

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
    }

    private fun updateCellInfo(x: Int, y: Int) {
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
