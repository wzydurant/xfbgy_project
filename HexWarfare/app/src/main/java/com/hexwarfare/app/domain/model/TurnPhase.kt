package com.hexwarfare.app.domain.model

/**
 * 回合阶段封闭类
 */
sealed class TurnPhase(val displayName: String) {
    /**
     * 将军命令阶段 - 将军可以下达命令给从属单位
     */
    data object GeneralCommand : TurnPhase("将军命令")

    /**
     * 参谋长命令阶段 - 参谋长可以下达命令
     */
    data object ChiefOfStaffCommand : TurnPhase("参谋长命令")

    /**
     * 补给阶段 - 资源产出结算、粮食补给判定等
     */
    data object Supply : TurnPhase("补给阶段")

    /**
     * 回合结算阶段 - 处理回合结束的各种结算
     */
    data object Settlement : TurnPhase("回合结算")

    /**
     * 获取下一个阶段
     */
    fun next(): TurnPhase = when (this) {
        GeneralCommand -> ChiefOfStaffCommand
        ChiefOfStaffCommand -> Supply
        Supply -> Settlement
        Settlement -> GeneralCommand
    }

    companion object {
        /**
         * 判断是否是新回合开始
         */
        fun isNewTurn(phase: TurnPhase): Boolean = phase == GeneralCommand
    }
}

/**
 * 回合数据类
 */
data class Turn(
    val turnNumber: Int = 1,
    val phase: TurnPhase = TurnPhase.GeneralCommand,
    val commander: Commander = Commander.PLAYER_GENERAL
)

/**
 * 指挥官枚举
 */
enum class Commander {
    PLAYER_GENERAL,    // 玩家将军
    PLAYER_CHIEF,      // 玩家参谋长
    ENEMY_GENERAL,     // 敌方将军
    ENEMY_CHIEF        // 敌方参谋长
}

/**
 * 命令执行记录
 */
data class CommandRecord(
    val unitId: String,
    val commandType: CommandType,
    val fromCoord: HexCoord,
    val toCoord: HexCoord,
    val turnNumber: Int,
    val phase: TurnPhase
)

/**
 * 命令类型枚举
 */
enum class CommandType {
    MOVE,
    CHANGE_STATE,
    ATTACK,
    SUPPLY
}

/**
 * 路径结果
 */
data class PathResult(
    val path: List<HexCoord>,
    val totalCost: Int,
    val remainingMovePower: Int
)

/**
 * 可移动区域结果
 */
data class MoveRangeResult(
    val reachableTiles: Set<HexCoord>,
    val moveCosts: Map<HexCoord, Int>
)