package com.xfbgy.hexmap.generation

import android.util.Log
import com.xfbgy.hexmap.data.DebugHexMap
import kotlin.random.Random

/**
 * 调试用河流生成器
 *
 * 实现河流生成算法：
 * 1. 选择第一行任意格子的1号边（顶边）作为河流入口
 * 2. 按固定概率选择出口边（排除闭环边和对边）
 * 3. 标记从入口到出口沿流动方向的所有边为河流
 * 4. 找到出口边的邻居，切换流动方向
 * 5. 以邻居的对应边为入口，重复步骤2~4
 * 6. 触碰地图边界时停止
 *
 * @param seed 随机种子
 */
class DebugRiverGenerator(
    private var seed: Long = System.currentTimeMillis()
) {

    enum class FlowDirection {
        CLOCKWISE,
        COUNTER_CLOCKWISE
    }

    companion object {
        private const val TAG = "RiverGenerator"

        /** 沿流动方向各位置的固定概率：位置1→0.1, 2→0.4, 3→0.4, 4→0.1, 5→0.0 */
        private val POSITION_WEIGHTS = floatArrayOf(0f, 0.1f, 0.4f, 0.4f, 0.1f, 0.0f)
    }

    /**
     * 生成河流
     * @param map 目标地图
     * @param seed 随机种子（可选，覆盖构造函数的seed）
     */
    fun generate(
        map: DebugHexMap,
        seed: Long? = null
    ) {
        val random = Random(seed ?: this.seed)

        // 步骤1：选择第一行任意格子的1号边作为入口
        val startX = random.nextInt(map.width)
        val startY = 0
        val entryDir = 0 // 1号边 = direction 0（顶边）

        Log.d(TAG, "===== 开始生成河流 =====")
        Log.d(TAG, "入口: cell=($startX,$startY), 边=${entryDir + 1}号(idx$entryDir), 地图大小=${map.width}x${map.height}")

        var currentX = startX
        var currentY = startY
        var currentEntry = entryDir
        var flowDir = FlowDirection.CLOCKWISE
        val visitedCells = mutableSetOf<Pair<Int, Int>>()
        visitedCells.add(Pair(currentX, currentY))

        var maxSteps = map.width * map.height * 2
        var steps = 0

        while (steps < maxSteps) {
            steps++

            // 步骤2：获取候选出口边（排除入口边、闭环边、对边）
            val candidates = getCandidateExitEdges(
                map, currentX, currentY, currentEntry, visitedCells, flowDir
            )

            Log.d(TAG, "步骤$steps: cell=($currentX,$currentY), 入口=${currentEntry + 1}号(idx$currentEntry), 方向=${flowDir.name}, 候选=${candidates.map { "${it + 1}号(idx$it)" }}")

            if (candidates.isEmpty()) {
                Log.d(TAG, "  → 无有效候选出口，河流停止")
                break
            }

            // 按固定概率选择出口
            val exitDir = selectExitByProbability(
                candidates, currentEntry, flowDir, random
            )

            Log.d(TAG, "  → 选中出口: ${exitDir + 1}号(idx$exitDir)")

            // 步骤3：标记从入口到出口的所有边（沿流动方向）
            val riverEdges = getRiverEdges(currentEntry, exitDir, flowDir)
            val edgeNames = riverEdges.map { "${it + 1}号(idx$it)" }
            Log.d(TAG, "  → 标记河流边: $edgeNames")
            for (edge in riverEdges) {
                map.setRiver(currentX, currentY, edge, true)
            }

            // 步骤4：找到邻居
            val (nx, ny) = map.getNeighborCoord(currentX, currentY, exitDir)

            if (!map.isValidCell(nx, ny)) {
                Log.d(TAG, "  → 邻居($nx,$ny)在地图外，河流停止")
                break
            }

            // 邻居的入口边
            val neighborEntry = DebugHexMap.oppositeDirection(exitDir)
            Log.d(TAG, "  → 邻居=($nx,$ny), 邻居入口=${neighborEntry + 1}号(idx$neighborEntry)")

            // 步骤5：切换流动方向
            flowDir = if (flowDir == FlowDirection.CLOCKWISE) {
                FlowDirection.COUNTER_CLOCKWISE
            } else {
                FlowDirection.CLOCKWISE
            }
            Log.d(TAG, "  → 切换方向为: ${flowDir.name}")

            // 步骤6：继续
            currentX = nx
            currentY = ny
            currentEntry = neighborEntry
            visitedCells.add(Pair(currentX, currentY))
        }

        Log.d(TAG, "===== 河流生成完毕 =====")
    }

    /**
     * 获取从入口边到出口边沿流动方向的所有边
     *
     * 顺时针流动：从entryDir开始，编号递增，直到exitDir
     * 逆时针流动：从entryDir开始，编号递减，直到exitDir
     *
     * @param entryDir 入口边方向(0~5)
     * @param exitDir 出口边方向(0~5)
     * @param flowDirection 流动方向
     * @return 从入口到出口的边列表（包含入口和出口）
     */
    private fun getRiverEdges(
        entryDir: Int,
        exitDir: Int,
        flowDirection: FlowDirection
    ): List<Int> {
        val edges = mutableListOf<Int>()
        var current = entryDir
        while (true) {
            edges.add(current)
            if (current == exitDir) break
            current = when (flowDirection) {
                FlowDirection.CLOCKWISE -> (current + 1) % 6
                FlowDirection.COUNTER_CLOCKWISE -> (current + 5) % 6
            }
        }
        return edges
    }

    /**
     * 获取候选出口边（排除入口边、闭环边、对边）
     *
     * @param flowDirection 当前流动方向，用于排除对边（第5条边概率为0）
     */
    private fun getCandidateExitEdges(
        map: DebugHexMap,
        x: Int,
        y: Int,
        entryDir: Int,
        visitedCells: Set<Pair<Int, Int>>,
        flowDirection: FlowDirection
    ): List<Int> {
        val candidates = mutableListOf<Int>()
        val oppositeDir = (entryDir + 3) % 6 // 对边，概率为0，始终排除

        for (dir in 0..5) {
            if (dir == entryDir) continue // 排除入口边
            if (dir == oppositeDir) continue // 排除对边（第5条边概率为0）

            val (nx, ny) = map.getNeighborCoord(x, y, dir)

            // 如果邻居在地图外，该边仍可作为出口（河流流出地图后停止）
            // 但如果邻居已访问，则排除（避免闭环）
            if (map.isValidCell(nx, ny)) {
                if (Pair(nx, ny) in visitedCells) {
                    continue // 闭环，排除
                }
            }

            candidates.add(dir)
        }

        return candidates
    }

    /**
     * 根据固定概率选择出口边
     *
     * 沿流动方向位置1→0.1, 2→0.4, 3→0.4, 4→0.1
     * 归一化后加权随机选择
     */
    private fun selectExitByProbability(
        candidates: List<Int>,
        entryDir: Int,
        flowDirection: FlowDirection,
        random: Random
    ): Int {
        if (candidates.size == 1) return candidates[0]

        // 计算每条候选边沿流动方向的位置和权重
        val weights = candidates.map { dir ->
            val position = getPositionAlongFlow(entryDir, dir, flowDirection)
            POSITION_WEIGHTS[position]
        }

        // 归一化
        val totalWeight = weights.sum()
        val normalizedWeights = if (totalWeight > 0f) {
            weights.map { it / totalWeight }
        } else {
            // 不应发生，但做保护
            weights.map { 1f / weights.size }
        }

        Log.d(TAG, "  概率选择: ${candidates.indices.map { i -> "${candidates[i] + 1}号(pos=${getPositionAlongFlow(entryDir, candidates[i], flowDirection)}, w=${"%.3f".format(normalizedWeights[i])})" }}")

        // 加权随机选择
        val roll = random.nextDouble()
        var cumulative = 0.0
        for (i in candidates.indices) {
            cumulative += normalizedWeights[i]
            if (roll <= cumulative) {
                return candidates[i]
            }
        }

        return candidates.last()
    }

    /**
     * 计算候选边沿流动方向的位置（1~5）
     *
     * 顺时针：(candidate - entry + 6) % 6
     * 逆时针：(entry - candidate + 6) % 6
     */
    private fun getPositionAlongFlow(
        entryDir: Int,
        candidateDir: Int,
        flowDirection: FlowDirection
    ): Int {
        return when (flowDirection) {
            FlowDirection.CLOCKWISE -> (candidateDir - entryDir + 6) % 6
            FlowDirection.COUNTER_CLOCKWISE -> (entryDir - candidateDir + 6) % 6
        }
    }
}
