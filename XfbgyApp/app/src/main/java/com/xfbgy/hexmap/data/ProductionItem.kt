package com.xfbgy.hexmap.data

/**
 * 装备生产序列项
 *
 * @param equipType 装备类型
 * @param totalRounds 所需总回合数（= 装备成本）
 * @param progressRounds 已完成回合数（0 ~ totalRounds-1）
 */
data class ProductionItem(
    val equipType: EquipmentType,
    val totalRounds: Int = equipType.cost,
    var progressRounds: Int = 0
)
