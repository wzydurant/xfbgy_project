package com.hexwarfare.app.domain.model;

/**
 * 体力等级
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0006\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0003\u001a\u00020\u0000J\u0006\u0010\u0004\u001a\u00020\u0005J\u0006\u0010\u0006\u001a\u00020\u0000j\u0002\b\u0007j\u0002\b\bj\u0002\b\tj\u0002\b\n\u00a8\u0006\u000b"}, d2 = {"Lcom/hexwarfare/app/domain/model/StaminaLevel;", "", "(Ljava/lang/String;I)V", "consume", "getStaminaValue", "", "recover", "EXHAUSTED", "TIRED", "NORMAL", "FRESH", "app_debug"})
public enum StaminaLevel {
    /*public static final*/ EXHAUSTED /* = new EXHAUSTED() */,
    /*public static final*/ TIRED /* = new TIRED() */,
    /*public static final*/ NORMAL /* = new NORMAL() */,
    /*public static final*/ FRESH /* = new FRESH() */;
    
    StaminaLevel() {
    }
    
    public final int getStaminaValue() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.StaminaLevel consume() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hexwarfare.app.domain.model.StaminaLevel recover() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.hexwarfare.app.domain.model.StaminaLevel> getEntries() {
        return null;
    }
}