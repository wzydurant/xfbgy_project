package com.hexwarfare.app.domain.model;

/**
 * 纪律等级
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0003\u001a\u00020\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\b\u00a8\u0006\t"}, d2 = {"Lcom/hexwarfare/app/domain/model/DisciplineLevel;", "", "(Ljava/lang/String;I)V", "getDisciplineValue", "", "BROKEN", "LOOSE", "NORMAL", "STRICT", "app_debug"})
public enum DisciplineLevel {
    /*public static final*/ BROKEN /* = new BROKEN() */,
    /*public static final*/ LOOSE /* = new LOOSE() */,
    /*public static final*/ NORMAL /* = new NORMAL() */,
    /*public static final*/ STRICT /* = new STRICT() */;
    
    DisciplineLevel() {
    }
    
    public final int getDisciplineValue() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.hexwarfare.app.domain.model.DisciplineLevel> getEntries() {
        return null;
    }
}