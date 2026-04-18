package com.hexwarfare.app.domain.model;

/**
 * 单位数据类
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u001e\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001BM\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u0012\u0006\u0010\u000e\u001a\u00020\u000f\u0012\u0006\u0010\u0010\u001a\u00020\u0011\u0012\u0006\u0010\u0012\u001a\u00020\u0013\u00a2\u0006\u0002\u0010\u0014J\t\u0010\'\u001a\u00020\u0003H\u00c6\u0003J\t\u0010(\u001a\u00020\u0005H\u00c6\u0003J\t\u0010)\u001a\u00020\u0007H\u00c6\u0003J\t\u0010*\u001a\u00020\tH\u00c6\u0003J\t\u0010+\u001a\u00020\u000bH\u00c6\u0003J\t\u0010,\u001a\u00020\rH\u00c6\u0003J\t\u0010-\u001a\u00020\u000fH\u00c6\u0003J\t\u0010.\u001a\u00020\u0011H\u00c6\u0003J\t\u0010/\u001a\u00020\u0013H\u00c6\u0003Jc\u00100\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u000e\u001a\u00020\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\u00112\b\b\u0002\u0010\u0012\u001a\u00020\u0013H\u00c6\u0001J\u0013\u00101\u001a\u0002022\b\u00103\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00104\u001a\u000205H\u00d6\u0001J\t\u00106\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0011\u0010\u000e\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0011\u0010\f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001eR\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010 R\u0011\u0010\u0012\u001a\u00020\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\"R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010$R\u0011\u0010\u0010\u001a\u00020\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010&\u00a8\u00067"}, d2 = {"Lcom/hexwarfare/app/domain/model/Unit;", "", "id", "", "coord", "Lcom/hexwarfare/app/domain/model/HexCoord;", "faction", "Lcom/hexwarfare/app/domain/model/Faction;", "personnelLevel", "Lcom/hexwarfare/app/domain/model/PersonnelLevel;", "staminaLevel", "Lcom/hexwarfare/app/domain/model/StaminaLevel;", "moraleLevel", "Lcom/hexwarfare/app/domain/model/MoraleLevel;", "disciplineLevel", "Lcom/hexwarfare/app/domain/model/DisciplineLevel;", "state", "Lcom/hexwarfare/app/domain/model/UnitState;", "safety", "Lcom/hexwarfare/app/domain/model/UnitSafety;", "(Ljava/lang/String;Lcom/hexwarfare/app/domain/model/HexCoord;Lcom/hexwarfare/app/domain/model/Faction;Lcom/hexwarfare/app/domain/model/PersonnelLevel;Lcom/hexwarfare/app/domain/model/StaminaLevel;Lcom/hexwarfare/app/domain/model/MoraleLevel;Lcom/hexwarfare/app/domain/model/DisciplineLevel;Lcom/hexwarfare/app/domain/model/UnitState;Lcom/hexwarfare/app/domain/model/UnitSafety;)V", "getCoord", "()Lcom/hexwarfare/app/domain/model/HexCoord;", "getDisciplineLevel", "()Lcom/hexwarfare/app/domain/model/DisciplineLevel;", "getFaction", "()Lcom/hexwarfare/app/domain/model/Faction;", "getId", "()Ljava/lang/String;", "getMoraleLevel", "()Lcom/hexwarfare/app/domain/model/MoraleLevel;", "getPersonnelLevel", "()Lcom/hexwarfare/app/domain/model/PersonnelLevel;", "getSafety", "()Lcom/hexwarfare/app/domain/model/UnitSafety;", "getStaminaLevel", "()Lcom/hexwarfare/app/domain/model/StaminaLevel;", "getState", "()Lcom/hexwarfare/app/domain/model/UnitState;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
public final class Unit {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String id = null;
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
    
    public Unit(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.HexCoord coord, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.Faction faction, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.PersonnelLevel personnelLevel, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.StaminaLevel staminaLevel, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.MoraleLevel moraleLevel, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.DisciplineLevel disciplineLevel, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.UnitState state, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.UnitSafety safety) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getId() {
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
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.HexCoord component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.Faction component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.PersonnelLevel component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.StaminaLevel component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.MoraleLevel component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.DisciplineLevel component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.UnitState component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.UnitSafety component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.Unit copy(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.HexCoord coord, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.Faction faction, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.PersonnelLevel personnelLevel, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.StaminaLevel staminaLevel, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.MoraleLevel moraleLevel, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.DisciplineLevel disciplineLevel, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.UnitState state, @org.jetbrains.annotations.NotNull()
    com.hexwarfare.app.domain.model.UnitSafety safety) {
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