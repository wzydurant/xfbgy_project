package com.xfbgy.hexmap.data

/**
 * 资源种类枚举
 *
 * - 粮食（不储存，每回合清零）
 * - 人力
 * - 装备（原始点数）
 * - 牛
 * - 马
 */
enum class ResourceKind(
    val chineseName: String,
    val icon: String
) {
    FOOD("粮食", "🌾"),
    MANPOWER("人力", "👷"),
    EQUIPMENT("装备点", "⚔"),
    CATTLE("牛", "🐄"),
    HORSE("马", "🐎")
}
