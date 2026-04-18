package com.hexwarfare.app.domain.model

/**
 * 武器类型
 */
enum class WeaponType {
    SPEAR,  // 长矛
    SWORD,  // 剑
    BOW     // 弓
}

/**
 * 防御装备类型
 */
enum class DefensiveGearType {
    SHIELD,  // 盾牌
    ARMOR    // 盔甲
}

/**
 * 火炮类型
 */
enum class ArtilleryGear {
    NONE,
    LIGHT,
    HEAVY
}

/**
 * 携带装备
 */
data class CarriedEquipment(
    val weapon: WeaponType? = null,
    val defensiveGear: DefensiveGearType? = null,
    val artillery: ArtilleryGear = ArtilleryGear.NONE
)

/**
 * 装备装备
 */
data class EquippedGear(
    val carried: CarriedEquipment = CarriedEquipment(),
    val extraSupplies: Int = 0
)
