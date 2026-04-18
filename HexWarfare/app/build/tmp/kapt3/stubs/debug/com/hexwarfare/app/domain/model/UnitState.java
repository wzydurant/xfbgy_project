package com.hexwarfare.app.domain.model;

/**
 * 单位状态
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\t\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\t\u00a8\u0006\n"}, d2 = {"Lcom/hexwarfare/app/domain/model/UnitState;", "", "(Ljava/lang/String;I)V", "BATTLE_FORMATION_STRICT", "BATTLE_FORMATION_LOOSE", "MARCH", "REST", "DEFENSE", "TRANSPORT", "RAID", "app_debug"})
public enum UnitState {
    /*public static final*/ BATTLE_FORMATION_STRICT /* = new BATTLE_FORMATION_STRICT() */,
    /*public static final*/ BATTLE_FORMATION_LOOSE /* = new BATTLE_FORMATION_LOOSE() */,
    /*public static final*/ MARCH /* = new MARCH() */,
    /*public static final*/ REST /* = new REST() */,
    /*public static final*/ DEFENSE /* = new DEFENSE() */,
    /*public static final*/ TRANSPORT /* = new TRANSPORT() */,
    /*public static final*/ RAID /* = new RAID() */;
    
    UnitState() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.hexwarfare.app.domain.model.UnitState> getEntries() {
        return null;
    }
}