package com.xfbgy.hexmap.generation

import android.util.Log
import com.xfbgy.hexmap.data.DebugHexMap
import com.xfbgy.hexmap.data.FortType
import com.xfbgy.hexmap.data.ResourcePoint
import com.xfbgy.hexmap.data.ResourcePointType
import com.xfbgy.hexmap.data.TerrainType
import kotlin.math.max
import kotlin.random.Random

/**
 * 资源点识别器
 *
 * 根据地形和工事要求自动识别四种资源点：
 * - 村庄: 建筑群 + 全部6边均为栅栏
 * - 城镇: 建筑群 + 全部6边均为土墙
 * - 都市: 建筑群 + 全部6边均为石墙（相邻都市格之间的边除外）
 * - 马场: 平原 + 不与任何建筑群格相邻
 */
class ResourcePointScanner {

    companion object {
        private const val TAG = "ResourcePointScanner"
    }

    /**
     * 扫描地图并识别所有资源点
     *
     * @param map 地图数据
     * @param clusterSet 聚团格子集合（来自建筑群生成算法）
     */
    fun scanMap(map: DebugHexMap, clusterSet: Set<Pair<Int, Int>>) {
        Log.d(TAG, "========== 开始资源点识别 ==========")

        // 清除所有已有的资源点
        for (x in 0 until map.width) {
            for (y in 0 until map.height) {
                map.cells[x][y].resourcePoint = null
            }
        }

        // 收集所有建筑群格子
        val urbanCells = mutableSetOf<Pair<Int, Int>>()
        for (x in 0 until map.width) {
            for (y in 0 until map.height) {
                if (map.cells[x][y].terrain == TerrainType.URBAN) {
                    urbanCells.add(Pair(x, y))
                }
            }
        }

        // Step 1: 识别都市聚团（BFS找相邻石墙建筑群连通分量）
        val cityClusters = findCityClusters(map, urbanCells)
        Log.d(TAG, "都市聚团数: ${cityClusters.size}")
        for ((index, cluster) in cityClusters.withIndex()) {
            Log.d(TAG, "都市聚团$index: ${cluster.map { "(${it.first},${it.second})" }}")
            for (cell in cluster) {
                map.cells[cell.first][cell.second].resourcePoint = ResourcePoint(ResourcePointType.CITY)
            }
        }

        // 收集所有都市格坐标（用于后续排除）
        val cityCellSet = cityClusters.flatten().toSet()

        // Step 2: 识别村庄和城镇
        for (x in 0 until map.width) {
            for (y in 0 until map.height) {
                if (map.cells[x][y].terrain != TerrainType.URBAN) continue
                if (Pair(x, y) in cityCellSet) continue // 已经是都市

                val fortType = getUniformFortType(map, x, y)
                if (fortType == FortType.FENCE) {
                    map.cells[x][y].resourcePoint = ResourcePoint(ResourcePointType.VILLAGE)
                    Log.d(TAG, "村庄: ($x,$y)")
                } else if (fortType == FortType.EARTHWALL) {
                    map.cells[x][y].resourcePoint = ResourcePoint(ResourcePointType.TOWN)
                    Log.d(TAG, "城镇: ($x,$y)")
                }
            }
        }

        // Step 3: 识别马场
        val ranchCount = identifyRanches(map, urbanCells)
        Log.d(TAG, "马场数: $ranchCount")

        // 统计
        var villageCount = 0
        var townCount = 0
        var cityCount = 0
        var ranchCountFinal = 0
        for (x in 0 until map.width) {
            for (y in 0 until map.height) {
                when (map.cells[x][y].resourcePoint?.type) {
                    ResourcePointType.VILLAGE -> villageCount++
                    ResourcePointType.TOWN -> townCount++
                    ResourcePointType.CITY -> cityCount++
                    ResourcePointType.RANCH -> ranchCountFinal++
                    null -> {}
                }
            }
        }
        Log.d(TAG, "资源点识别完成: 村庄=$villageCount, 城镇=$townCount, 都市=$cityCount, 马场=$ranchCountFinal")
        Log.d(TAG, "========== 资源点识别完毕 ==========")
    }

    /**
     * 识别都市聚团
     *
     * 都市候选格 = 建筑群 + 所有非都市邻居边均为石墙
     * 相邻候选格之间用BFS连通形成聚团
     */
    private fun findCityClusters(map: DebugHexMap, urbanCells: Set<Pair<Int, Int>>): List<List<Pair<Int, Int>>> {
        val visited = mutableSetOf<Pair<Int, Int>>()
        val clusters = mutableListOf<List<Pair<Int, Int>>>()

        for (cell in urbanCells) {
            if (cell in visited) continue
            if (!isCityCandidate(map, cell.first, cell.second, urbanCells)) continue

            // BFS扩展
            val cluster = mutableListOf<Pair<Int, Int>>()
            val queue = ArrayDeque<Pair<Int, Int>>()
            queue.add(cell)
            visited.add(cell)

            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                cluster.add(current)

                for (dir in 0..5) {
                    val (nx, ny) = map.getNeighborCoord(current.first, current.second, dir)
                    val neighbor = Pair(nx, ny)
                    if (neighbor in visited) continue
                    if (!map.isValidCell(nx, ny)) continue
                    if (neighbor !in urbanCells) continue
                    if (!isCityCandidate(map, nx, ny, urbanCells)) continue

                    visited.add(neighbor)
                    queue.add(neighbor)
                }
            }
            clusters.add(cluster)
        }
        return clusters
    }

    /**
     * 判断是否是都市候选格
     *
     * 条件：建筑群 + 所有非都市邻居边均为石墙
     */
    private fun isCityCandidate(map: DebugHexMap, x: Int, y: Int, urbanCells: Set<Pair<Int, Int>>): Boolean {
        if (map.cells[x][y].terrain != TerrainType.URBAN) return false

        for (dir in 0..5) {
            val (nx, ny) = map.getNeighborCoord(x, y, dir)
            val neighborInUrban = map.isValidCell(nx, ny) && Pair(nx, ny) in urbanCells

            // 如果邻居不是建筑群，则这条边必须是石墙
            if (!neighborInUrban && map.edges[x][y][dir].fortification != FortType.STONEWALL) {
                return false
            }
        }
        return true
    }

    /**
     * 获取格子统一的工事类型（6条边全部相同）
     * 如果6条边不统一，返回null
     */
    private fun getUniformFortType(map: DebugHexMap, x: Int, y: Int): FortType? {
        val firstFort = map.edges[x][y][0].fortification
        if (firstFort == FortType.NONE) return null

        for (dir in 1..5) {
            if (map.edges[x][y][dir].fortification != firstFort) {
                return null
            }
        }
        return firstFort
    }

    /**
     * 识别马场
     *
     * 条件：平原 + 不与任何建筑群格相邻
     * 数量公式：max(1, floor(width * height / 200))
     */
    private fun identifyRanches(map: DebugHexMap, urbanCells: Set<Pair<Int, Int>>): Int {
        // 收集候选格：平原且不与建筑群相邻
        val candidates = mutableListOf<Pair<Int, Int>>()
        for (x in 0 until map.width) {
            for (y in 0 until map.height) {
                if (map.cells[x][y].terrain != TerrainType.PLAIN) continue
                if (map.cells[x][y].resourcePoint != null) continue

                // 检查是否与建筑群相邻
                var adjacentToUrban = false
                for (dir in 0..5) {
                    val (nx, ny) = map.getNeighborCoord(x, y, dir)
                    if (map.isValidCell(nx, ny) && Pair(nx, ny) in urbanCells) {
                        adjacentToUrban = true
                        break
                    }
                }
                if (!adjacentToUrban) {
                    candidates.add(Pair(x, y))
                }
            }
        }

        // 计算马场数量
        val ranchCount = max(1, map.width * map.height / 200)
        Log.d(TAG, "马场目标数: $ranchCount, 候选格数: ${candidates.size}")

        if (candidates.isEmpty()) return 0

        // 随机均匀采样，间距不少于5格
        val random = Random.Default
        val selected = mutableListOf<Pair<Int, Int>>()
        val shuffled = candidates.shuffled(random)

        for (candidate in shuffled) {
            if (selected.size >= ranchCount) break

            // 检查与已选马场的距离
            var tooClose = false
            for (existing in selected) {
                if (hexDistance(candidate, existing, map.width) < 5) {
                    tooClose = true
                    break
                }
            }
            if (!tooClose) {
                selected.add(candidate)
            }
        }

        // 设置马场
        for (pos in selected) {
            map.cells[pos.first][pos.second].resourcePoint = ResourcePoint(ResourcePointType.RANCH)
            Log.d(TAG, "马场: (${pos.first},${pos.second})")
        }

        return selected.size
    }

    /**
     * 六角格距离（简化版，使用曼哈顿距离近似）
     */
    private fun hexDistance(p1: Pair<Int, Int>, p2: Pair<Int, Int>, mapWidth: Int): Int {
        val x1 = p1.first
        val z1 = p1.second - (p1.first - (p1.first and 1)) / 2
        val y1 = -x1 - z1

        val x2 = p2.first
        val z2 = p2.second - (p2.first - (p2.first and 1)) / 2
        val y2 = -x2 - z2

        return maxOf(
            kotlin.math.abs(x1 - x2),
            kotlin.math.abs(y1 - y2),
            kotlin.math.abs(z1 - z2)
        )
    }
}
