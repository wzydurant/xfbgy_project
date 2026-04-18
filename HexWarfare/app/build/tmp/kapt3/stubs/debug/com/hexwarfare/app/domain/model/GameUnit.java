package com.hexwarfare.app.domain.model;

/**
 * 单位数据类
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b7\n\u0002\u0010 \n\u0002\b\u0006\b\u0086\b\u0018\u00002\u00020\u0001B\u0087\u0001\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\u0006\u0010\t\u001a\u00020\n\u0012\u0006\u0010\u000b\u001a\u00020\f\u0012\u0006\u0010\r\u001a\u00020\u000e\u0012\u0006\u0010\u000f\u001a\u00020\u0010\u0012\u0006\u0010\u0011\u001a\u00020\u0012\u0012\u0006\u0010\u0013\u001a\u00020\u0014\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u0016\u0012\b\b\u0002\u0010\u0017\u001a\u00020\u0016\u0012\b\b\u0002\u0010\u0018\u001a\u00020\u0016\u0012\b\b\u0002\u0010\u0019\u001a\u00020\u001a\u0012\b\b\u0002\u0010\u001b\u001a\u00020\u001c\u00a2\u0006\u0002\u0010\u001dJ\u000e\u00108\u001a\u00020\u00002\u0006\u00109\u001a\u00020\u0012J\t\u0010:\u001a\u00020\u0003H\u00c6\u0003J\t\u0010;\u001a\u00020\u0014H\u00c6\u0003J\t\u0010<\u001a\u00020\u0016H\u00c6\u0003J\t\u0010=\u001a\u00020\u0016H\u00c6\u0003J\t\u0010>\u001a\u00020\u0016H\u00c6\u0003J\t\u0010?\u001a\u00020\u001aH\u00c6\u0003J\t\u0010@\u001a\u00020\u001cH\u00c6\u0003J\t\u0010A\u001a\u00020\u0003H\u00c6\u0003J\t\u0010B\u001a\u00020\u0006H\u00c6\u0003J\t\u0010C\u001a\u00020\bH\u00c6\u0003J\t\u0010D\u001a\u00020\nH\u00c6\u0003J\t\u0010E\u001a\u00020\fH\u00c6\u0003J\t\u0010F\u001a\u00020\u000eH\u00c6\u0003J\t\u0010G\u001a\u00020\u0010H\u00c6\u0003J\t\u0010H\u001a\u00020\u0012H\u00c6\u0003J\u000e\u0010I\u001a\u00020\u00002\u0006\u0010J\u001a\u00020\u0016J\u009f\u0001\u0010K\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u00102\b\b\u0002\u0010\u0011\u001a\u00020\u00122\b\b\u0002\u0010\u0013\u001a\u00020\u00142\b\b\u0002\u0010\u0015\u001a\u00020\u00162\b\b\u0002\u0010\u0017\u001a\u00020\u00162\b\b\u0002\u0010\u0018\u001a\u00020\u00162\b\b\u0002\u0010\u0019\u001a\u00020\u001a2\b\b\u0002\u0010\u001b\u001a\u00020\u001cH\u00c6\u0001J\u0006\u0010L\u001a\u00020\u0000J\u0013\u0010M\u001a\u00020\u001a2\b\u0010N\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\u0006\u0010O\u001a\u00020\u0016J\u0006\u0010P\u001a\u00020\u0016J\t\u0010Q\u001a\u00020\u0016H\u00d6\u0001J\u0014\u0010R\u001a\u00020\u001a2\f\u0010S\u001a\b\u0012\u0004\u0012\u00020\u00000TJ\u000e\u0010U\u001a\u00020\u00002\u0006\u0010V\u001a\u00020\u0006J\u0006\u0010W\u001a\u00020\u0000J\u0006\u0010X\u001a\u00020\u0000J\t\u0010Y\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0015\u001a\u00020\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001fR\u0011\u0010\u001b\u001a\u00020\u001c\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u0011\u0010\u0018\u001a\u00020\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u001fR\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010$R\u0011\u0010\u0017\u001a\u00020\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u001fR\u0011\u0010\u000f\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\'R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010)R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010+R\u0011\u0010\u0019\u001a\u00020\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010,R\u0011\u0010\r\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010.R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010+R\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u00101R\u0011\u0010\u0013\u001a\u00020\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u00103R\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u00105R\u0011\u0010\u0011\u001a\u00020\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b6\u00107\u00a8\u0006Z"}, d2 = {"Lcom/hexwarfare/app/domain/model/GameUnit;", "", "id", "", "name", "coord", "Lcom/hexwarfare/app/domain/model/HexCoord;", "faction", "Lcom/hexwarfare/app/domain/model/Faction;", "personnelLevel", "Lcom/hexwarfare/app/domain/model/PersonnelLevel;", "staminaLevel", "Lcom/hexwarfare/app/domain/model/StaminaLevel;", "moraleLevel", "Lcom/hexwarfare/app/domain/model/MoraleLevel;", "disciplineLevel", "Lcom/hexwarfare/app/domain/model/DisciplineLevel;", "state", "Lcom/hexwarfare/app/domain/model/UnitState;", "safety", "Lcom/hexwarfare/app/domain/model/UnitSafety;", "baseMovePower", "", "currentMovePower", "consecutiveDefenseTurns", "isGeneralUnit", "", "carriedEquipment", "Lcom/hexwarfare/app/domain/model/CarriedEquipment;", "(Ljava/lang/String;Ljava/lang/String;Lcom/hexwarfare/app/domain/model/HexCoord;Lcom/hexwarfare/app/domain/model/Faction;Lcom/hexwarfare/app/domain/model/PersonnelLevel;Lcom/hexwarfare/app/domain/model/StaminaLevel;Lcom/hexwarfare/app/domain/model/MoraleLevel;Lcom/hexwarfare/app/domain/model/DisciplineLevel;Lcom/hexwarfare/app/domain/model/UnitState;Lcom/hexwarfare/app/domain/model/UnitSafety;IIIZLcom/hexwarfare/app/domain/model/CarriedEquipment;)V", "getBaseMovePower", "()I", "getCarriedEquipment", "()Lcom/hexwarfare/app/domain/model/CarriedEquipment;", "getConsecutiveDefenseTurns", "getCoord", "()Lcom/hexwarfare/app/domain/model/HexCoord;", "getCurrentMovePower", "getDisciplineLevel", "()Lcom/hexwarfare/app/domain/model/DisciplineLevel;", "getFaction", "()Lcom/hexwarfare/app/domain/model/Faction;", "getId", "()Ljava/lang/String;", "()Z", "getMoraleLevel", "()Lcom/hexwarfare/app/domain/model/MoraleLevel;", "getName", "getPersonnelLevel", "()Lcom/hexwarfare/app/domain/model/PersonnelLevel;", "getSafety", "()Lcom/hexwarfare/app/domain/model/UnitSafety;", "getStaminaLevel", "()Lcom/hexwarfare/app/domain/model/StaminaLevel;", "getState", "()Lcom/hexwarfare/app/domain/model/UnitState;", "changeState", "newState", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "consumeMovePower", "cost", "copy", "enterDefense", "equals", "other", "getActualMovePower", "getEquipmentCount", "hashCode", "isUnderZOC", "allUnits", "", "moveTo", "newCoord", "onTurnEnd", "resetMovePower", "toString", "app_debug"})
public final class GameUnit {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String name = null;
    @org.jetbrains.annotations.NotNull()
    private final com.hexwarfare.app.domain.model.HexCoord coord = null;
    @org.jetbrains.annotations.NotNull()
    private final com.hexwarfare.app.domain.model.Faction faction = null;
    @org.jetbrains.annotations.NotNull()
    private final com.hexwarfare.app.domain.model.PersonnelLevel personnelLevel = null;
    @org.jetbrains.annotations.NotNull()
    private final com.hexwarfare.app.domain.model.StaminaLevel staminaLevel = null;
    @org.jetbrains.annotations.NotNull()
    private final com.hexwarfare.app.domain.model.MoraleLevel moraleLevel = null;
    @org.jetbrains.annotations.NotNull()
    private final com.hexwarfare.app.domain.model.DisciplineLevel disciplineLevel = null;
    @org.jetbrains.annotations.NotNull()
    private final com.hexwarfare.app.domain.model.UnitState state = null;
    @org.jetbrains.annotations.NotNull()
    private final com.hexwarfare.app.domain.model.UnitSafety safety = null;
    private final int baseMovePower = 0;
    private final int currentMovePower = 0;
    private final int consecutiveDefenseTurns = 0;
    private final boolean isGeneralUnit = false;
    @org.jetbrains.annotations.NotNull()
    private final com.hexwarfare.app.domain.model.CarriedEquipment carriedEquipment = null;
    
    public GameUnit(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.HexCoord coord, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.Faction faction, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.PersonnelLevel personnelLevel, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.StaminaLevel staminaLevel, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.MoraleLevel moraleLevel, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.DisciplineLevel disciplineLevel, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.UnitState state, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.UnitSafety safety, int baseMovePower, int currentMovePower, int consecutiveDefenseTurns, boolean isGeneralUnit, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.CarriedEquipment carriedEquipment) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.HexCoord getCoord() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.Faction getFaction() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.PersonnelLevel getPersonnelLevel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.StaminaLevel getStaminaLevel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.MoraleLevel getMoraleLevel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.DisciplineLevel getDisciplineLevel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.UnitState getState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.UnitSafety getSafety() {
        return null;
    }
    
    public final int getBaseMovePower() {
        return 0;
    }
    
    public final int getCurrentMovePower() {
        return 0;
    }
    
    public final int getConsecutiveDefenseTurns() {
        return 0;
    }
    
    public final boolean isGeneralUnit() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.CarriedEquipment getCarriedEquipment() {
        return null;
    }
    
    /**
     * 获取当前移动力
     */
    public final int getActualMovePower() {
        return 0;
    }
    
    /**
     * 检查是否在ZOC内（简化实现）
     */
    public final boolean isUnderZOC(@org.jetbrains.annotations.NotNull()
    java.util.List<com.hexwarfare.app.domain.model.GameUnit> allUnits) {
        return false;
    }
    
    /**
     * 获取携带装备数量
     */
    public final int getEquipmentCount() {
        return 0;
    }
    
    /**
     * 创建副本并更新位置
     */
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.GameUnit moveTo(@org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.HexCoord newCoord) {
        return null;
    }
    
    /**
     * 消耗移动力
     */
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.GameUnit consumeMovePower(int cost) {
        return null;
    }
    
    /**
     * 重置移动力
     */
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.GameUnit resetMovePower() {
        return null;
    }
    
    /**
     * 进入守备状态
     */
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.GameUnit enterDefense() {
        return null;
    }
    
    /**
     * 切换状态
     */
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.GameUnit changeState(@org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.UnitState newState) {
        return null;
    }
    
    /**
     * 回合结束处理（消耗体力、士气等）
     */
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.GameUnit onTurnEnd() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.UnitSafety component10() {
        return null;
    }
    
    public final int component11() {
        return 0;
    }
    
    public final int component12() {
        return 0;
    }
    
    public final int component13() {
        return 0;
    }
    
    public final boolean component14() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.CarriedEquipment component15() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.HexCoord component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.Faction component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.PersonnelLevel component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.StaminaLevel component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.MoraleLevel component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.DisciplineLevel component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.UnitState component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.GameUnit copy(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.HexCoord coord, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.Faction faction, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.PersonnelLevel personnelLevel, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.StaminaLevel staminaLevel, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.MoraleLevel moraleLevel, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.DisciplineLevel disciplineLevel, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.UnitState state, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.UnitSafety safety, int baseMovePower, int currentMovePower, int consecutiveDefenseTurns, boolean isGeneralUnit, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.CarriedEquipment carriedEquipment) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}