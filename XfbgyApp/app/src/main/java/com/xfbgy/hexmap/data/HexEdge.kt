package com.xfbgy.hexmap.data

/**
 * 六角格边缘属性数据类
 * 
 * 边的编号：顺时针 0-5
 * - 0: 上 (顶部边)
 * - 1: 右上
 * - 2: 右下
 * - 3: 下 (底部边)
 * - 4: 左下
 * - 5: 左上
 *
 * 每条边独立存储以下属性：
 * @param hasRiver 是否有河流
 * @param fortification 防御工事等级
 * @param movementPenalty 移动破坏值（叠加计算）
 * @param defenseBonus 防御优势等级（叠加计算）
 * @param defenseConditions 条件防御优势列表
 * @param attackBonus 进攻优势等级（叠加计算）
 * @param attackConditions 条件进攻优势列表
 */
data class HexEdge(
    var hasRiver: Boolean = false,
    var fortification: FortType = FortType.NONE,
    var movementPenalty: Int = 0,
    var defenseBonus: Int = 0,
    var defenseConditions: MutableList<String> = mutableListOf(),
    var attackBonus: Int = 0,
    var attackConditions: MutableList<String> = mutableListOf()
) {
    /**
     * 重置边的所有属性
     */
    fun reset() {
        hasRiver = false
        fortification = FortType.NONE
        movementPenalty = 0
        defenseBonus = 0
        defenseConditions.clear()
        attackBonus = 0
        attackConditions.clear()
    }

    /**
     * 应用河流效果
     */
    fun applyRiverEffect() {
        if (hasRiver) {
            movementPenalty += 1
            defenseBonus += 1
        }
    }

    /**
     * 应用防御工事效果
     */
    fun applyFortBonus() {
        when (fortification) {
            FortType.FENCE -> {
                defenseBonus += 1
            }
            FortType.EARTHWALL -> {
                movementPenalty += 1
                defenseBonus += 2
            }
            FortType.STONEWALL -> {
                movementPenalty += 1
                defenseBonus += 1
            }
            FortType.NONE -> {
                // 无工事，不叠加效果
            }
        }
    }
}
