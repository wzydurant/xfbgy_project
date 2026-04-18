package com.hexwarfare.app.domain.model;

/**
 * 命令配额系统
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u000b\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J>\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\t2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u00042\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\u0011\u001a\u00020\u0004J \u0010\u0012\u001a\u00020\u000f2\u0006\u0010\u0013\u001a\u00020\u00042\u0006\u0010\u0014\u001a\u00020\u00042\b\b\u0002\u0010\u0015\u001a\u00020\u000fJ\u0018\u0010\u0016\u001a\u00020\u00042\u0006\u0010\u0017\u001a\u00020\u00042\b\b\u0002\u0010\u0018\u001a\u00020\u000fJ\u000e\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\u0019\u001a\u00020\tR\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001a"}, d2 = {"Lcom/hexwarfare/app/domain/model/CommandSystem;", "", "()V", "BASE_COMMAND_DISTANCE", "", "BASE_COMMAND_QUOTA", "canChangeState", "Lcom/hexwarfare/app/domain/model/StateChangeResult;", "currentState", "Lcom/hexwarfare/app/domain/model/UnitState;", "targetState", "moraleLevel", "Lcom/hexwarfare/app/domain/model/MoraleLevel;", "consecutiveDefenseTurns", "isUnderZOC", "", "isVulnerableState", "equipmentCount", "canUseCommand", "quotaLeft", "distance", "isGeneralCommand", "getCommandQuota", "unitCount", "isChiefOfStaff", "state", "app_debug"})
public final class CommandSystem {
    
    /**
     * 普通将军的基础命令配额
     */
    public static final int BASE_COMMAND_QUOTA = 6;
    
    /**
     * 基础命令距离限制（格）
     */
    public static final int BASE_COMMAND_DISTANCE = 5;
    @org.jetbrains.annotations.NotNull()
    public static final com.hexwarfare.app.domain.model.CommandSystem INSTANCE = null;
    
    private CommandSystem() {
        super();
    }
    
    /**
     * 检查是否可以使用命令
     * @param quotaLeft 剩余配额
     * @param distance 命令距离
     * @param isGeneralCommand 是否是本队命令（将军直接命令）
     * @return 是否可以使用命令
     */
    public final boolean canUseCommand(int quotaLeft, int distance, boolean isGeneralCommand) {
        return false;
    }
    
    /**
     * 获取单位的命令配额
     * @param unitCount 从属单位数量
     * @param isChiefOfStaff 是否是参谋长
     * @return 可用配额
     */
    public final int getCommandQuota(int unitCount, boolean isChiefOfStaff) {
        return 0;
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
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.StateChangeResult canChangeState(@org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.UnitState currentState, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.UnitState targetState, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.MoraleLevel moraleLevel, int consecutiveDefenseTurns, boolean isUnderZOC, boolean isVulnerableState, int equipmentCount) {
        return null;
    }
    
    /**
     * 判断是否为脆弱状态
     */
    public final boolean isVulnerableState(@org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.UnitState state) {
        return false;
    }
}