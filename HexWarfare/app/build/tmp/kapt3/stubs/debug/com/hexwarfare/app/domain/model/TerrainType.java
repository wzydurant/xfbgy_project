package com.hexwarfare.app.domain.model;

/**
 * 地形类型枚举
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0010\u000b\n\u0002\b\u0006\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\'\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0006\u0010\u000f\u001a\u00020\u0010R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\nR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\nj\u0002\b\u0011j\u0002\b\u0012j\u0002\b\u0013j\u0002\b\u0014j\u0002\b\u0015\u00a8\u0006\u0016"}, d2 = {"Lcom/hexwarfare/app/domain/model/TerrainType;", "", "moveCost", "", "visionBonus", "defenseBonus", "displayName", "", "(Ljava/lang/String;IIIILjava/lang/String;)V", "getDefenseBonus", "()I", "getDisplayName", "()Ljava/lang/String;", "getMoveCost", "getVisionBonus", "canPass", "", "PLAINS", "MOUNTAIN", "RIVER", "BUILDING", "IMPASSABLE", "app_debug"})
public enum TerrainType {
    /*public static final*/ PLAINS /* = new PLAINS(0, 0, 0, null) */,
    /*public static final*/ MOUNTAIN /* = new MOUNTAIN(0, 0, 0, null) */,
    /*public static final*/ RIVER /* = new RIVER(0, 0, 0, null) */,
    /*public static final*/ BUILDING /* = new BUILDING(0, 0, 0, null) */,
    /*public static final*/ IMPASSABLE /* = new IMPASSABLE(0, 0, 0, null) */;
    private final int moveCost = 0;
    private final int visionBonus = 0;
    private final int defenseBonus = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String displayName = null;
    
    TerrainType(int moveCost, int visionBonus, int defenseBonus, java.lang.String displayName) {
    }
    
    public final int getMoveCost() {
        return 0;
    }
    
    public final int getVisionBonus() {
        return 0;
    }
    
    public final int getDefenseBonus() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDisplayName() {
        return null;
    }
    
    public final boolean canPass() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.hexwarfare.app.domain.model.TerrainType> getEntries() {
        return null;
    }
}