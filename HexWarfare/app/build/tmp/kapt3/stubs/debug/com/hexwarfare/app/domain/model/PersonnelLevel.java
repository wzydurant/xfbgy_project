package com.hexwarfare.app.domain.model;

/**
 * 兵种等级
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0003\u001a\u00020\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007\u00a8\u0006\b"}, d2 = {"Lcom/hexwarfare/app/domain/model/PersonnelLevel;", "", "(Ljava/lang/String;I)V", "getPersonnelCount", "", "LOW", "MEDIUM", "HIGH", "app_debug"})
public enum PersonnelLevel {
    /*public static final*/ LOW /* = new LOW() */,
    /*public static final*/ MEDIUM /* = new MEDIUM() */,
    /*public static final*/ HIGH /* = new HIGH() */;
    
    PersonnelLevel() {
    }
    
    public final int getPersonnelCount() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.hexwarfare.app.domain.model.PersonnelLevel> getEntries() {
        return null;
    }
}