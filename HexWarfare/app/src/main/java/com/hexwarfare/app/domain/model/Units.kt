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
    HIGH;

    fun getPersonnelCount(): Int = when (this) {
        LOW -> 100
        MEDIUM -> 500
        HIGH -> 1000
    }
}

/**
 * 体力等级
 */
enum class StaminaLevel {
    EXHAUSTED,  // 筋疲力尽
    TIRED,      // 疲惫
    NORMAL,     // 正常
    FRESH;      // 充沛

    fun getStaminaValue(): Int = when (this) {
        EXHAUSTED -> 1
        TIRED -> 2
        NORMAL -> 3
        FRESH -> 4
    }

    fun consume(): StaminaLevel = when (this) {
        EXHAUSTED -> EXHAUSTED
        TIRED -> EXHAUSTED
        NORMAL -> TIRED
        FRESH -> NORMAL
    }

    fun recover(): StaminaLevel = when (this) {
        EXHAUSTED -> TIRED
        TIRED -> NORMAL
        NORMAL -> FRESH
        FRESH -> FRESH
    }
}

/**
 * 士气等级
 */
enum class MoraleLevel {
    BROKEN,     // 崩溃
    LOW,        // 低落
    NORMAL,     // 正常
    HIGH;       // 高昂（昂首挺胸）

    fun getMoraleValue(): Int = when (this) {
        BROKEN -> 1
        LOW -> 2
        NORMAL -> 3
        HIGH -> 4
    }
}

/**
 * 纪律等级
 */
enum class DisciplineLevel {
    BROKEN,     // 崩溃
    LOOSE,      // 涣散
    NORMAL,     // 正常
    STRICT;     // 严整

    fun getDisciplineValue(): Int = when (this) {
        BROKEN -> 1
        LOOSE -> 2
        NORMAL -> 3
        STRICT -> 4
    }
}

/**
 * 单位状态
 */
enum class UnitState(val displayName: String) {
    BATTLE_FORMATION_STRICT("野战严整"),   // 仅将军可下令
    BATTLE_FORMATION_LOOSE("野战散乱"),
    MARCH("行军"),
    REST("休整"),
    DEFENSE("守备"),
    TRANSPORT("运输"),
    RAID("掠袭");

    fun isMobile(): Boolean = this in listOf(MARCH, RAID)

    fun getMovePowerBonus(): Int = when (this) {
        BATTLE_FORMATION_STRICT -> 0
        BATTLE_FORMATION_LOOSE -> 0
        MARCH -> 2
        REST -> -1
        DEFENSE -> -2
        TRANSPORT -> -1
        RAID -> 1
    }
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
data class GameUnit(
    val id: String,
    val name: String,
    val coord: HexCoord,
    val faction: Faction,
    val personnelLevel: PersonnelLevel,
    val staminaLevel: StaminaLevel,
    val moraleLevel: MoraleLevel,
    val disciplineLevel: DisciplineLevel,
    val state: UnitState,
    val safety: UnitSafety,
    val baseMovePower: Int = 5,
    val currentMovePower: Int = 5,
    val consecutiveDefenseTurns: Int = 0,
    val isGeneralUnit: Boolean = false, // 是否是本队（将军/参谋长直属）
    val carriedEquipment: CarriedEquipment = CarriedEquipment()
) {
    /**
     * 获取当前移动力
     */
    fun getActualMovePower(): Int {
        return (baseMovePower + state.getMovePowerBonus()).coerceAtLeast(0)
    }

    /**
     * 检查是否在ZOC内（简化实现）
     */
    fun isUnderZOC(allUnits: List<GameUnit>): Boolean {
        return allUnits.any { other ->
            other.faction != this.faction &&
            other.coord.distanceTo(this.coord) <= 2 &&
            other.state.isMobile()
        }
    }

    /**
     * 获取携带装备数量
     */
    fun getEquipmentCount(): Int {
        var count = 0
        if (carriedEquipment.weapon != null) count++
        if (carriedEquipment.defensiveGear != null) count++
        if (carriedEquipment.artillery != ArtilleryGear.NONE) count++
        return count
    }

    /**
     * 创建副本并更新位置
     */
    fun moveTo(newCoord: HexCoord): GameUnit {
        return copy(
            coord = newCoord,
            currentMovePower = currentMovePower - coord.distanceTo(newCoord)
        )
    }

    /**
     * 消耗移动力
     */
    fun consumeMovePower(cost: Int): GameUnit {
        return copy(currentMovePower = (currentMovePower - cost).coerceAtLeast(0))
    }

    /**
     * 重置移动力
     */
    fun resetMovePower(): GameUnit {
        return copy(currentMovePower = getActualMovePower())
    }

    /**
     * 进入守备状态
     */
    fun enterDefense(): GameUnit {
        return copy(
            state = UnitState.DEFENSE,
            consecutiveDefenseTurns = consecutiveDefenseTurns + 1
        )
    }

    /**
     * 切换状态
     */
    fun changeState(newState: UnitState): GameUnit {
        return copy(
            state = newState,
            consecutiveDefenseTurns = if (newState == UnitState.DEFENSE) 0 else consecutiveDefenseTurns
        )
    }

    /**
     * 回合结束处理（消耗体力、士气等）
     */
    fun onTurnEnd(): GameUnit {
        val newStamina = if (state == UnitState.REST) {
            staminaLevel.recover()
        } else if (state == UnitState.MARCH || state == UnitState.RAID) {
            staminaLevel.consume()
        } else {
            staminaLevel
        }

        return copy(
            staminaLevel = newStamina,
            currentMovePower = getActualMovePower()
        )
    }
}

/**
 * 单位安全状态
 */
data class UnitSafetyState(
    val unit: GameUnit,
    val isUnderZOC: Boolean,
    val nearbyEnemies: Int
)