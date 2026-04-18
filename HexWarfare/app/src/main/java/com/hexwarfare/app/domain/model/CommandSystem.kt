package com.hexwarfare.app.domain.model

/**
 * 命令配额系统
 */
object CommandSystem {

    /**
     * 普通将军的基础命令配额
     */
    const val BASE_COMMAND_QUOTA = 6

    /**
     * 基础命令距离限制（格）
     */
    const val BASE_COMMAND_DISTANCE = 5

    /**
     * 检查是否可以使用命令
     * @param quotaLeft 剩余配额
     * @param distance 命令距离
     * @param isGeneralCommand 是否是本队命令（将军直接命令）
     * @return 是否可以使用命令
     */
    fun canUseCommand(
        quotaLeft: Int,
        distance: Int,
        isGeneralCommand: Boolean = false
    ): Boolean {
        // 本队命令不计入配额
        if (isGeneralCommand) return true
        // 检查配额
        if (quotaLeft <= 0) return false
        // 检查距离
        if (distance > BASE_COMMAND_DISTANCE) return false
        return true
    }

    /**
     * 获取单位的命令配额
     * @param unitCount 从属单位数量
     * @param isChiefOfStaff 是否是参谋长
     * @return 可用配额
     */
    fun getCommandQuota(
        unitCount: Int,
        isChiefOfStaff: Boolean = false
    ): Int {
        // 参谋长配额可能不同，这里简化处理
        return if (isChiefOfStaff) {
            (BASE_COMMAND_QUOTA * 0.8).toInt() // 参谋长80%配额
        } else {
            BASE_COMMAND_QUOTA
        }
    }

    /**
     * 检查状态切换是否允许
     * @param currentState 当前状态
     * @param targetState 目标状态
     * @param moraleLevel 士气等级
     * @param consecutiveDefenseTurns 连续守备回合数
     * @param isUnderZOC 是否在敌方ZOC内
     * @param isVulnerableState 是否脆弱状态
     * @param equipmentCount 当前携带装备数
     * @return 切换是否允许及原因
     */
    fun canChangeState(
        currentState: UnitState,
        targetState: UnitState,
        moraleLevel: MoraleLevel,
        consecutiveDefenseTurns: Int,
        isUnderZOC: Boolean,
        isVulnerableState: Boolean,
        equipmentCount: Int
    ): StateChangeResult {
        // 在ZOC内不能切换状态
        if (isUnderZOC) {
            return StateChangeResult(false, "处于敌方ZOC范围内，无法切换状态")
        }

        // 守备状态最多3回合
        if (targetState == UnitState.DEFENSE) {
            if (consecutiveDefenseTurns >= 3) {
                return StateChangeResult(false, "守备状态已达3回合上限")
            }
            // 需要士气>=HIGH（昂首挺胸）才能守备
            if (moraleLevel != MoraleLevel.HIGH) {
                return StateChangeResult(false, "士气需要达到高昂才能进入守备状态")
            }
        }

        // 野战严整仅将军可下令
        if (targetState == UnitState.BATTLE_FORMATION_STRICT) {
            return StateChangeResult(false, "野战严整状态仅将军可直接下令")
        }

        // 脆弱状态装备限制（≤1）
        if (isVulnerableState && equipmentCount > 1) {
            return StateChangeResult(false, "脆弱状态下携带装备不能超过1件")
        }

        return StateChangeResult(true, "可以切换状态")
    }

    /**
     * 判断是否为脆弱状态
     */
    fun isVulnerableState(state: UnitState): Boolean {
        return state in listOf(
            UnitState.REST,
            UnitState.TRANSPORT,
            UnitState.RAID
        )
    }
}

/**
 * 状态切换结果
 */
data class StateChangeResult(
    val allowed: Boolean,
    val reason: String
)

/**
 * 补给阶段结果
 */
data class SupplyResult(
    val resourceProduced: Int,
    val foodSupplied: Boolean,
    val newUnitsFormed: List<String>,
    val equipmentAdjusted: Boolean
)

/**
 * 回合结算结果
 */
data class SettlementResult(
    val moraleChanged: Map<String, MoraleLevel>,
    val staminaChanged: Map<String, StaminaLevel>,
    val casualties: Map<String, Int>,
    val zocEffects: Map<String, Boolean>
)