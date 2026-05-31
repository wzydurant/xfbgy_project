package com.xfbgy.hexmap.data

/**
 * 装备类型枚举
 */
enum class EquipmentType(
    val chineseName: String,
    val cost: Int  // 装备点成本
) {
    SWORD("剑", 1),
    SPEAR("枪", 1),
    BOW("弓", 1),
    SHIELD("盾", 1),
    ARMOR("甲", 2),
    CANNON("火炮", 3)
}
