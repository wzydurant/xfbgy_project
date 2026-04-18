package com.hexwarfare.app.domain.model

/**
 * 阵营枚举
 */
enum class Faction {
    PLAYER,
    ENEMY,
    NEUTRAL
}

/**
 * 兵种等级
 */
enum class PersonnelLevel {
    LOW,
    MEDIUM,
    HIGH
}

/**
 * 体力等级
 */
enum class StaminaLevel {
    EXHAUSTED,  // 筋疲力尽
    TIRED,      // 疲惫
    NORMAL,     // 正常
    FRESH       // 充沛
}

/**
 * 士气等级
 */
enum class MoraleLevel {
    BROKEN,     // 崩溃
    LOW,        // 低落
    NORMAL,     // 正常
    HIGH        // 高昂
}

/**
 * 纪律等级
 */
enum class DisciplineLevel {
    BROKEN,     // 崩溃
    LOOSE,      // 涣散
    NORMAL,     // 正常
    STRICT      // 严整
}

/**
 * 单位状态
 */
enum class UnitState {
    BATTLE_FORMATION_STRICT,  // 野战严整
    BATTLE_FORMATION_LOOSE,   // 野战散乱
    MARCH,                    // 行军
    REST,                     // 休整
    DEFENSE,                  // 守备
    TRANSPORT,                // 运输
    RAID                      // 掠袭
}

/**
 * 单位安全状态
 */
enum class UnitSafety {
    SAFE,
    DANGGER,
    CRITICAL
}

/**
 * 单位数据类
 */
data class Unit(
    val id: String,
    val coord: HexCoord,
    val faction: Faction,
    val personnelLevel: PersonnelLevel,
    val staminaLevel: StaminaLevel,
    val moraleLevel: MoraleLevel,
    val disciplineLevel: DisciplineLevel,
    val state: UnitState,
    val safety: UnitSafety
)
