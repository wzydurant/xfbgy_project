package com.xfbgy.hexmap.game

import android.util.Log
import com.xfbgy.hexmap.data.DebugHexMap
import com.xfbgy.hexmap.data.EquipmentType
import com.xfbgy.hexmap.data.ProductionItem
import com.xfbgy.hexmap.data.ResourceKind
import com.xfbgy.hexmap.data.ResourcePoint
import com.xfbgy.hexmap.data.ResourcePointType

/**
 * 资源管理器
 *
 * 职责：
 * - 扫描地图中所有资源点
 * - 回合结算：产出/消耗/周期产出/粮食分配/装备生产
 * - 统计玩家总资源
 * - 不包含任何UI逻辑
 */
class ResourceManager(
    private val map: DebugHexMap
) {
    companion object {
        private const val TAG = "ResourceManager"

        /** 产出规则：每回合粮食产出 */
        private val FOOD_PRODUCTION = mapOf(
            ResourcePointType.VILLAGE to 5,
            ResourcePointType.TOWN to 2,
            ResourcePointType.CITY to 0,
            ResourcePointType.RANCH to 0
        )

        /** 周期产出：周期 */
        private val PERIODIC_INTERVAL = mapOf(
            ResourcePointType.VILLAGE to 5,
            ResourcePointType.TOWN to 3,
            ResourcePointType.RANCH to 5
        )
    }

    /** 所有资源点列表 */
    private val allPoints = mutableListOf<ResourcePoint>()

    /** 资源点位置映射：(x, y) -> ResourcePoint */
    private val pointMap = mutableMapOf<Pair<Int, Int>, ResourcePoint>()

    /** 全局粮食池：阵营ID -> 当前粮食数量（每回合结算时产出汇入，消耗后清零） */
    private val playerFoodPool = mutableMapOf<Int, Int>()

    /** 本回合粮食产出：阵营ID -> 产出量（Phase2写入） */
    private val roundFoodProduced = mutableMapOf<Int, Int>()

    /** 本回合粮食消耗：阵营ID -> 消耗量（Phase4写入） */
    private val roundFoodConsumed = mutableMapOf<Int, Int>()

    /** 上回合粮食统计：阵营ID -> (产出, 消耗, 结余) */
    private val lastRoundFoodSummary = mutableMapOf<Int, Triple<Int, Int, Int>>()

    /** 回合结算事件监听器 */
    var onRoundSettled: (() -> Unit)? = null

    /**
     * 扫描地图，收集所有资源点
     * 在地图生成完成后调用一次
     */
    fun scanMap() {
        allPoints.clear()
        pointMap.clear()

        for (x in 0 until map.width) {
            for (y in 0 until map.height) {
                val rp = map.cells[x][y].resourcePoint
                if (rp != null) {
                    allPoints.add(rp)
                    pointMap[Pair(x, y)] = rp
                }
            }
        }
        Log.d(TAG, "扫描完成，共${allPoints.size}个资源点")
    }

    /**
     * 获取所有资源点
     */
    fun getAllPoints(): List<ResourcePoint> = allPoints.toList()

    /**
     * 获取指定阵营的资源点
     */
    fun getPointsByFaction(faction: Int): List<ResourcePoint> {
        return allPoints.filter { it.faction == faction }
    }

    /**
     * 查询某格属于哪个资源点
     */
    fun getPointAt(x: Int, y: Int): ResourcePoint? = pointMap[Pair(x, y)]

    /**
     * 获取指定玩家的总资源统计
     *
     * @return 资源种类 -> 总数（FOOD为全局粮食池当前值）
     */
    fun getPlayerResourceTotals(faction: Int): Map<ResourceKind, Int> {
        val totals = mutableMapOf(
            ResourceKind.FOOD to (playerFoodPool[faction] ?: 0),
            ResourceKind.MANPOWER to 0,
            ResourceKind.EQUIPMENT to 0,
            ResourceKind.CATTLE to 0,
            ResourceKind.HORSE to 0
        )

        for (rp in getPointsByFaction(faction)) {
            if (rp.isSuppressed) continue
            totals[ResourceKind.MANPOWER] = totals[ResourceKind.MANPOWER]!! + rp.manpowerStock
            totals[ResourceKind.CATTLE] = totals[ResourceKind.CATTLE]!! + rp.cattleStock
            totals[ResourceKind.HORSE] = totals[ResourceKind.HORSE]!! + rp.horseStock
            val totalEquip = rp.equipStock.values.sum()
            totals[ResourceKind.EQUIPMENT] = totals[ResourceKind.EQUIPMENT]!! + totalEquip
        }

        return totals
    }

    /**
     * 获取指定玩家的全局粮食池数量
     */
    fun getPlayerFoodPool(faction: Int): Int = playerFoodPool[faction] ?: 0

    /**
     * 获取指定玩家的上回合粮食统计
     * @return Triple(产出, 消耗, 结余)
     */
    fun getLastRoundFoodSummary(faction: Int): Triple<Int, Int, Int> =
        lastRoundFoodSummary[faction] ?: Triple(0, 0, 0)

    /**
     * 获取指定玩家的总装备统计
     */
    fun getPlayerEquipTotals(faction: Int): Map<EquipmentType, Int> {
        val totals = EquipmentType.entries.associateWith { 0 }.toMutableMap()
        for (rp in getPointsByFaction(faction)) {
            if (rp.isSuppressed) continue
            for ((type, count) in rp.equipStock) {
                totals[type] = totals[type]!! + count
            }
        }
        return totals
    }

    /**
     * 玩家回合结算入口
     *
     * 每个玩家结束回合时调用，只处理该阵营的资源。
     * 严格按顺序执行Phase：
     * Phase 1: 压制计算（暂不实现，需单位模块）
     * Phase 2: 粮食产出汇总 → 汇入全局粮食池
     * Phase 3: 粮食分配（暂不实现，跳过 — 等第5章运输逻辑）
     * Phase 4: 消耗点产出（从全局粮食池消耗）
     * Phase 5: 生产者周期产出
     * Phase 6: 装备生产推进
     * Phase 7: 粮食清零（暂不实现，跳过 — 全局粮食池在结算末清零）
     * Phase 8: 事件广播
     *
     * @param roundNumber 当前回合数
     * @param faction 阵营ID（只结算该阵营的资源点）
     */
    fun processRoundEnd(roundNumber: Int, faction: Int) {
        Log.d(TAG, "===== 回合$roundNumber 阵营$faction 资源结算开始 =====")

        // Phase 1: 压制计算（暂不实现，等对接单位模块）
        // calculateSuppression()

        // Phase 2: 粮食产出汇总 → 全局粮食池
        phase2_FoodProduction(faction)
        Log.d(TAG, "Phase2: 粮食产出汇总完成，阵营$faction 粮食池: ${playerFoodPool[faction]}")

        // Phase 3: 粮食分配（跳过 — 等第5章运输逻辑实现）

        // Phase 4: 消耗点产出（都市/马场从全局粮食池消耗）
        phase4_ConsumerProduction(roundNumber, faction)

        // Phase 5: 生产者周期产出（村庄/城镇）
        phase5_PeriodicProduction(roundNumber, faction)

        // Phase 6: 装备生产推进
        phase6_AdvanceProduction(faction)

        // Phase 7: 全局粮食池清零（清零前记录统计）
        phase7_ClearGlobalFoodPool(roundNumber, faction)

        // Phase 8: 事件广播
        onRoundSettled?.invoke()

        Log.d(TAG, "===== 回合$roundNumber 阵营$faction 资源结算完成 =====")
    }

    // ==================== Phase 2: 粮食产出汇总（全局粮食池） ====================

    /**
     * 遍历指定阵营未压制的村庄/城镇，计算粮食产出 → 汇入全局粮食池
     */
    private fun phase2_FoodProduction(faction: Int) {
        // 仅重置本阵营的追踪数据
        roundFoodProduced.remove(faction)
        roundFoodConsumed.remove(faction)

        for (rp in allPoints) {
            if (rp.isSuppressed) continue
            if (rp.faction != faction) continue  // 只处理指定阵营
            if (rp.type != ResourcePointType.VILLAGE && rp.type != ResourcePointType.TOWN) continue

            val foodOutput = FOOD_PRODUCTION[rp.type] ?: 0
            if (foodOutput > 0) {
                playerFoodPool[faction] = (playerFoodPool[faction] ?: 0) + foodOutput
                roundFoodProduced[faction] = (roundFoodProduced[faction] ?: 0) + foodOutput
                Log.d(TAG, "Phase2: ${rp.type.chineseName}(阵营$faction) → 全局粮食池 +$foodOutput")
            }
        }
    }

    // ==================== Phase 3: 粮食分配（跳过 — 等第5章运输逻辑） ====================

    // 粮食分配逻辑暂不实现，当前使用全局粮食池替代

    // ==================== Phase 7: 全局粮食池清零 ====================

    /**
     * 结算完成后，记录指定阵营的粮食统计
     *
     * TODO: 实现## 5. 粮食供给与运输逻辑后，结算末清空全局粮食池
     * 当前暂不清空，保留粮食数据供展示
     */
    private fun phase7_ClearGlobalFoodPool(roundNumber: Int, faction: Int) {
        val produced = roundFoodProduced[faction] ?: 0
        val consumed = roundFoodConsumed[faction] ?: 0
        val remaining = playerFoodPool[faction] ?: 0

        lastRoundFoodSummary[faction] = Triple(produced, consumed, remaining)

        Log.d(TAG, "Phase7: 阵营$faction 粮食 产出=$produced 消耗=$consumed 结余=$remaining (暂不清空)")

        // TODO: 实现第5章粮食运输逻辑后，取消下面注释清空粮食池
        // playerFoodPool[faction] = 0
    }

    // ==================== Phase 4: 消耗点产出（从全局粮食池消耗） ====================

    /**
     * 都市/马场：从全局粮食池消耗粮食，然后产出
     * 只处理指定阵营的资源点
     */
    private fun phase4_ConsumerProduction(roundNumber: Int, faction: Int) {
        for (rp in allPoints) {
            if (rp.isSuppressed) continue
            if (rp.faction != faction) continue

            when (rp.type) {
                ResourcePointType.CITY -> {
                    // 都市：全局粮食池 >= 1 → 消耗1粮食，产出人力+1, 装备点+1
                    val pool = playerFoodPool[rp.faction] ?: 0
                    if (pool >= 1) {
                        playerFoodPool[rp.faction] = pool - 1
                        roundFoodConsumed[rp.faction] = (roundFoodConsumed[rp.faction] ?: 0) + 1
                        rp.manpowerStock += 1
                        // 装备点直接驱动生产推进，在Phase6中处理
                        rp.hasEquipPointThisRound = true
                        Log.d(TAG, "Phase4: 都市(阵营${rp.faction}) 消耗全局粮食1 → 人力+1, 装备点+1")
                    } else {
                        Log.d(TAG, "Phase4: 都市(阵营${rp.faction}) 粮食不足，跳过产出")
                    }
                }
                ResourcePointType.RANCH -> {
                    // 马场：每5回合 且 全局粮食池 >= 1 → 消耗1粮食，马+1
                    val interval = PERIODIC_INTERVAL[ResourcePointType.RANCH] ?: 5
                    val pool = playerFoodPool[rp.faction] ?: 0
                    if (roundNumber % interval == 0 && pool >= 1) {
                        playerFoodPool[rp.faction] = pool - 1
                        roundFoodConsumed[rp.faction] = (roundFoodConsumed[rp.faction] ?: 0) + 1
                        rp.horseStock += 1
                        Log.d(TAG, "Phase4: 马场(阵营${rp.faction}) 消耗全局粮食1 → 马+1")
                    }
                }
                else -> { /* 村庄/城镇不在此阶段产出 */ }
            }
        }
    }

    // ==================== Phase 5: 生产者周期产出 ====================

    /**
     * 村庄：每5回合 人力+1, 牛+1
     * 城镇：每3回合 人力+1, 装备点+1
     * 只处理指定阵营的资源点
     */
    private fun phase5_PeriodicProduction(roundNumber: Int, faction: Int) {
        for (rp in allPoints) {
            if (rp.isSuppressed) continue
            if (rp.faction != faction) continue

            when (rp.type) {
                ResourcePointType.VILLAGE -> {
                    val interval = PERIODIC_INTERVAL[ResourcePointType.VILLAGE] ?: 5
                    if (roundNumber % interval == 0) {
                        rp.manpowerStock += 1
                        rp.cattleStock += 1
                        Log.d(TAG, "Phase5: 村庄周期产出 → 人力+1, 牛+1")
                    }
                }
                ResourcePointType.TOWN -> {
                    val interval = PERIODIC_INTERVAL[ResourcePointType.TOWN] ?: 3
                    if (roundNumber % interval == 0) {
                        rp.manpowerStock += 1
                        // 装备点直接驱动生产推进，在Phase6中处理
                        rp.hasEquipPointThisRound = true
                        Log.d(TAG, "Phase5: 城镇周期产出 → 人力+1, 装备点+1")
                    }
                }
                else -> { /* 都市/马场不在此阶段产出 */ }
            }
        }
    }

    // ==================== Phase 6: 装备生产推进 ====================

    /**
     * 城镇/都市：若本回合有装备点 → 推进生产进度
     * 只处理指定阵营的资源点
     */
    private fun phase6_AdvanceProduction(faction: Int) {
        for (rp in allPoints) {
            if (rp.isSuppressed) continue
            if (rp.faction != faction) continue
            if (!rp.hasEquipPointThisRound) continue
            if (rp.type != ResourcePointType.TOWN && rp.type != ResourcePointType.CITY) continue

            advanceProduction(rp)
            rp.hasEquipPointThisRound = false
        }
    }

    /**
     * 推进单个资源点的装备生产进度
     */
    private fun advanceProduction(rp: ResourcePoint) {
        if (rp.productionQueue.isEmpty()) return

        if (rp.currentQueueIndex >= rp.productionQueue.size) {
            // 队列循环
            rp.currentQueueIndex = 0
        }

        val item = rp.productionQueue[rp.currentQueueIndex]
        item.progressRounds++

        Log.d(TAG, "Phase6: ${rp.type.chineseName}生产推进 → ${item.equipType.chineseName} [${item.progressRounds}/${item.totalRounds}]")

        if (item.progressRounds >= item.totalRounds) {
            // 当前项完成，产出装备
            rp.equipStock[item.equipType] = (rp.equipStock[item.equipType] ?: 0) + 1
            item.progressRounds = 0
            rp.currentQueueIndex++

            Log.d(TAG, "Phase6: 生产完成 → ${item.equipType.chineseName}×1")

            // 如果超过队列末尾则循环
            if (rp.currentQueueIndex >= rp.productionQueue.size) {
                rp.currentQueueIndex = 0
            }
        }
    }

    // ==================== 生产序列管理 ====================

    /**
     * 修改生产序列（修改后进度清零）
     */
    fun setProductionQueue(point: ResourcePoint, newQueue: List<EquipmentType>) {
        point.productionQueue.clear()
        for (equipType in newQueue) {
            point.productionQueue.add(ProductionItem(equipType))
        }
        point.currentQueueIndex = 0
    }

}
