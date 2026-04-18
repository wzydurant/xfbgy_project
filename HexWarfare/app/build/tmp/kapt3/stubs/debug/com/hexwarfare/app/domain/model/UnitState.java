package com.hexwarfare.app.domain.model;

/**
 * 单位状态
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\b\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0007\u001a\u00020\bJ\u0006\u0010\t\u001a\u00020\nR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006j\u0002\b\u000bj\u0002\b\fj\u0002\b\rj\u0002\b\u000ej\u0002\b\u000fj\u0002\b\u0010j\u0002\b\u0011\u00a8\u0006\u0012"}, d2 = {"Lcom/hexwarfare/app/domain/model/UnitState;", "", "displayName", "", "(Ljava/lang/String;ILjava/lang/String;)V", "getDisplayName", "()Ljava/lang/String;", "getMovePowerBonus", "", "isMobile", "", "BATTLE_FORMATION_STRICT", "BATTLE_FORMATION_LOOSE", "MARCH", "REST", "DEFENSE", "TRANSPORT", "RAID", "app_debug"})
public enum UnitState {
    /*public static final*/ BATTLE_FORMATION_STRICT /* = new BATTLE_FORMATION_STRICT(null) */,
    /*public static final*/ BATTLE_FORMATION_LOOSE /* = new BATTLE_FORMATION_LOOSE(null) */,
    /*public static final*/ MARCH /* = new MARCH(null) */,
    /*public static final*/ REST /* = new REST(null) */,
    /*public static final*/ DEFENSE /* = new DEFENSE(null) */,
    /*public static final*/ TRANSPORT /* = new TRANSPORT(null) */,
    /*public static final*/ RAID /* = new RAID(null) */;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String displayName = null;
    
    UnitState(java.lang.String displayName) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDisplayName() {
        return null;
    }
    
    public final boolean isMobile() {
        return false;
    }
    
    public final int getMovePowerBonus() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.hexwarfare.app.domain.model.UnitState> getEntries() {
        return null;
    }
}