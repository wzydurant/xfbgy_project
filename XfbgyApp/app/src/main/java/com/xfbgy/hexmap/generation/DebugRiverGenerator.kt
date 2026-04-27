package com.xfbgy.hexmap.generation

import android.util.Log
import com.xfbgy.hexmap.data.DebugHexMap
import kotlin.random.Random

/**
 * 调试用河流生成器
 *
 * 实现河流生成算法：
 * 1. 选择地图边缘的格子作为河流入口（支持横向/纵向河流）
 *    - 顶部入口：1号边(顶边)，顺时针
 *    - 底部入口：4号边(底边)，顺时针
 *    - 左侧入口：4号边(底边)，逆时针
 *    - 右侧入口：1号边(顶边)，逆时针
 * 2. 按固定概率选择出口边（排除闭环边和对边）
 *    - 初始区域（前两行/列）特殊概率：位置2→0.5, 3→0.5
 * 3. 标记从入口到出口沿流动方向的所有边为河流
 * 4. 找到出口边的邻居，切换流动方向
 * 5. 以邻居的对应边为入口，重复步骤2~4
 * 6. 停止条件：触碰地图边界、无候选出口、或概率结束（长度>10时P=0.3+0.02*(n-10)）
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

    /**
     * 河流类型
     * VERTICAL: 纵向河流，从顶部(y=0)或底部(y=Y)进入
     * HORIZONTAL: 横向河流，从左侧(x=0)或右侧(x=X)进入
     */
    enum class RiverType {
        VERTICAL,
        HORIZONTAL
    }

    /**
     * 入口边缘
     */
    enum class EntrySide {
        TOP,       // y=0 顶部
        BOTTOM,    // y=Y 底部
        LEFT,      // x=0 左侧
        RIGHT      // x=X 右侧
    }

    companion object {
        private const val TAG = "RiverGenerator"

        /** 沿流动方向各位置的固定概率：位置1→0.1, 2→0.4, 3→0.4, 4→0.1, 5→0.0 */
        private val POSITION_WEIGHTS = floatArrayOf(0f, 0.1f, 0.4f, 0.4f, 0.1f, 0.0f)

        /** 初始区域概率：位置1→0, 2→0.5, 3→0.5, 4→0, 5→0.0 */
        private val INITIAL_ZONE_WEIGHTS = floatArrayOf(0f, 0f, 0.5f, 0.5f, 0f, 0.0f)

        /** 概率结束的起始长度阈值 */
        private const val PROB_TERMINATION_THRESHOLD = 10

        /** 概率结束基础概率 */
        private const val PROB_TERMINATION_BASE = 0.3

        /** 每增加1长度的概率增量 */
        private const val PROB_TERMINATION_INCREMENT = 0.02
    }

    /**
     * 生成单条河流（兼容旧接口，从顶部进入的纵向河流）
     * @param map 目标地图
     * @param seed 随机种子（可选，覆盖构造函数的seed）
     */
    fun generate(
        map: DebugHexMap,
        seed: Long? = null
    ) {
        generateRiver(map, RiverType.VERTICAL, seed)
    }

    /**
     * 生成单条指定类型的河流
     * @param map 目标地图
     * @param riverType 河流类型（纵向/横向）
     * @param seed 随机种子（可选，覆盖构造函数的seed）
     */
    fun generateRiver(
        map: DebugHexMap,
        riverType: RiverType,
        seed: Long? = null
    ) {
        val random = Random(seed ?: this.seed)

        // 步骤1：根据河流类型选择入口
        val entryPoint = selectEntryPoint(map, riverType, random)

        Log.d(TAG, "===== 开始生成${if (riverType == RiverType.VERTICAL) "纵向" else "横向"}河流 =====")
        Log.d(TAG, "入口: cell=(${entryPoint.x},${entryPoint.y}), 边=${entryPoint.entryDir + 1}号(idx${entryPoint.entryDir}), 方向=${entryPoint.flowDir.name}, 地图大小=${map.width}x${map.height}")

        var currentX = entryPoint.x
        var currentY = entryPoint.y
        var currentEntry = entryPoint.entryDir
        var currentFlowDir = entryPoint.flowDir
        val entrySide = entryPoint.entrySide
        val visitedCells = mutableSetOf<Pair<Int, Int>>()
        visitedCells.add(Pair(currentX, currentY))

        var maxSteps = map.width * map.height * 2
        var steps = 0
        var riverLength = 1 // 当前河流长度（经过的格子数）

        // 判断是否在初始区域
        val isInInitialZone = isInInitialZone(map, currentX, currentY, entrySide)

        // 在起始格子选择出口边并标记
        val firstCandidates = getCandidateExitEdges(
            map, currentX, currentY, currentEntry, visitedCells, currentFlowDir
        )

        if (firstCandidates.isEmpty()) {
            Log.d(TAG, "起始格子无有效候选出口，河流停止")
            return
        }

        val firstExit = selectExitByProbability(
            firstCandidates, currentEntry, currentFlowDir, random, isInInitialZone
        )
        Log.d(TAG, "步骤1: cell=($currentX,$currentY), 入口=${currentEntry + 1}号(idx$currentEntry), 方向=${currentFlowDir.name}, 出口=${firstExit + 1}号(idx$firstExit), 初始区域=$isInInitialZone")

        val firstEdges = getRiverEdges(currentEntry, firstExit, currentFlowDir)
        for (edge in firstEdges) {
            map.setRiver(currentX, currentY, edge, true)
        }

        // 检查邻居是否在地图内
        val (nx, ny) = map.getNeighborCoord(currentX, currentY, firstExit)
        if (!map.isValidCell(nx, ny)) {
            Log.d(TAG, "邻居($nx,$ny)在地图外，河流停止")
            Log.d(TAG, "===== 河流生成完毕，长度=$riverLength =====")
            return
        }

        // 切换流动方向
        currentFlowDir = toggleFlowDirection(currentFlowDir)
        currentEntry = DebugHexMap.oppositeDirection(firstExit)
        currentX = nx
        currentY = ny
        visitedCells.add(Pair(currentX, currentY))

        // 主循环
        while (steps < maxSteps) {
            steps++
            riverLength++

            // 概率结束检查：当河流长度 > 10 时
            if (riverLength > PROB_TERMINATION_THRESHOLD) {
                val p = (PROB_TERMINATION_BASE + PROB_TERMINATION_INCREMENT * (riverLength - PROB_TERMINATION_THRESHOLD))
                    .coerceAtMost(1.0)
                if (random.nextDouble() < p) {
                    Log.d(TAG, "步骤${steps + 1}: 河流长度=$riverLength, 概率P=${"%.2f".format(p)}, 河流概率结束")
                    break
                }
            }

            // 判断是否在初始区域
            val currentIsInInitialZone = isInInitialZone(map, currentX, currentY, entrySide)

            // 步骤2：获取候选出口边
            val candidates = getCandidateExitEdges(
                map, currentX, currentY, currentEntry, visitedCells, currentFlowDir
            )

            Log.d(TAG, "步骤${steps + 1}: cell=($currentX,$currentY), 入口=${currentEntry + 1}号(idx$currentEntry), 方向=${currentFlowDir.name}, 候选=${candidates.map { "${it + 1}号(idx$it)" }}, 长度=$riverLength, 初始区域=$currentIsInInitialZone")

            if (candidates.isEmpty()) {
                Log.d(TAG, "  → 无有效候选出口，河流停止")
                break
            }

            // 按概率选择出口
            val exitDir = selectExitByProbability(
                candidates, currentEntry, currentFlowDir, random, currentIsInInitialZone
            )

            Log.d(TAG, "  → 选中出口: ${exitDir + 1}号(idx$exitDir)")

            // 步骤3：标记从入口到出口的所有边（沿流动方向）
            val riverEdges = getRiverEdges(currentEntry, exitDir, currentFlowDir)
            val edgeNames = riverEdges.map { "${it + 1}号(idx$it)" }
            Log.d(TAG, "  → 标记河流边: $edgeNames")
            for (edge in riverEdges) {
                map.setRiver(currentX, currentY, edge, true)
            }

            // 步骤4：找到邻居
            val (nextX, nextY) = map.getNeighborCoord(currentX, currentY, exitDir)

            if (!map.isValidCell(nextX, nextY)) {
                Log.d(TAG, "  → 邻居($nextX,$nextY)在地图外，河流停止")
                break
            }

            // 步骤5：切换流动方向
            currentFlowDir = toggleFlowDirection(currentFlowDir)

            // 步骤6：继续
            currentEntry = DebugHexMap.oppositeDirection(exitDir)
            currentX = nextX
            currentY = nextY
            visitedCells.add(Pair(currentX, currentY))
        }

        Log.d(TAG, "===== 河流生成完毕，长度=$riverLength =====")
    }

    /**
     * 生成多条河流，横向和纵向各50%概率
     * @param map 目标地图
     * @param count 河流数量
     * @param seed 随机种子（可选，覆盖构造函数的seed）
     */
    fun generateMultiple(
        map: DebugHexMap,
        count: Int,
        seed: Long? = null
    ) {
        val random = Random(seed ?: this.seed)
        for (i in 0 until count) {
            val riverType = if (random.nextBoolean()) {
                RiverType.HORIZONTAL
            } else {
                RiverType.VERTICAL
            }
            Log.d(TAG, "--- 生成第${i + 1}条河流，类型=${if (riverType == RiverType.HORIZONTAL) "横向" else "纵向"} ---")
            generateRiver(map, riverType, random.nextLong())
        }
    }

    /**
     * 根据河流类型选择入口点
     *
     * 纵向河流：从顶部(y=0)或底部(y=Y)进入
     *   - 顶部入口：1号边(idx0)，顺时针
     *   - 底部入口：4号边(idx3)，顺时针
     * 横向河流：从左侧(x=0)或右侧(x=X)进入
     *   - 左侧入口：4号边(idx3)，逆时针
     *   - 右侧入口：1号边(idx0)，逆时针
     */
    private fun selectEntryPoint(
        map: DebugHexMap,
        riverType: RiverType,
        random: Random
    ): EntryPoint {
        return when (riverType) {
            RiverType.VERTICAL -> {
                // 随机选择顶部或底部
                if (random.nextBoolean()) {
                    // 从顶部进入：1号边(顶边)，顺时针
                    val startX = random.nextInt(map.width)
                    val startY = 0
                    EntryPoint(startX, startY, 0, FlowDirection.CLOCKWISE, EntrySide.TOP)
                } else {
                    // 从底部进入：4号边(底边)，顺时针
                    val startX = random.nextInt(map.width)
                    val startY = map.height - 1
                    EntryPoint(startX, startY, 3, FlowDirection.CLOCKWISE, EntrySide.BOTTOM)
                }
            }
            RiverType.HORIZONTAL -> {
                // 随机选择左侧或右侧
                if (random.nextBoolean()) {
                    // 从左侧进入：4号边(底边)，逆时针
                    val startX = 0
                    val startY = random.nextInt(map.height)
                    EntryPoint(startX, startY, 3, FlowDirection.COUNTER_CLOCKWISE, EntrySide.LEFT)
                } else {
                    // 从右侧进入：1号边(顶边)，逆时针
                    val startX = map.width - 1
                    val startY = random.nextInt(map.height)
                    EntryPoint(startX, startY, 0, FlowDirection.COUNTER_CLOCKWISE, EntrySide.RIGHT)
                }
            }
        }
    }

    /**
     * 判断当前格子是否在初始区域内
     *
     * 纵向河流：入口侧的前两行
     * 横向河流：入口侧的前两列
     */
    private fun isInInitialZone(
        map: DebugHexMap,
        x: Int,
        y: Int,
        entrySide: EntrySide
    ): Boolean {
        return when (entrySide) {
            EntrySide.TOP -> y <= 1
            EntrySide.BOTTOM -> y >= map.height - 2
            EntrySide.LEFT -> x <= 1
            EntrySide.RIGHT -> x >= map.width - 2
        }
    }

    private data class EntryPoint(
        val x: Int,
        val y: Int,
        val entryDir: Int,
        val flowDir: FlowDirection,
        val entrySide: EntrySide
    )

    /**
     * 切换流动方向
     */
    private fun toggleFlowDirection(dir: FlowDirection): FlowDirection {
        return if (dir == FlowDirection.CLOCKWISE) {
            FlowDirection.COUNTER_CLOCKWISE
        } else {
            FlowDirection.CLOCKWISE
        }
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
     * 根据概率选择出口边
     *
     * 正常区域：沿流动方向位置1→0.1, 2→0.4, 3→0.4, 4→0.1
     * 初始区域：沿流动方向位置2→0.5, 3→0.5，其他→0
     * 归一化后加权随机选择
     *
     * @param isInInitialZone 是否在初始区域（入口侧前两行/列）
     */
    private fun selectExitByProbability(
        candidates: List<Int>,
        entryDir: Int,
        flowDirection: FlowDirection,
        random: Random,
        isInInitialZone: Boolean
    ): Int {
        if (candidates.size == 1) return candidates[0]

        // 选择概率表
        val weights = if (isInInitialZone) INITIAL_ZONE_WEIGHTS else POSITION_WEIGHTS

        // 计算每条候选边沿流动方向的位置和权重
        val candidateWeights = candidates.map { dir ->
            val position = getPositionAlongFlow(entryDir, dir, flowDirection)
            weights[position]
        }

        // 过滤掉权重为0的候选边
        val filteredCandidates = candidates.filterIndexed { i, _ -> candidateWeights[i] > 0f }
        val filteredWeights = candidateWeights.filter { it > 0f }

        if (filteredCandidates.isEmpty()) {
            // 所有候选边权重为0（不应发生），回退到正常概率
            return selectExitByProbability(candidates, entryDir, flowDirection, random, false)
        }

        if (filteredCandidates.size == 1) return filteredCandidates[0]

        // 归一化
        val totalWeight = filteredWeights.sum()
        val normalizedWeights = if (totalWeight > 0f) {
            filteredWeights.map { it / totalWeight }
        } else {
            filteredWeights.map { 1f / filteredWeights.size }
        }

        Log.d(TAG, "  概率选择(${if (isInInitialZone) "初始区域" else "正常区域"}): ${filteredCandidates.indices.map { i -> "${filteredCandidates[i] + 1}号(pos=${getPositionAlongFlow(entryDir, filteredCandidates[i], flowDirection)}, w=${"%.3f".format(normalizedWeights[i])})" }}")

        // 加权随机选择
        val roll = random.nextDouble()
        var cumulative = 0.0
        for (i in filteredCandidates.indices) {
            cumulative += normalizedWeights[i]
            if (roll <= cumulative) {
                return filteredCandidates[i]
            }
        }

        return filteredCandidates.last()
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
